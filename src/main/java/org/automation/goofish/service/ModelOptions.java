package org.automation.goofish.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelOptions {

    @Bean(name="intent-detect")
    public DashScopeChatOptions intentDetect(){
        return DashScopeChatOptions.builder().withModel("tongyi-intent-detect-v3").build();
    }
}
