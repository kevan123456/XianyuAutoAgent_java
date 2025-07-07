package org.automation.goofish.core.socket;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.SneakyThrows;
import org.automation.goofish.core.ConnectionProperties;
import org.automation.goofish.core.GoofishClient;
import org.automation.goofish.core.socket.msg.MsgDispatcher;
import org.automation.goofish.core.socket.msg.receive.ReceiveMsg;
import org.automation.goofish.core.socket.msg.send.*;
import org.automation.goofish.data.ChatContext;
import org.automation.goofish.data.ChatRepository;
import org.automation.goofish.data.ItemContext;
import org.automation.goofish.data.ItemRepository;
import org.automation.goofish.interceptor.AutoReplyService;
import org.automation.goofish.item.ItemInfo;
import org.automation.goofish.utils.JsonUtils;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.invoke.MethodHandles.lookup;
import static org.automation.goofish.utils.JsonUtils.OBJECT_MAPPER;
import static org.springframework.util.StringUtils.hasLength;

@Lazy
@Component
public class GoofishSocket implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(lookup().lookupClass());

    @Autowired
    GoofishClient client;
    @Autowired
    MsgDispatcher dispatcher;
    @Autowired
    ConnectionProperties properties;
    @Autowired
    AutoReplyService replyService;

    @Autowired
    ItemRepository itemRepository;
    @Autowired
    ChatRepository chatRepository;
    ReactorNettyWebSocketClient delegate;

    public Mono<Void> establish() {
        return delegate.execute(properties.getSocketUrl(),
                session -> {
                    // send /reg request
                    return register(session)
                            .thenMany(Flux.merge(
                                    receive(session),
                                    heartbeat(session)
                            )).then();
                });
    }

    private Mono<Void> register(WebSocketSession session) {
        return client.getToken().flatMap(token ->
                new RegMsg(properties.getDeviceId(), token).send(session));
    }

    private final ConcurrentHashMap<String, Boolean> heartbeatMid = new ConcurrentHashMap<>();

    private Flux<Void> heartbeat(WebSocketSession session) {
        return Flux.interval(Duration.ofSeconds(properties.getInterval()))
                .flatMap(tick -> {
                    HeartbeatMsg heartbeatMsg = new HeartbeatMsg();
                    heartbeatMid.put(heartbeatMsg.getHeaders().getMid(), true);
                    return heartbeatMsg.send(session);
                });
    }

    @Value("${goofish.historical-data-maximum}")
    int historicalDataMaximum = 20;

    // request history chat
    private Mono<Void> history(ReceiveMsg recMsg, MsgDispatcher.MsgContext context, WebSocketSession session, ToReply toReply) {
        logger.trace("[{}] start fetching chat history process", recMsg.hashCode());
        if (recMsg.getMid().equals(context.getMid())) {
            logger.trace("[{}] mid of receive message is equal to which stored in message context: {}", recMsg.hashCode(), recMsg.getMid());
            ListUserMessageMsg listMsg = new ListUserMessageMsg(
                    context.getChatId(),
                    historicalDataMaximum
            );
            return chatRepository.findById(context.getChatId())
                    .doOnSuccess(chat -> {
                        if (chat != null) {
                            logger.trace("[{}] found chat history in db for chatId: {}", recMsg.hashCode(), context.getChatId());
                        }
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        logger.trace("[{}] querying historical chat records via api", recMsg.hashCode());
                        context.setMessagesQueryMid(listMsg.getHeaders().getMid());
                        return listMsg.send(session).then(Mono.empty());
                    }))
                    .flatMap(chat -> Mono.fromRunnable(() -> {
                        logger.info("loaded chat history from database");
                        logger.trace("[{}] store chat history {} into message context", recMsg.hashCode(), chat.getChatHistory());
                        context.setHistoryChat(chat.getChatHistory());
                        // Emit value if we got data from DB
                        if (toReply != null) {
                            logger.info("[{}] emitting pending reply {} to sink", recMsg.hashCode(), toReply);
                            toReply.responseSink.emitValue(recMsg, (signal, result) -> {
                                if (result.isFailure()) {
                                    logger.error("[{}] emit failed", recMsg.hashCode());
                                }
                                return result.isFailure();
                            });
                        }
                    }));
        }

        // get chat history from response
        if (recMsg.getMid().equals(context.getMessagesQueryMid())) {
            logger.trace("[{}] mid of receive message is equal to chat list response: {}", recMsg.hashCode(), context.getMessagesQueryMid());
            ArrayNode messagesArray = OBJECT_MAPPER.createArrayNode();

            if (recMsg.getBody() == null || recMsg.getBody().getUserMessageModels() == null) {
                if (recMsg.getCode() != 200) {
                    logger.error("encounter error with response of list user message, raw json: {}", recMsg.getRaw());
                }
                return Mono.empty();
            }

            recMsg.getBody().getUserMessageModels().forEach(model -> {
                ObjectNode messageNode = OBJECT_MAPPER.createObjectNode();
                messageNode.put("role",
                        Objects.equals(model.getMessage().getExtension().getSenderUserId(),
                                properties.getUserId()) ? "seller" : "buyer");
                messageNode.put("content",
                        model.getMessage().getExtension().getReminderContent());
                messageNode.put("timestamp",
                        model.getMessage().getCreateAt());
                messagesArray.add(messageNode);
            });
            ObjectNode chatHistoryJson = OBJECT_MAPPER.createObjectNode();
            chatHistoryJson.set("messages", messagesArray);
            logger.trace("[{}] built history chat list json success: {}", recMsg.hashCode(), chatHistoryJson);
            ChatContext chatContext = new ChatContext(
                    context.getChatId(),
                    chatHistoryJson.toString(),
                    context.getItemId()
            );
            logger.trace("[{}] insert history chat list json to database: {}", recMsg.hashCode(), chatHistoryJson);
            return chatRepository.insert(chatContext)
                    .doOnSuccess(saved -> {
                        context.setHistoryChat(chatContext.getChatHistory());
                        // Emit value after setting history from API
                        if (toReply != null) {
                            logger.info("[{}] unpacked received list chat history response, emitting mono", recMsg.hashCode());
                            toReply.responseSink.emitValue(recMsg, (signal, result) -> {
                                if (result.isFailure()) {
                                    logger.error("[{}] emit failed for api history response", recMsg.hashCode());
                                }
                                return result.isFailure();
                            });
                        }
                    })
                    .then();
        }
        return Mono.empty();
    }

    @SneakyThrows
    private Mono<ChatContext> updateChatHistory(MsgDispatcher.MsgContext ctx, String msg, boolean seller) {
        // 1. 解析现有历史记录
        ObjectNode historyJson = (ObjectNode) OBJECT_MAPPER.readTree(
                ctx.getHistoryChat() != null ? ctx.getHistoryChat() : """
                        {"messages":[]}
                        """.trim()
        );

        // 2. 创建新消息节点
        ObjectNode newMessage = OBJECT_MAPPER.createObjectNode()
                .put("role", seller ? "seller" : "buyer")
                .put("content", msg)
                .put("timestamp", System.currentTimeMillis());

        // 3. 添加到消息数组
        ArrayNode messages = (ArrayNode) historyJson.get("messages");
        messages.insert(0, newMessage); // 插入到数组开头（保持倒序）

        // 4. 更新上下文
        ctx.setHistoryChat(historyJson.toString());

        // 5. 保存到数据库
        return chatRepository.save(new ChatContext(
                ctx.getChatId(),
                historyJson.toString(),
                ctx.getItemId()
        ));
    }

    private Mono<Void> reply(WebSocketSession session, MsgDispatcher.MsgContext ctx) {
        logger.trace("[{}] dump message context before reply {}", ctx.getIdentity(), ctx);

        // 拼接最新消息到历史记录
        return Mono.fromCallable(() -> {
                    if (hasLength(ctx.getSendMessage())) {
                        return updateChatHistory(ctx, ctx.getSendMessage(), false);
                    }
                    return Mono.empty();
                })
                .then(new CheckRedPointMsg(ctx.getChatId(), ctx.getMessageId()).send(session))
                .then(loadItem(ctx))
                .then(Mono.defer(() -> {
                    // only reply when sender in not the receiver
                    if (!Objects.equals(properties.getUserId(), ctx.getReceiverId())) {
                        return botReply(session, ctx).flatMap(botMsg -> updateChatHistory(ctx, botMsg, true)).then();
                    }
                    return Mono.empty();
                }));
    }

    // 将商品信息加载到上下文中
    private Mono<Void> loadItem(MsgDispatcher.MsgContext msgContext) {
        final String itemId = msgContext.getItemId();
        logger.debug("starting to load item: {}", itemId);
        return itemRepository.findById(itemId)
                .switchIfEmpty(loadItemByApi(itemId))
                .flatMap(itemCtx -> {
                    // 情况1：数据库有记录且有效
                    if (itemCtx != null && itemCtx.getItemInfo() != null) {
                        logger.debug("found valid item info in db: {}", itemCtx.getItemInfo());
                        msgContext.setItemInfo(itemCtx.getItemInfo());
                    }
                    return Mono.empty();
                }).then();
    }

    public Mono<ItemContext> loadItemByApi(String itemId) {
        logger.info("send https requests to retrieve item info for: {}", itemId);
        return client.getItemInfo(itemId)
                .flatMap(itemJson -> {
                    String itemInfo = JsonUtils.toJson(JsonUtils.readValue(itemJson.toString(), ItemInfo.class));
                    logger.debug("api fallback success for item: {}", itemInfo);
                    ItemContext itemContext = new ItemContext(itemId, itemInfo);
                    return itemRepository.insert(itemContext)
                            .doOnSuccess(saved -> logger.debug("insert item {} to database successfully", itemId))
                            .then(Mono.just(itemContext));
                });
    }

    // 使用上下文处理回复
    private Mono<String> botReply(WebSocketSession session, MsgDispatcher.MsgContext msgContext) {
        try {
            // 1. 构建聊天历史
            String chatHistory = msgContext.getHistoryChat() != null ?
                    OBJECT_MAPPER.readTree(msgContext.getHistoryChat())
                            .path("messages")
                            .toString() : "";

            // 2. 获取上下文中的商品信息
            String itemInfo = msgContext.getItemInfo();

            ObjectNode prompt = OBJECT_MAPPER.createObjectNode();
            prompt.set("chat_history", JsonUtils.readTree(chatHistory));
            prompt.set("item_info", JsonUtils.readTree(itemInfo));

            // 3. 生成并发送回复
            return replyService.generateReply(prompt.toString())
                    .flatMap(botMsg -> new ReplyMsg(
                            msgContext.getChatId(),
                            msgContext.getReceiverId(),
                            properties.getUserId(),
                            botMsg
                    ).send(session).thenReturn(botMsg));
        } catch (Exception e) {
            logger.error("process reply failed", e);
            return Mono.empty();
        }
    }

    private final ConcurrentHashMap<String, ToReply> pendingRequests = new ConcurrentHashMap<>();

    // 辅助类：存储待处理的请求上下文
    private static class ToReply {
        final MsgDispatcher.MsgContext context;
        final Sinks.One<ReceiveMsg> responseSink = Sinks.one();

        ToReply(MsgDispatcher.MsgContext context) {
            this.context = context;
        }
    }

    private Flux<ReceiveMsg> receive(WebSocketSession session) {
        return session.receive()
                // 解析消息
                .flatMap(msg -> {
                    ReceiveMsg m = ReceiveMsg.parse(msg.getPayloadAsText());
                    logger.trace("receive <--- msg [mid:{}] [id:{}] {}", m.getMid(), m.hashCode(), JsonUtils.prettyJson(m.toJson()));
                    return Mono.just(m);
                })

                // 过滤心跳响应
                .filter(m -> !Boolean.TRUE.equals(heartbeatMid.remove(m.getMid())))

                // 处理IP摘要消息
                .flatMap(m -> m.hasIpDigest()
                        ? new GetStatMsg().send(session).then(Mono.just(m))
                        : Mono.just(m))

                // 处理过长标签消息
                .flatMap(m -> m.hasTooLong2Tag()
                        ? new AckDiffMsg().send(session).then(Mono.just(m))
                        : Mono.just(m))

                // 3. 处理同步推送包（核心逻辑）
                .flatMap(m -> {
                    if (m.getSyncData() == null || m.getSyncData().isEmpty()) {
                        return Mono.just(m);
                    }

                    // 只处理第一个syncData对象（或根据需要修改）
                    ReceiveMsg.SyncData data = m.getSyncData().getFirst();
                    return switch (data.getObjectType()) {
                        case 40000 -> dispatcher.handle(m, session)
                                .flatMap(context -> {
                                    ToReply pending = new ToReply(context);
                                    logger.info("put {} : {} into pending reply", m.getHeaders(), pending);
                                    pendingRequests.put(m.getMid(), pending);

                                    // send api request to get history
                                    return history(m, context, session, pending)
                                            .then(Mono.defer(() ->
                                                    pending.responseSink.asMono()
                                                            .timeout(Duration.ofSeconds(15))
                                                            .then(reply(session, pending.context))
                                                            .doFinally(s -> pendingRequests.remove(m.getMid()))
                                            ))
                                            .thenReturn(m);
                                });
                        case 40103 -> {
                            logger.debug("processing message with objectType 40103, mid: {}", m.getMid());
                            yield Mono.just(m);
                        }
                        case 37000 -> {
                            logger.debug("processing message with objectType 37000, mid: {}", m.getMid());
                            yield Mono.just(m);
                        }
                        case -1 -> {
                            logger.debug("processing default objectType (-1), mid: {}", m.getMid());
                            yield Mono.just(m);
                        }
                        default -> {
                            logger.debug("processing unknown objectType {}, mid: {}", data.getObjectType(), m.getMid());
                            yield Mono.just(m);
                        }
                    };
                })

                // 修改第4步：处理历史查询响应
                .flatMap(m -> {
                    Optional<ToReply> pending = pendingRequests.values().stream()
                            .filter(req -> m.getMid().equals(req.context.getMessagesQueryMid()))
                            .findFirst();

                    return pending.<Publisher<? extends ReceiveMsg>>map(toReply ->
                            history(m, toReply.context, session, toReply)
                                    .then(Mono.empty())
                    ).orElseGet(() -> Mono.just(m));
                })

                // 发送ACK确认
                .flatMap(m -> {
                    logger.debug("sending ack for mid: {}", m.getMid());
                    return new AckMsg(m).send(session).then(Mono.just(m));
                });
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
