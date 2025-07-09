package org.automation.goofish.core.socket.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

@Component
public class MsgReplyQ {

    @Data
    @ToString
    @AllArgsConstructor
    public static class ToReply {
        String mid;
        String chatId;
        String receiverId;
        String itemId;
    }

    private final LinkedList<ToReply> q = new LinkedList<>();

    public void push(String mid, String chatId, String receiverId, String itemId) {
        q.push(new ToReply(mid, chatId, receiverId, itemId));
    }

    public Optional<ToReply> pop(String mid) {
        Optional<ToReply> result = search(mid);
        result.ifPresent(q::remove);
        return result;
    }

    public Optional<ToReply> search(String mid) {
        return q.stream().filter(r -> Objects.equals(r.mid, mid)).findFirst();
    }
}
