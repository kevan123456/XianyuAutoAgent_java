package org.automation.goofish;

import com.fasterxml.jackson.databind.JsonNode;
import org.automation.goofish.core.GoofishClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static java.lang.invoke.MethodHandles.lookup;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(classes = Starter.class)
public class TestClient {

    private static final Logger logger = LoggerFactory.getLogger(lookup().lookupClass());

    @Autowired
    GoofishClient client;

    @Test
    void testUploadApi() {
        Path testFilePath = Path.of("screenshots/workflow.png");
        assertTrue(Files.exists(testFilePath), "Test file does not exist: " + testFilePath);

        ResponseEntity<JsonNode> response = client.upload(testFilePath)
                .blockOptional(Duration.ofSeconds(10))  // 添加超时
                .orElseThrow(() -> new AssertionError("Upload timed out or failed"));

        assertNotNull(response, "Response should not be null");

        JsonNode responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");

        logger.info("Upload response: {}", responseBody.toPrettyString());

        assertTrue(responseBody.has("success"), "Response should contain 'success' field");
        assertEquals(200, response.getStatusCode().value(), "HTTP status should be 200");
    }
}
