package org.automation.goofish.core.socket;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.automation.goofish.core.ConnectionProperties;
import org.automation.goofish.core.GoofishClient;
import org.automation.goofish.core.socket.msg.Message;
import org.automation.goofish.core.socket.msg.MsgDispatcher;
import org.automation.goofish.core.socket.msg.MsgReplyQ;
import org.automation.goofish.core.socket.msg.receive.ReceiveMsg;
import org.automation.goofish.core.socket.msg.send.*;
import org.automation.goofish.interceptor.AutoReplyService;
import org.automation.goofish.item.ItemInfo;
import org.automation.goofish.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.lookup;

@Lazy
@Component
public class GoofishSocket implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(lookup().lookupClass());

    @Autowired
    GoofishClient client;
    @Autowired
    MsgDispatcher dispatcher;
    @Autowired
    MsgReplyQ replyQ;
    @Autowired
    ConnectionProperties properties;
    @Autowired
    AutoReplyService replyService;
    ReactorNettyWebSocketClient delegate;

    private volatile long lastHeartbeatTime = System.currentTimeMillis();

    public boolean alive() {
        return System.currentTimeMillis() - lastHeartbeatTime < properties.getTimeout() * 1000L;
    }

    public Mono<Void> establish() {
        return client.getToken()
                .flatMap(token -> delegate.execute(properties.getSocketUrl(), session -> {

                    /*
                     * /req ----->
                     *     <------/ip-digest
                     * /getStat-->
                     *         <--/sync
                     * /syncDiff->
                     *          <-/ack
                     * */

                    // 1. registration request
                    Mono<Void> register = new RegMsg(properties.getDeviceId(), token).send(session);

                    // 2. receive msg
                    Flux<Void> receive = session.receive()
                            .map(WebSocketMessage::getPayloadAsText)
                            .doOnNext(msg -> logger.info("receive <--- msg {}", Message.prettyJson(msg)))
                            .flatMap(msg -> {
                                lastHeartbeatTime = System.currentTimeMillis();
                                ReceiveMsg recMsg = ReceiveMsg.parse(msg);

                                replyQ.pop(recMsg.getMid()).ifPresent(reply -> {
                                    // 构建消息上下文
                                    final LinkedList<Pair<String, String>> chatList = new LinkedList<>();
                                    recMsg.getBody().getUserMessageModels().forEach(model -> {
                                        String historyMsg = model.getMessage().getExtension().getReminderContent();
                                        String sender = model.getMessage().getExtension().getSenderUserId();
                                        chatList.add(new MutablePair<>(
                                                Objects.equals(sender, properties.getUserId()) ? "商家" : "买家",
                                                historyMsg
                                        ));
                                    });

                                    // 打印日志（保持原顺序）
                                    chatList.reversed().forEach(p -> logger.info(p.toString()));

                                    // 响应式处理流程
                                    client.getItemInfo(reply.getItemId()) // 返回Mono<JsonNode>
                                            .flatMap(itemInfo -> {
                                                ItemInfo i = JsonUtils.readValue(itemInfo.toString(), ItemInfo.class);
                                                String chatContext = chatList.reversed().stream()
                                                        .map(pair -> pair.getLeft() + ":" + pair.getRight())
                                                        .collect(Collectors.joining("\n"));

                                                return replyService.generateReply(
                                                        chatContext,
                                                        i.promptSellerSignature(),
                                                        i.promptSellerLocation(),
                                                        i.promptSellGoodDesc(),
                                                        i.promptSellGoodLabel(),
                                                        i.promptSellGoodPrice()
                                                );
                                            })
                                            .flatMap(botMsg -> {
                                                return new ReplyMsg(
                                                        reply.getChatId(),
                                                        reply.getReceiverId(),
                                                        properties.getUserId(),
                                                        botMsg
                                                ).send(session);
                                            })
                                            .subscribe(
                                                    null,
                                                    error -> logger.error("failed to process error", error)
                                            );
                                });

                                if (recMsg.hasSyncPushPackageMessage()) {
                                    MsgDispatcher.MsgContext msgContext = dispatcher.handle(recMsg, session);
                                    if (StringUtils.hasLength(msgContext.getMessagesQueryMid())) {
                                        replyQ.push(msgContext.getMessagesQueryMid(), msgContext.getChatId(), msgContext.getReceiverId(), msgContext.getItemId());
                                    }
                                }

                                return recMsg.hasIpDigest() ?
                                        new GetStatMsg().send(session) :
                                        recMsg.hasTooLong2Tag() ?
                                                new AckDiffMsg().send(session) : new AckMsg(recMsg).send(session);
                            });

                    // 3. heartbeat
                    Flux<Void> heartbeat = Flux.interval(Duration.ofSeconds(properties.getInterval()))
                            .flatMap(tick -> new HeartbeatMsg().send(session));

                    // 4. merge flow
                    return register
                            .thenMany(Flux.merge(
                                    receive,
                                    heartbeat
                            )).then();
                }));
    }

    @Override
    public void afterPropertiesSet() {
        HttpClient httpClient = HttpClient.create()
                .headers(headers -> {
                    headers.add(HttpHeaderNames.COOKIE, properties.getCookieStr());
                    headers.add(HttpHeaderNames.HOST, properties.getSocketUrl().getHost());
                    headers.add(HttpHeaderNames.CONNECTION, "Upgrade");
                    headers.add(HttpHeaderNames.PRAGMA, "no-cache");
                    headers.add(HttpHeaderNames.USER_AGENT, properties.getUserAgent());
                    headers.add(HttpHeaderNames.ORIGIN, properties.getHttpsUrl());
                    headers.add(HttpHeaderNames.ACCEPT_ENCODING, "gzip, deflate, br, zstd");
                    headers.add(HttpHeaderNames.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9");
                });

        delegate = new ReactorNettyWebSocketClient(httpClient, WebsocketClientSpec.builder().maxFramePayloadLength(1024 * 1024)); // 1MB
    }
}
