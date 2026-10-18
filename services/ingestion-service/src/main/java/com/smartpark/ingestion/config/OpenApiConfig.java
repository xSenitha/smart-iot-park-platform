package com.smartpark.ingestion.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ingestionOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Smart IoT Park Platform - Ingestion Service")
                .description("High-throughput REST and MQTT sensor telemetry validation, ingestion, and time-series persistence into MongoDB.")
                .version("1.0.0")
                .contact(new Contact().name("Senitha Gihen").email("senithagihean@gmail.com"))
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}
