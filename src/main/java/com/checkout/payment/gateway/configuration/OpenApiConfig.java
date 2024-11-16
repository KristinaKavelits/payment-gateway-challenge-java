package com.checkout.payment.gateway.configuration;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI gatewayPaymentOpenApiSpec() {
    return new OpenAPI()
        .info(new Info().title("Payment Gateway API")
            .version("1.0")
            .description("API for processing and managing payments through the Payment Gateway"));
  }
}