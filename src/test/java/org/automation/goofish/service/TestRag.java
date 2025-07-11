package org.automation.goofish.service;

import org.automation.goofish.Starter;
import org.automation.goofish.service.rag.CloudRagService;
import org.automation.goofish.service.rag.LocalRagService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static java.lang.invoke.MethodHandles.lookup;

@ActiveProfiles("test")
@SpringBootTest(classes = Starter.class)
public class TestRag {

    private static final Logger logger = LoggerFactory.getLogger(lookup().lookupClass());

    @Autowired
    AutoReplyService autoReplyService;

    @Autowired
    LocalRagService localRagService;
    @Autowired
    CloudRagService cloudRagService;

    @Test
    void testText() {
        String prompt = "分析比较一下任瓜堂的Slip1 和 Slip2 有什么区别";
        String result = autoReplyService.generateReply(prompt).block();
        logger.info(result);
    }
}
