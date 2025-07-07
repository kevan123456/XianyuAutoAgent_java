package org.automation.goofish.core.socket.msg.send;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.automation.goofish.core.socket.msg.Message;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HeartbeatMsg implements Message {
    private String lwp = "/!";
    private Headers headers = new Headers();

    @Data
    public static class Headers {
        private String mid = Message.generateMid();
    }
}
