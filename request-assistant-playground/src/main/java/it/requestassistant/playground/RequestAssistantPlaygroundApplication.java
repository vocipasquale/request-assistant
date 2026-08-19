package it.requestassistant.playground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RequestAssistantPlaygroundApplication {

    public static void main(String[] args) {
        SpringApplication.run(RequestAssistantPlaygroundApplication.class, args);
    }
}