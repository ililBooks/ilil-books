package com.example.ililbooks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableScheduling
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@EnableElasticsearchRepositories(basePackages = "com.example.ililbooks.domain.search.repository")
@EnableScheduling
public class IlilBooksApplication {
    public static void main(String[] args) {
        SpringApplication.run(IlilBooksApplication.class, args);
    }
}
