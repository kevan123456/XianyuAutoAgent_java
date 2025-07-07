package org.automation.goofish.interceptor;

import org.automation.goofish.Starter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = Starter.class)
public class TestAI {

    @Autowired
    AutoReplyService service;

    @Test
    void testReply(){

        String context = """
                
                """;
        String reply = service.generateReply(context).block();
        System.out.println(reply);
    }
}
