package org.automation.goofish.core.socket.msg.receive;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import org.automation.goofish.core.socket.msg.Message;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

import static org.automation.goofish.utils.JsonUtils.OBJECT_MAPPER;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReceiveMsg implements Message {
    private Headers headers;
    private String lwp;
    private Body body;
    private int code;
    @JsonIgnore
    private Object raw;
    @JsonIgnore
    private LinkedList<String> mq = new LinkedList<>();

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Headers {
        @JsonProperty("app-key")
        private String appKey;
        private String mid;
        private String ua;
        private String sid;
        private String dt;
        @JsonProperty("ip-digest")
        private String ipDigest;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private SyncPushPackage syncPushPackage;
        private syncExtraType syncExtraType;
        private Extension extension;
        private String tooLong2Tag;
        private List<UserMessageModel> userMessageModels;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserMessageModel {
        private HistoryChatMessage message;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HistoryChatMessage {
        private Extension extension;
        private long createAt;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Extension {
        private String reminderContent;
        private String reminderNotice;
        private String senderUserType;
        private String senderUserId;
        private String sessionType;
        private String reminderUrl;
        private String reminderTitle;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SyncPushPackage {
        private List<SyncData> data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SyncData {
        private int bizType;
        private String data;
        @JsonIgnore
        private String content;
        private int objectType;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class syncExtraType {
        private int type;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public boolean hasSyncPushPackageMessage() {
        return (body != null && body.syncPushPackage != null && body.syncPushPackage.data != null && !body.syncPushPackage.data.isEmpty()) &&
                body.syncPushPackage.data.stream().anyMatch(x -> StringUtils.hasLength(x.data));
    }

    @JsonIgnore
    public String getMessage(int idx) {
        if (body == null || body.syncPushPackage == null || body.syncPushPackage.data == null) {
            return "";
        }
        SyncData data = body.syncPushPackage.data.get(idx);
        return switch (data.getBizType()) {
            case 40 -> msgUnpack(data.data);
            case 50 -> "noop";
            default -> base64Unpack(data.data);
        };
    }

    @JsonIgnore
    public void setMessageContent(int idx, String value) {
        SyncData data = body.syncPushPackage.data.get(idx);
        data.setContent(value);
    }

    @JsonIgnore
    public String getMessage() {
        return getMessage(0);
    }

    @SneakyThrows
    public static ReceiveMsg parse(String json) {
        ReceiveMsg recMsg = OBJECT_MAPPER.readValue(json, ReceiveMsg.class);
        recMsg.setRaw(json);
        if (recMsg.hasSyncPushPackageMessage()) {
            List<SyncData> dataList = recMsg.getBody().getSyncPushPackage().data;
            for (int i = 0; i < dataList.size(); i++) {
                String content = recMsg.getMessage(i);
                if (StringUtils.hasLength(content)) {
                    recMsg.setMessageContent(i, content);
                    recMsg.getMq().addLast(content);
                }
            }
        }
        return recMsg;
    }

    @SneakyThrows
    public String msgUnpack(String base64Data) {
        byte[] msgpackBytes = Base64.getDecoder().decode(base64Data);
        ObjectMapper msgpackMapper = new ObjectMapper(new MessagePackFactory());
        return msgpackMapper.readTree(msgpackBytes).toString();
    }

    public String base64Unpack(String base64Data) {
        byte[] msgpackBytes = Base64.getDecoder().decode(base64Data);
        return new String(msgpackBytes);
    }

    public boolean hasMid() {
        return headers != null && StringUtils.hasLength(headers.mid);
    }

    @JsonIgnore
    public String getMid() {
        return hasMid() ? headers.mid : "";
    }

    public boolean hasSid() {
        return headers != null && StringUtils.hasLength(headers.sid);
    }

    @JsonIgnore
    public String getSid() {
        return hasSid() ? headers.sid : "";
    }

    public boolean hasAppKey() {
        return headers != null && StringUtils.hasLength(headers.appKey);
    }

    @JsonIgnore
    public String getAppKey() {
        return hasAppKey() ? headers.appKey : "";
    }

    public boolean hasUa() {
        return headers != null && StringUtils.hasLength(headers.ua);
    }

    @JsonIgnore
    public String getUa() {
        return hasUa() ? headers.ua : "";
    }

    public boolean hasDt() {
        return headers != null && StringUtils.hasLength(headers.dt);
    }

    @JsonIgnore
    public String getDt() {
        return hasDt() ? headers.dt : "";
    }

    public boolean hasIpDigest() {
        return headers != null && StringUtils.hasLength(headers.ipDigest);
    }

    public boolean hasTooLong2Tag() {
        return body != null && StringUtils.hasLength(body.tooLong2Tag);
    }

    @JsonIgnore
    public List<SyncData> getSyncData() {
        return body != null && body.syncPushPackage != null && body.syncPushPackage.data != null ? body.syncPushPackage.data : new ArrayList<>();
    }
}
