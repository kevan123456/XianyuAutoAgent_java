package org.automation.goofish.core.socket.msg.send;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.automation.goofish.core.socket.msg.Message;

import java.util.List;

import static org.automation.goofish.utils.JsonUtils.OBJECT_MAPPER;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckRedPointMsg implements Message {
    private String lwp = "/r/Conversation/clearRedPoint";
    private CheckRedPointMsg.Headers headers = new CheckRedPointMsg.Headers();
    private List<Object> body;

    public CheckRedPointMsg(String chatId, String msgId) {
        ArrayNode array = OBJECT_MAPPER.createArrayNode();
        ObjectNode f = OBJECT_MAPPER.createObjectNode();
        f.put("cid", chatId + "@goofish");
        f.put("messageId", msgId);
        body = List.of(array.add(f));
    }

    @Data
    public static class Headers {
        private String mid = Message.generateMid();
    }
}
