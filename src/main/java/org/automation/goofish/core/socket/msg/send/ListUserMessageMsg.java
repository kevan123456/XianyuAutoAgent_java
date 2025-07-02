package org.automation.goofish.core.socket.msg.send;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.automation.goofish.core.socket.msg.Message;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListUserMessageMsg implements Message {
    private String lwp = "/r/MessageManager/listUserMessages";
    private Headers headers = new Headers();
    private List<Object> body;

    public ListUserMessageMsg(String chatId, int messageAmount) {
        body = List.of(chatId + "@goofish", false, System.currentTimeMillis(), messageAmount, false);
    }

    @Data
    public static class Headers {
        private String mid = Message.generateMid();
    }
}