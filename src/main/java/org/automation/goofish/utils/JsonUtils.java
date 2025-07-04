package org.automation.goofish.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
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
}
