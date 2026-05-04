package com.msel.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking Operations Platform API")
                        .version("1.0.0")
                        .description("REST API for managing banking transactions and users")
                        .contact(new Contact()
                                .name("Banking Operations Team")
                                .email("ops@bankingops.com")
                        )
                );
    }
}
