package org.automation.goofish;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import org.automation.goofish.core.socket.GoofishSocket;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class Starter {

    @Value("${spring.ai.dashscope.api-key}")
    String apiKey ;
    public static void main(String[] args) {
        SpringApplication.run(Starter.class, args);
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner run(GoofishSocket socket) {
        return args -> socket.establish().subscribe();
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * 存储多轮对话历史（基于内存）
     * 实现上下文感知的连续对话
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().build();
    }

    @Bean
    public DashScopeApi dashScopeApi() {
        return DashScopeApi.builder().apiKey(apiKey).build();
    }
}
