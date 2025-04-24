package com.emobile.springtodo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "TODO API",
                version = "1.0",
                description = "API for managing TODO items"
        )
)
public class OpenApiConfig {
}