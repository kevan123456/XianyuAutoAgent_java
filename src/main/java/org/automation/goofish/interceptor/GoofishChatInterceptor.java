package org.automation.goofish.interceptor;

import org.automation.goofish.core.GoofishClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.ConnectException;

import static java.lang.invoke.MethodHandles.lookup;

@Component
public class GoofishChatInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(lookup().lookupClass());

    @Value("${goofish.socket-url}")
    String websocketUrl;

    @Autowired
    GoofishClient client;

    public GoofishChatInterceptor() {
    }

    public void refreshToken() throws InterruptedException {
        Thread.startVirtualThread(() -> {
            // 异步逻辑代码
            System.out.println("refreshing token...");
        });
    }

    public void tokenRefreshLoop() {
        Thread.startVirtualThread(() -> {
            System.out.println("loop refresh token");
        });
    }

    public void run() {
        while (true) {
            try {
                throw new ConnectException();
            } catch (ConnectException e) {
            }
        }
    }
}
