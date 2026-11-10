package br.com.mhr.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadados exibidos no Swagger UI ({@code /docs}). */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mhrOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MHR API")
                        .description("API do site institucional e portal de vagas da MHR")
                        .version("v1"));
    }
}
