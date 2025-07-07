package org.automation.goofish.core.socket.msg.send;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.automation.goofish.core.socket.msg.Message;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetStatMsg implements Message {
    private String lwp = "/r/SyncStatus/getState";
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
        private String topic = "sync";
    }
}
