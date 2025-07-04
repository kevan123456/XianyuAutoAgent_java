package org.automation.goofish.interceptor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class AutoReplyService {

    private final ChatClient chatClient;

    public AutoReplyService(ChatClient.Builder builder) {
        String systemPrompt = """
                你是一个专业的自动回复助手。我会提供历史聊天记录作为上下文给你参考，上下文中包含商家和买家两个角色，你扮演商家，你的回复应该:
                1. 简洁明了
                2. 专业友好
                3. 针对用户问题提供准确信息
                """;
        this.chatClient = builder.defaultSystem(systemPrompt).build();
    }

    public Mono<String> generateReply(String... userMessages) {
        return Mono.fromCallable(() -> chatClient.prompt().user(String.join("\n\n\n", userMessages))
                .call().content()).subscribeOn(Schedulers.boundedElastic());
    }
}