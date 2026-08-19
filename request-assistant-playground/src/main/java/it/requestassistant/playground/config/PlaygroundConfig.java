package it.requestassistant.playground.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PlaygroundConfig {

    @Bean
    public RestClient requestAssistantClient(@Value("${requestassistant.url}") String url) {
        return RestClient.builder()
                .baseUrl(url)
                .build();
    }
}
