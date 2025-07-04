package org.automation.goofish.core.socket.msg;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.automation.goofish.core.ConnectionProperties;
import org.automation.goofish.core.socket.msg.receive.ReceiveMsg;
import org.automation.goofish.core.socket.msg.send.ListUserMessageMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.invoke.MethodHandles.lookup;
import static org.automation.goofish.utils.JsonUtils.OBJECT_MAPPER;
import static org.springframework.util.StringUtils.hasLength;

@Component
public class MsgDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(lookup().lookupClass());

    @Value("${goofish.historical-data-maximum}")
    int historicalDataMaximum = 20;

    @Autowired
    ConnectionProperties properties;

    @Data
    @ToString
    public static class MsgContext {
        String redReminder;
        String userId;
        String userUrl;
        String createTime;
        String sendUserName;
        String receiverId;
        String sendMessage;
        String urlInfo;
        String itemId;
        String chatId;
        String messagesQueryMid; // used for filter response return listed history chat
    }

    @SneakyThrows
    public Mono<MsgContext> handle(ReceiveMsg receiveMsg, WebSocketSession session) {
        MsgContext msgContext = new MsgContext();

        // 1. 同步解析消息（无法避免，但后续操作转为响应式）
        while (!receiveMsg.getMq().isEmpty()) {
            String msg = receiveMsg.getMq().removeFirst();
            JsonNode node = OBJECT_MAPPER.readTree(msg);
            msgContext.redReminder = node.path("3").path("redReminder").asText();
            msgContext.userId = StringUtils.substringBefore(node.path("1").asText(), "@");
            msgContext.userUrl = "https://www.goofish.com/personal?userId=" + msgContext.userId;

            switch (msgContext.redReminder) {
                case "等待买家付款" -> logger.info("等待买家 {} 付款", msgContext.userUrl);
                case "交易关闭" -> logger.info("买家 {} 交易关闭", msgContext.userUrl);
                case "等待卖家发货" -> logger.info("交易成功 {} 等待卖家发货", msgContext.userUrl);
                default -> { /* noop */ }
            }

            msgContext.createTime = node.path("1").path("5").asText();
            msgContext.sendUserName = node.path("1").path("10").path("reminderTitle").asText();
            msgContext.receiverId = node.path("1").path("10").path("senderUserId").asText();
            msgContext.sendMessage = node.path("1").path("10").path("reminderContent").asText();

            // 解析 itemId 和 chatId
            msgContext.urlInfo = node.path("1").path("10").path("reminderUrl").asText();
            if (hasLength(msgContext.urlInfo)) {
                Matcher matcher = Pattern.compile("(?:^|[&?])itemId=(?<itemId>[^&]*)").matcher(msgContext.urlInfo);
                if (matcher.find()) {
                    msgContext.itemId = matcher.group("itemId");
                }
            }
            msgContext.chatId = StringUtils.substringBefore(node.path("1").path("2").asText(), "@");
        }

        // 2. 条件检查并返回 Mono<MsgContext>
        logger.info("dump chat context: {}", msgContext);

        if (hasLength(msgContext.chatId) &&
                hasLength(msgContext.receiverId) &&
                !Objects.equals(msgContext.receiverId, properties.getUserId())) {

            // 发送消息并返回 msgContext（不等待发送完成）
            ListUserMessageMsg listUserMessageMsg = new ListUserMessageMsg(msgContext.chatId, historicalDataMaximum);
            msgContext.messagesQueryMid = listUserMessageMsg.getHeaders().getMid();

            return Mono.fromRunnable(() ->
                    listUserMessageMsg.send(session).subscribe()  // 异步发送
            ).thenReturn(msgContext);  // 忽略 send 的结果，直接返回 msgContext
        } else {
            // 无需发送消息，直接返回
            return Mono.just(msgContext);
        }
    }
}
