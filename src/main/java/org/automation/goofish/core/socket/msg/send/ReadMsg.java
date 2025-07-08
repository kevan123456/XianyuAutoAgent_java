package org.automation.goofish.core.socket.msg.send;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Data;
import org.automation.goofish.core.socket.msg.Message;

import java.util.List;

import static org.automation.goofish.utils.JsonUtils.OBJECT_MAPPER;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReadMsg implements Message {
    private String lwp = "/r/MessageStatus/read";
    private ReadMsg.Headers headers = new ReadMsg.Headers();
    private List<Object> body;

    public ReadMsg(String msgId) {
        ArrayNode array = OBJECT_MAPPER.createArrayNode();
        body = List.of(array.add(msgId));
    }

    @Data
    public static class Headers {
        private String mid = Message.generateMid();
    }
}
