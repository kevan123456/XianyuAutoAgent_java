package org.automation.goofish;

import org.automation.goofish.core.socket.GoofishSocket;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class Starter {
    public static void main(String[] args) {
        SpringApplication.run(Starter.class, args);
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner run(GoofishSocket socket) {
        return args -> {
            socket.establish().subscribe();
        };
    }
}
