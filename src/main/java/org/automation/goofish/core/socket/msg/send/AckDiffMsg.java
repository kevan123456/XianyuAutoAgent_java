package org.automation.goofish.core.socket.msg.send;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.automation.goofish.core.socket.msg.Message;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AckDiffMsg implements Message {
    private String lwp = "/r/SyncStatus/ackDiff";
    private Headers headers = new Headers();
    private List<BodyItem> body = new ArrayList<>();

    {
        body.add(new BodyItem());
    }

    @Data
    public static class Headers {
        private String mid = Message.generateMid();
    }

    @Data
    public static class BodyItem {
        private String pipeline = "sync";
        private String tooLong2Tag = "PNM,1";
        private String channel = "sync";
        private String topic = "sync";
        private long highPts = 0;
        private long pts = System.currentTimeMillis() * 1000;
        private long seq = 0;
        private long timestamp = System.currentTimeMillis();
    }
}
