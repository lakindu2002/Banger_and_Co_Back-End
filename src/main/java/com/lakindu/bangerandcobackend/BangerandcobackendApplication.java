package com.lakindu.bangerandcobackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;


@SpringBootApplication
@EnableAsync //enable async operations
@EnableScheduling //enable schedule jobs
public class BangerandcobackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BangerandcobackendApplication.class, args);
    }

    @Bean(name = "taskExecutor")
    public Executor executor() {
        //configure a thread pool executor
        ThreadPoolTaskExecutor theExecutor = new ThreadPoolTaskExecutor();
        theExecutor.initialize(); //enable methods to utilize the @Async operation so that background operations can be carried out.
        return theExecutor;
    }
}
