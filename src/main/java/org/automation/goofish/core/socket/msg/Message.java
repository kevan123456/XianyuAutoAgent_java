package org.automation.goofish.core.socket.msg;

import lombok.SneakyThrows;
import org.automation.goofish.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.invoke.MethodHandles.lookup;

public interface Message {

    Logger logger = LoggerFactory.getLogger(lookup().lookupClass());

    @SneakyThrows
    default String toJson() {
        return JsonUtils.toJson(this);
    }

    static String generateMid() {
        int randomPart = ThreadLocalRandom.current().nextInt(1000);
        long timestamp = System.currentTimeMillis();
        return "%d%d 0".formatted(randomPart, timestamp);
    }

    static String generateUuid() {
        long timestamp = Instant.now().toEpochMilli();
        return "-" + timestamp + "1";
    }

    @SneakyThrows
    default Mono<Void> send(WebSocketSession session) {
        String json = toJson();
        logger.trace("sent ---> msg [{}] {}", hashCode(), JsonUtils.prettyJson(json));
        return session.send(Mono.just(session.textMessage(json)));
    }
}
