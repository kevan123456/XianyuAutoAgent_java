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
import org.automation.goofish.item.ItemInfo;
import org.automation.goofish.service.AutoReplyService;
import org.automation.goofish.utils.JsonUtils;
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
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.invoke.MethodHandles.lookup;
import static org.automation.goofish.utils.JsonUtils.OBJECT_MAPPER;
import static org.springframework.util.StringUtils.hasLength;

@Lazy
@Component
public class GoofishSocket implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(lookup().lookupClass());

    @Autowired
    AutoReplyService service;
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
    private Mono<Void> history(ReceiveMsg recMsg, MsgDispatcher.MsgContext context, WebSocketSession session) {
        logger.debug("start fetching chat history process");
        if (recMsg.getMid().equals(context.getMid())) {
            logger.trace("[{}] process history records within received message of mid: {}", recMsg.hashCode(), recMsg.getMid());
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
                        String mid = listMsg.getHeaders().getMid();
                        context.setMessagesQueryMid(mid);
                        dispatcher.getHistoryMsgRegistry().put(mid, context);
                        return listMsg.send(session)
                                .doOnSuccess(__ -> logger.trace("[{}] api request completed", recMsg.hashCode()))
                                .then(Mono.empty());
                    }))
                    .flatMap(chat -> Mono.fromRunnable(() -> {
                        String chatHistory = chat.getChatHistory();
                        logger.info("loaded chat history from database");
                        logger.trace("[{}] chat history loaded: {}", recMsg.hashCode(), chatHistory);

                        if (!Objects.equals(context.getReceiverId(), properties.getUserId())) {
                            logger.debug("[{}]from database branch: emitting chat history to context's sink", recMsg.hashCode());
                            context.setHistoryChat(JsonUtils.readValue(chatHistory, ObjectNode.class));
                        }
                    }));
        }

        // get chat history from response
        if (recMsg.getMid().equals(context.getMessagesQueryMid())) {
            logger.trace("[{}] process history records within received response of mid : {}", recMsg.hashCode(), context.getMessagesQueryMid());

            if (recMsg.getBody() == null || recMsg.getBody().getUserMessageModels() == null) {
                if (recMsg.getCode() != 200) {
                    logger.error("encounter error with response of list user message, raw json: {}", recMsg.getRaw());
                }
                return Mono.empty();
            }

            ArrayNode messagesArray = OBJECT_MAPPER.createArrayNode();
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
                    null,
                    context.getItemId()
            );

            logger.trace("[{}] insert history chat list json to database: {}", recMsg.hashCode(), chatHistoryJson);
            return Mono.defer(() -> {
                        logger.trace("[{}] starting db insert", recMsg.hashCode());
                        return chatRepository.insert(chatContext)
                                .doOnSuccess(saved -> {
                                    context.setHistoryChat(chatHistoryJson);
                                    logger.debug("[{}] from api branch: emitting chat history to context's sink", recMsg.hashCode());
                                })
                                .doOnError(e -> logger.error("[{}] db insert failed", recMsg.hashCode(), e));
                    })
                    .then();
        }
        return Mono.empty();
    }

    @SneakyThrows
    public Mono<ChatContext> updateChatHistory(ObjectNode historyChat, String msg, boolean seller, String chatId, String itemId) {
        // 1. 初始化或复用历史记录（不再需要类型检查）
        ObjectNode historyJson = (historyChat != null)
                ? historyChat  // 直接使用原对象
                : OBJECT_MAPPER.createObjectNode();

        // 2. 创建新消息节点
        ObjectNode newMessage = OBJECT_MAPPER.createObjectNode()
                .put("role", seller ? "seller" : "buyer")
                .put("content", msg)
                .put("timestamp", System.currentTimeMillis());

        // 3. 获取或创建消息数组（直接操作ObjectNode）
        ArrayNode messages = historyJson.has("messages")
                ? (ArrayNode) historyJson.get("messages")
                : historyJson.putArray("messages");
        messages.insert(0, newMessage);

        // 4. 保存到数据库
        return chatRepository.save(new ChatContext(
                chatId,
                historyJson.toString(),  // 直接传递修改后的ObjectNode
                null,
                itemId
        ));
    }

    ConcurrentHashMap<String, Boolean> m = new ConcurrentHashMap<>();

    private Mono<Void> reply(WebSocketSession session, MsgDispatcher.MsgContext ctx) {
        logger.trace("[{}] dump message context before reply {}", ctx.getIdentity(), ctx);

        // 拼接最新消息到历史记录
        return new ReadMsg(ctx.getMessageId()).send(session)
                .then(loadItem(ctx))
                .then(ctx.getHistoryChat())
                .flatMap(historyChat -> {
                    if (hasLength(ctx.getSendMessage())) {
                        return updateChatHistory(historyChat, ctx.getSendMessage(),
                                false, ctx.getChatId(), ctx.getItemId())
                                .flatMap(x -> {
                                    // only reply when sender in not the receiver
                                    if (!Objects.equals(properties.getUserId(), ctx.getReceiverId()) && !m.containsKey(ctx.getChatId())) {
                                        logger.info("买家: {}", ctx.getSendMessage());
                                        return botReply(session, ctx);
                                    } else {
                                        // user manually reply
                                        if (Objects.equals(ctx.getSendMessage(), "[微笑]")) {
                                            // return to auto mode
                                            logger.info("received emoji [微笑], exit manual mode for chat {}", ctx.getChatId());
                                            m.remove(ctx.getChatId());
                                        } else {
                                            // switch to manually mode
                                            logger.info("detected user send message from other device, suspend for the chat {}", ctx.getChatId());
                                            m.put(ctx.getChatId(), true);
                                        }
                                        return Mono.just(ctx.getSendMessage());
                                    }
                                }).flatMap(m -> {
                                    logger.info("商家: {}", m);
                                    logger.debug("update seller's message to database in chat history table");
                                    return ctx.getHistoryChat().flatMap(hc ->
                                            updateChatHistory(hc, m, true, ctx.getChatId(), ctx.getItemId())).then();
                                });
                    }
                    return Mono.empty();
                });
    }

    // 将商品信息加载到上下文中
    private Mono<ItemContext> loadItem(MsgDispatcher.MsgContext msgContext) {
        final String itemId = msgContext.getItemId();
        // fast return
        ItemContext cache = dispatcher.getItemContextRegistry().get(itemId);
        if (cache != null) return Mono.just(cache);
        logger.debug("starting to load item: {}", itemId);

        return itemRepository.findById(itemId)
                .switchIfEmpty(
                        client.getItemInfo(itemId)
                                .flatMap(itemJson -> {
                                    String itemInfo = JsonUtils.toJson(JsonUtils.readValue(itemJson.toString(), ItemInfo.class));
                                    logger.debug("api fallback success for item: {}", itemInfo);
                                    ItemContext itemContext = new ItemContext(itemId, itemInfo);

                                    // 确保插入操作会被执行
                                    return itemRepository.insert(itemContext)
                                            .doOnSuccess(saved -> {
                                                logger.debug("insert item {} to database successfully", itemId);
                                                dispatcher.getItemContextRegistry().put(itemId, itemContext);
                                                msgContext.setItemInfo(itemInfo); // 在这里设置上下文
                                            })
                                            .thenReturn(itemContext); // 使用 thenReturn 而不是 Mono.just
                                })
                )
                .flatMap(itemCtx -> {
                    if (itemCtx != null && itemCtx.getItemInfo() != null) {
                        logger.debug("found valid item info in db: {}", itemCtx.getItemInfo());
                        dispatcher.getItemContextRegistry().put(itemId, itemCtx);
                        msgContext.setItemInfo(itemCtx.getItemInfo());
                        return Mono.just(itemCtx);
                    }
                    return Mono.empty();
                });
    }

    // 使用上下文处理回复
    private Mono<String> botReply(WebSocketSession session, MsgDispatcher.MsgContext msgContext) {
        return msgContext.getHistoryChat()
                .flatMap(chatHistory -> {
                    // 2. 构建情感提示词（流式处理）
                    String emotionPrompt = """
                            请基于以下模板
                            %s
                            处理json格式的message_history 请按时间倒序处理附件的chat_history
                            %s
                            """.formatted(service.emotionPrompt, chatHistory.toString());

                    // 3. 生成回复（链式调用）
                    return replyService.generateReply(emotionPrompt)
                            .map(em -> """
                                    基于以下上下文生成你的回复
                                    聊天上下文: %s
                                    商品信息: %s
                                    """.formatted(em, msgContext.getItemInfo()))
                            .flatMap(replyService::generateReply)
                            .flatMap(botMsg ->
                                    new ReplyMsg(
                                            msgContext.getChatId(),
                                            msgContext.getReceiverId(),
                                            properties.getUserId(),
                                            botMsg
                                    ).send(session)
                                            .thenReturn(botMsg)
                            );
                })
                .onErrorResume(e -> {
                    logger.error("Process reply failed", e);
                    return Mono.empty();
                });
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
                // 发送ACK确认, 放在处理推送包前 因为推送包处理时间长的话会触发server resend package
                .flatMap(m -> {
                    logger.trace("sending ack for mid: {}", m.getMid());
                    return new AckMsg(m).send(session).thenReturn(m);
                })
                // 3. 处理同步推送包（核心逻辑）
                .flatMap(m -> {

                    if (!m.hasSyncPushPackageMessage()) {
                        return Mono.just(m);
                    }

                    // 只处理第一个syncData对象（或根据需要修改）
                    ReceiveMsg.SyncData data = m.getSyncData().getFirst();
                    return switch (data.getObjectType()) {
                        case 40000 -> dispatcher.handle(m, session)
                                .flatMap(context -> {
                                    if (!hasLength(context.getChatId())) {
                                        return Mono.just(m);
                                    }
                                    // send api request to get history
                                    return history(m, context, session)
                                            .then(Mono.defer(() -> reply(session, context)))
                                            .thenReturn(m);
                                });
                        case 40103 -> {
                            logger.debug("processing message with objectType 40103, mid: {}", m.getMid());
                            yield Mono.just(m);
                        }
                        // 消息已读
                        case 40102 -> {
                            logger.debug("processing message with objectType 40102, mid: {}", m.getMid());
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
                    MsgDispatcher.MsgContext context = dispatcher.getHistoryMsgRegistry().remove(m.getMid());
                    if (context != null) {
                        return history(m, context, session)
                                .doOnSuccess(__ -> logger.trace("[{}] history process completed", m.getMid()))
                                .thenReturn(m);
                    }
                    return Mono.just(m);
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
