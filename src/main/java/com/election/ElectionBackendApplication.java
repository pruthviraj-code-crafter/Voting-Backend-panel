package com.election;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.service.annotation.GetExchange;

@OpenAPIDefinition(
        info = @Info(
                title = "VAVYUG project REST API Documentation",
                description = "VAVYUG application REST API Documentation",
                version="v1",
                contact = @Contact(
                        name = "Code-crafter",
                        email = "info@code-crafter.in",
                        url = "https://code-crafter.in"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description = "VAVYUG application REST API Documentation",
                url = "https://code-crafter.in"
        )
)
@SpringBootApplication
@RestController
@CrossOrigin("*")
@EnableScheduling
public class ElectionBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElectionBackendApplication.class, args);
    }

    @GetExchange("/test")
    public String home() {
        return "Welcome to Election Backend API";
    }
}
