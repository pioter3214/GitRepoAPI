package com.example.gitrepoapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class GitRepoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitRepoApiApplication.class, args);
    }

    @Bean
    RestClient restClient(RestClient.Builder builder) {
        return builder.baseUrl("https://api.github.com").build();
    }

}
