package org.automation.goofish;

import com.fasterxml.jackson.databind.JsonNode;
import org.automation.goofish.core.GoofishClient;
import org.automation.goofish.core.socket.msg.Message;
import org.automation.goofish.core.socket.msg.receive.ReceiveMsg;
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
import java.util.Base64;

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

    @Test
    void decode(){
        String msg = """
                {
                  "headers" : {
                    "mid" : "bdcd0006 0",
                    "ua" : "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36 DingTalk(2.1.5) OS(Windows/10) Browser(Chrome/133.0.0.0) DingWeb/2.1.5 IMPaaS DingWeb/2.1.5",
                    "sid" : "212ccd186871244207fe393ac71a4c58043ddec8568c",
                    "app-key" : "444e9908a51d1cb236a27862abc769c9"
                  },
                  "lwp" : "/s/vulcan",
                  "body" : {
                    "syncPushPackage" : {
                      "data" : [ {
                        "bizType" : 370,
                        "data" : "eyJjaGF0VHlwZSI6MSwiaW5jcmVtZW50VHlwZSI6MSwib3BlcmF0aW9uIjp7ImNvbnRlbnQiOnsiY29udGVudFR5cGUiOjgsInNlc3Npb25Bcm91c2UiOnsibWVtYmVyRmxhZ3MiOjAsIm5lZWRLZXlCb2FyZCI6dHJ1ZSwic2Vzc2lvbkFyb3VzZUluZm8iOnsicmVzaWRlbnRGdW5jdGlvbnMiOltdLCJhcm91c2VUaW1lU3RhbXAiOjE3NTIyNDUzNzM2ODYsImFyb3VzZUNoYXRTY3JpcHRJbmZvIjpbeyJjaGF0U2NyaXAiOiLov5nmmK/mrabol6TmuLjmiI/nu53niYjljaHlpZciLCJjaGF0U2NyaXBTdHJhdGVneSI6InNoYWRpbmdfY2hhdCIsImFyZ0luZm8iOnsiYXJncyI6eyJ0b3BpY190aXRsZSI6InNoYWRpbmdfY2hhdCIsInNlc3Npb25faWQiOjUxNTMxNTAyMjQ0LCJjb250ZW50Ijoi6L+Z5piv5q2m6Jek5ri45oiP57ud54mI5Y2h5aWXIn0sImFyZzEiOiJDaGF0VG9waWMifX1dLCJjaGF0R3VpZGFuY2UiOnsidHh0Ijoi5bCP6Zey6bG854yc5L2g5oOz6K+0IiwiY2hhdEd1aWRhbmNlSWNvbiI6Imh0dHBzOi8vZy5hbGljZG4uY29tL2V2YS1hc3NldHMvOTAwYjgwODk2YjBhNGEzMjc0Yzg5OTBkYzc2YTM5MDYvMC4wLjEvdG1wL2M4MzljZmQvYzgzOWNmZC5qc29uIiwiaWNvbiI6Imh0dHBzOi8vZy5hbGljZG4uY29tL2V2YS1hc3NldHMvYWYyOTA2YjM0ODYwYzFmM2Y1YjFlNWU1YzY3MWE5NjYvMC4wLjEvdG1wL2EzZjgyMzIvYTNmODIzMi5qc29uP3NwbT1hMjE3MTQuaG9tZXBhZ2UuMC4wLjRlMGEzZmUwamxPZVl5JmZpbGU9YTNmODIzMi5qc29uIn19fX0sInJlY2VpdmVySWRzIjpbIjIxNTAzNjk4MTkiXSwic2Vzc2lvbkluZm8iOnsiY3JlYXRlVGltZSI6MTc1MjA3ODA2MTAwMCwiZXh0ZW5zaW9ucyI6eyJleHRVc2VyVHlwZSI6IjAiLCJpdGVtVGl0bGUiOiIj5a6Y5pa55Y2h5aWXICMxNeWRqOW5tCAj5q2m6Jek5ri45oiPICPmoJflrZDnkIMgI+e7neeJiOWNoeWllyIsInNxdWFkSWRfMjE1MDM2OTgxOSI6Ijc4ODE3NTQ3OTQ3MSIsImV4dFVzZXJJZCI6IjQwMDg2MzkwNzIiLCJzcXVhZElkXzQwMDg2MzkwNzIiOiIxMjY4NjAzNzUiLCJzb3VyY2UiOiIiLCJpdGVtTWFpblBpYyI6Imh0dHBzOi8vaW1nLmFsaWNkbi5jb20vYmFvL3VwbG9hZGVkL2kyL08xQ04wMWJUMGRpMjJNUDl2M01XUWxBXyEhMC1mbGVhbWFya2V0LmpwZyIsIm93bmVyVXNlclR5cGUiOiIwIiwiaXRlbUlkIjoiNzg4MTc1NDc5NDcxIiwiaXRlbUZlYXR1cmVzIjoie1wiaWRsZV9jYXRfbGVhZlwiOlwiMTI2ODYwMzc1XCJ9IiwiaXRlbVNlbGxlcklkIjoiMjE1MDM2OTgxOSIsIm93bmVyVXNlcklkIjoiMjE1MDM2OTgxOSIsInNxdWFkTmFtZV8yMTUwMzY5ODE5IjoiI+WumOaWueWNoeWllyAjMTXlkajlubQgI+atpuiXpOa4uOaIjyAj5qCX5a2Q55CDICPnu53niYjljaHlpZciLCJzcXVhZE5hbWVfNDAwODYzOTA3MiI6IuWNoeeJjCJ9LCJncm91cE93bmVySWQiOiIyMTUwMzY5ODE5Iiwic2Vzc2lvbklkIjoiNTE1MzE1MDIyNDQiLCJzZXNzaW9uVHlwZSI6MSwidHlwZSI6MX19LCJzZXNzaW9uSWQiOiI1MTUzMTUwMjI0NCJ9",
                        "objectType" : 370000
                      } ]
                    },
                    "syncExtraType" : {
                      "type" : 3
                    }
                  },
                  "code" : 0
                }
                """;
        ReceiveMsg parsed = ReceiveMsg.parse(msg);
        logger.info(parsed.toString());
    }
}
