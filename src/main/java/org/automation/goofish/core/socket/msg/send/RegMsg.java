package org.automation.goofish.core.socket.msg.send;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.automation.goofish.core.socket.msg.Message;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegMsg implements Message {
    private String lwp = "/reg";
    private Headers headers = new Headers();

    public RegMsg(String deviceId, String token) {
        headers.setDid(deviceId);
        headers.setToken(token);
        headers.setMid(Message.generateMid());
    }

    @Data
    public static class Headers {
        @JsonProperty("cache-header")
        private String cacheHeader = "app-key token ua wv";

        @JsonProperty("app-key")
        private String appKey = "444e9908a51d1cb236a27862abc769c9";

        private String token;
        private String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36 DingTalk(2.1.5) OS(Windows/10) Browser(Chrome/133.0.0.0) DingWeb/2.1.5 IMPaaS DingWeb/2.1.5";
        private String dt = "j";
        private String wv = "im:3,au:3,sy:6";
        private String sync = "0,0;0;0;";
        private String did;
        private String mid;
    }
}
