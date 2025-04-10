package com.example.ililbooks.config.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "ililBooks 온라인 서점 플랫폼",
                description = "사용자가 다양한 책을 검색, 주문, 리뷰할 수 있으며 한정판 책 예약 기능을 이용할 수 있는 온라인 서점 플랫폼을 개발",
                version = "1.0.0"
        )
)
@RequiredArgsConstructor
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi publicApi() {
        String[] packages = {"com.example.ililbooks"};
        return GroupedOpenApi.builder()
                .group("default")
                .packagesToScan(packages)
                .build();
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                        )
                )
                .security(Collections.singletonList(new SecurityRequirement().addList("JWT")));
    }
}
