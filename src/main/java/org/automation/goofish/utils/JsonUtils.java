package org.automation.goofish.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

public class JsonUtils {

    public static ObjectMapper OBJECT_MAPPER = createConfiguredObjectMapper();

    static ObjectMapper createConfiguredObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return mapper;
    }

    @SneakyThrows
    public static <T> T readValue(String content, Class<T> valueType) {
        return OBJECT_MAPPER.readValue(content, valueType);
    }

    @SneakyThrows
    public static String toJson(Object value) {
        return OBJECT_MAPPER.writeValueAsString(value);
    }

    @SneakyThrows
    public static String prettyJson(String json) {
        return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(OBJECT_MAPPER.readValue(json, Object.class));
    }

    @SneakyThrows
    public static JsonNode readTree(String json) {
        return OBJECT_MAPPER.readTree(json);
    }
}
