package org.automation.goofish.core.socket.msg.send;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.automation.goofish.core.socket.msg.Message;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReplyMsg implements Message {

    private String lwp = "/r/MessageSend/sendByReceiverScope";
    private Headers headers = new Headers();
    private List<Object> body = new ArrayList<>();

    // constructor
    public ReplyMsg(String chatId, String receiverId, String senderId, String reply) {

        MessageItem messageItem = new MessageItem();
        messageItem.cid = "%s@goofish".formatted(chatId);
        String text = """
                {"contentType":1,"text":{"text":"%s"}}
                """.trim().formatted(reply);
        messageItem.getContent().getCustom().setData(Base64.getEncoder().encodeToString(text.getBytes()));
        body.add(messageItem);

        ReceiverItem receiverItem = new ReceiverItem();
        receiverItem.getActualReceivers().add(receiverId + "@goofish");
        receiverItem.getActualReceivers().add(senderId + "@goofish");
        body.add(receiverItem);
    }

    @Data
    public static class Headers {
        private String mid = Message.generateMid();
    }

    @Data
    public static class MessageItem {
        private String uuid = Message.generateUuid();
        private String cid;
        private int conversationType = 1;
        private Content content = new Content();
        private int redPointPolicy = 0;
        private Extension extension = new Extension();
        private Context ctx = new Context();
        private Object mtags = new Object(); //empty object
        private int msgReadStatusSetting = 1;
    }

    @Data
    public static class Content {
        private int contentType = 101;
        private Custom custom = new Custom();
    }

    @Data
    public static class Custom {
        private int type = 1;
        private String data;
    }

    @Data
    public static class Extension {
        private Object extJson = new Object(); //empty object
    }

    @Data
    public static class Context {
        private String appVersion = "1.0";
        private String platform = "web";
    }

    @Data
    public static class ReceiverItem {
        private List<String> actualReceivers = new ArrayList<>();
    }
}
