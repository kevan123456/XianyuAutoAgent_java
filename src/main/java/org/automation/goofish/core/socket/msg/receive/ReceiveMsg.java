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

import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

import static org.automation.goofish.utils.JsonUtils.OBJECT_MAPPER;

@Data
public class ReceiveMsg implements Message {
    private Headers headers;
    private String lwp;
    private Body body;
    private int code;
    private LinkedList<String> mq = new LinkedList<>();

    @Data
    public static class Headers {
        @JsonProperty("app-key")
        private String appKey;
        private String mid;
        private String ua;
        private String sid;
        private String dt;
        @JsonProperty("ip-digest")
        private String ipDigest;
        // for ip-digest
        @JsonProperty("reg-sid")
        private String regSid;
        // for ip-digest
        @JsonProperty("ip-region-digest")
        private String ipRegionDigest;
        // for ip-digest
        @JsonProperty("reg-uid")
        private String regUid;
        // for ip-digest
        @JsonProperty("real-ip")
        private String realIp;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private SyncPushPackage syncPushPackage;
        private SyncExtensionModel syncExtensionModel;
        private syncExtraType syncExtraType;
        private Extension extension;
        // for ip-digest
        private String unitName;
        private String cookie;
        private long timestamp;
        // for ip-digest
        @JsonProperty("isFromChina")
        private boolean fromChina;
        private String pipeline;
        private String tooLong2Tag;
        private String topic;
        private String channel;
        private String highPts;
        private String pts;
        private String seq;
        private int receiverCount;
        private int unreadCount;
        private int msgReadStatusSetting;
        private int msgReadStatusDowngrade;
        private String messageId;
        private String uuid;
        private long createAt;
        private Object content;
        // for failed message reply
        private String reason;
        // for failed message reply
        private String code;
        // for failed message reply
        private String developerMessage;
        // for failed message reply
        private String scope;
        // for historical user message
        private long nextCursor;
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
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Extension {
        @JsonProperty("_platform")
        private String platform = "web";
        private String reminderContent;
        private String reminderNotice;
        private String senderUserType;
        private String detailNotice;
        private String senderUserId;
        private Object extJson;
        private String sessionType;
        private String reminderUrl;
        private String bizTag;
        private String reminderTitle;
    }

    @Data
    public static class SyncPushPackage {
        private long maxHighPts;
        private long startSeq;
        private long endSeq;
        private long minCreateTime;
        private List<SyncData> data;
        private long maxPts;
        private int hasMore;
        private long timestamp;
    }

    @Data
    public static class SyncData {
        private int bizType;
        private String data;
        @JsonIgnore
        private String content;
        private String streamId;
        private int objectType;
        private String syncId;
    }

    @Data
    public static class SyncExtensionModel {
        private int reconnectType;
        private int failover;
        private int fingerprint;
    }

    @Data
    public static class syncExtraType {
        private int type;
    }

    public boolean hasSyncPushPackageMessage() {
        return (body != null && body.syncPushPackage != null && body.syncPushPackage.data != null && !body.syncPushPackage.data.isEmpty()) &&
                body.syncPushPackage.data.stream().anyMatch(x -> StringUtils.hasLength(x.data));
    }

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

    public void setMessageContent(int idx, String value) {
        SyncData data = body.syncPushPackage.data.get(idx);
        data.setContent(value);
    }

    public String getMessage() {
        return getMessage(0);
    }

    public void setMessageContent(String value) {
        setMessageContent(0, value);
    }

    @SneakyThrows
    public static ReceiveMsg parse(String json) {
        ReceiveMsg recMsg = OBJECT_MAPPER.readValue(json, ReceiveMsg.class);

        if (recMsg.hasSyncPushPackageMessage()) {
            List<SyncData> dataList = recMsg.getBody().getSyncPushPackage().data;
            for (int i = 0; i < dataList.size(); i++) {
                String content = recMsg.getMessage(i);
                if (StringUtils.hasLength(content)) {
                    recMsg.setMessageContent(i, content);
                    recMsg.getMq().addLast(content);
                    logger.info("unpack <---> message: {}", Message.prettyJson(content));
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

    public boolean shouldReply() {
        return hasMid() && hasAppKey();
    }

    public boolean hasMid() {
        return headers != null && StringUtils.hasLength(headers.mid);
    }

    public String getMid() {
        return hasMid() ? headers.mid : "";
    }

    public boolean hasSid() {
        return headers != null && StringUtils.hasLength(headers.sid);
    }

    public String getSid() {
        return hasSid() ? headers.sid : "";
    }

    public boolean hasAppKey() {
        return headers != null && StringUtils.hasLength(headers.appKey);
    }

    public String getAppKey() {
        return hasAppKey() ? headers.appKey : "";
    }

    public boolean hasUa() {
        return headers != null && StringUtils.hasLength(headers.ua);
    }

    public String getUa() {
        return hasUa() ? headers.ua : "";
    }

    public boolean hasDt() {
        return headers != null && StringUtils.hasLength(headers.dt);
    }

    public String getDt() {
        return hasDt() ? headers.dt : "";
    }

    public boolean hasIpDigest() {
        return headers != null && StringUtils.hasLength(headers.ipDigest);
    }

    public boolean hasSyncExtraType() {
        return body != null && body.syncExtraType != null;
    }

    public int getSyncExtraType() {
        return hasSyncExtraType() ? body.syncExtraType.type : -1;
    }

    public boolean reqSync() {
        return "/s/sync".equals(lwp);
    }

    public boolean reqVulcan() {
        return "/s/vulcan".equals(lwp);
    }

    public boolean hasTooLong2Tag() {
        return body != null && StringUtils.hasLength(body.tooLong2Tag);
    }
}
