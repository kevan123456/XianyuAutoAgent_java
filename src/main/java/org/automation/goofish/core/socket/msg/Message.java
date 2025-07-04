package org.automation.goofish.core.socket.msg;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.invoke.MethodHandles.lookup;
import static org.automation.goofish.utils.JsonUtils.OBJECT_MAPPER;

public interface Message {

    Logger logger = LoggerFactory.getLogger(lookup().lookupClass());

    @SneakyThrows
    default String toJson() {
        return OBJECT_MAPPER.writeValueAsString(this);
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
        return session.send(Mono.just(session.textMessage(toJson())))
                .doOnSuccess(v -> logger.info("sent ---> msg {}", prettyJson(toJson())))
                .doOnError(e -> logger.error("error {} occur while sending msg: {}", e.getMessage(), prettyJson(toJson())));
    }

    @SneakyThrows
    static String prettyJson(String json) {
        return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(OBJECT_MAPPER.readValue(json, Object.class));
    }
}
