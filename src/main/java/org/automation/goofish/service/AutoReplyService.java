package org.automation.goofish.service;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static java.lang.invoke.MethodHandles.lookup;

@Service
public class AutoReplyService {
    private static final Logger logger = LoggerFactory.getLogger(lookup().lookupClass());
    private final ChatClient chatClient;
    private final String systemPrompt;

    @Autowired
    @SneakyThrows
    public AutoReplyService(ChatClient.Builder builder, @Value("${goofish.prompt-config-path}") Resource systemPrompt) {
        this.systemPrompt = new String(FileCopyUtils.copyToByteArray(systemPrompt.getInputStream()));
        this.chatClient = builder.defaultSystem(this.systemPrompt).build();
    }

    public Mono<String> generateReply(String prompt) {
        // 每次调用都获取最新prompt（双检锁优化）
        logger.info("send prompt to ai host: {}", prompt);
        return Mono.fromCallable(() ->
                chatClient.prompt()
                        .system(systemPrompt) // 动态注入最新system
                        .user(prompt)
                        .call()
                        .content()
        ).subscribeOn(Schedulers.boundedElastic());
    }
}