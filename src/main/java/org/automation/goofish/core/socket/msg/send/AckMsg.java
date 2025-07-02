package org.automation.goofish.core.socket.msg.send;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.automation.goofish.core.socket.msg.Message;
import org.automation.goofish.core.socket.msg.receive.ReceiveMsg;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AckMsg implements Message {
    private Headers headers = new Headers();
    private int code = 200;

    public AckMsg(String appKey, String sid, String ua, String dt, String mid) {
        headers.setAppKey(appKey);
        headers.setSid(sid);
        headers.setUa(ua);
        headers.setDt(dt);
        headers.setMid(mid);
    }

    public AckMsg(ReceiveMsg reqMsg) {
        this(reqMsg.getAppKey(),
                reqMsg.getSid(), reqMsg.getUa(),
                reqMsg.getDt(), reqMsg.getMid());
    }

    @Data
    public static class Headers {
        @JsonProperty("app-key")
        private String appKey;
        private String sid;
        private String ua;
        private String dt;
        private String mid;
    }
}
