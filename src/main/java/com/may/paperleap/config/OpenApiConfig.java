package com.may.paperleap.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("伙伴匹配系统")
                .version("1.0.0")
                .description("本项目是伙伴匹配系统的 API 文档")
                .contact(new Contact().name("澹台").email("txcc@example.com")));
    }

}
