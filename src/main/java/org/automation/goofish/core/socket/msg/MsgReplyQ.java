package org.automation.goofish.core.socket.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

@Component
public class MsgReplyQ {

    @Data
    @AllArgsConstructor
    public static class toReply {
        String mid;
        String chatId;
        String receiverId;
    }

    private final LinkedList<toReply> q = new LinkedList<>();

    public void push(String mid, String chatId, String receiverId) {
        q.push(new toReply(mid, chatId, receiverId));
    }

    public Optional<toReply> pop(String mid) {
        Optional<toReply> result = search(mid);
        result.ifPresent(q::remove);
        return result;
    }

    public Optional<toReply> search(String mid) {
        return q.stream().filter(r -> Objects.equals(r.mid, mid)).findFirst();
    }
}
