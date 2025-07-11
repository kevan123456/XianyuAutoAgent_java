package org.automation.goofish.service;

import lombok.Getter;
import lombok.SneakyThrows;
import org.automation.goofish.service.rag.CloudRagService;
import org.automation.goofish.service.rag.LocalRagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static java.lang.invoke.MethodHandles.lookup;

@Service
public class AutoReplyService implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(lookup().lookupClass());
    private final ChatClient chatClient;

    @Value("${goofish.system-prompt}")
    Resource systemPromptResource;
    public String systemPrompt = "你是一个电商平台客户";

    @Value("${goofish.emotion-prompt}")
    Resource emotionPromptResource;
    @Getter
    public String emotionPrompt;

    @Autowired
    @SneakyThrows
    public AutoReplyService(ChatClient.Builder builder,
                            LocalRagService localRagService,
                            CloudRagService cloudRagService) {
        this.chatClient = builder
                .defaultSystem(systemPrompt)
                /*.defaultAdvisors(
                        localRagService.getDocumentRetrievalAdvisor(),
                        cloudRagService.getDocumentRetrievalAdvisor())*/
                .build();
    }

    public Mono<String> generateReply(String prompt) {
        logger.info("send prompt to ai host: {}", prompt);
        return Mono.fromCallable(() ->
                chatClient.prompt()
                        .system(systemPrompt)
                        .user(prompt)
                        .call()
                        .content()
        ).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.systemPrompt = new String(FileCopyUtils.copyToByteArray(systemPromptResource.getInputStream()));
        this.emotionPrompt = new String(FileCopyUtils.copyToByteArray(emotionPromptResource.getInputStream()));
    }
}