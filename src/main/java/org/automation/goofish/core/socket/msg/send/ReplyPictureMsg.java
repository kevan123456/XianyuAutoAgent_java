package org.automation.goofish.core.socket.msg.send;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.automation.goofish.core.socket.msg.Message;
import org.automation.goofish.utils.JsonUtils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReplyPictureMsg implements Message {

    private String lwp = "/r/MessageSend/sendByReceiverScope";
    private Headers headers = new Headers();
    private List<Object> body = new ArrayList<>();

    // constructor
    public ReplyPictureMsg(String chatId, String receiverId, String senderId, JsonNode picAddress) {

        MessageItem messageItem = new MessageItem();
        messageItem.cid = "%s@goofish".formatted(chatId);

        @Data
        @AllArgsConstructor
        class MessageContent {
            private int contentType;
            private ImageData image;

            @Data
            @AllArgsConstructor
            static class ImageData {
                private List<PicInfo> pics;

                @Data
                @AllArgsConstructor
                static class PicInfo {
                    private int height;
                    private int type;
                    private String url;
                    private int width;
                }
            }
        }

        List<MessageContent.ImageData.PicInfo> pics = new ArrayList<>();
        // width * height
        String pix = picAddress.path("pix").asText();
        // 2547x953
        Pattern pattern = Pattern.compile("(?<width>\\d+)x(?<height>\\d+)");
        Matcher matcher = pattern.matcher(pix);

        if (matcher.find()) {
            String width = matcher.group("width");
            String height = matcher.group("height");
            pics.add(new MessageContent.ImageData.PicInfo(Integer.parseInt(height), 0,
                    picAddress.path("url").asText(),
                    Integer.parseInt(width)));

            MessageContent content = new MessageContent(2, new MessageContent.ImageData(pics));

            messageItem.getContent().getCustom().setData(Base64.getEncoder().encodeToString(JsonUtils.toJson(content).getBytes()));
            body.add(messageItem);

            ReceiverItem receiverItem = new ReceiverItem();
            receiverItem.getActualReceivers().add(receiverId + "@goofish");
            receiverItem.getActualReceivers().add(senderId + "@goofish");
            body.add(receiverItem);
        }
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
        private int type = 2;
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
