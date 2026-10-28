package com.datathon.api.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("Datathon API")
                .version("0.0.1")
                .description("Natječaji, propisi i podudaranje profila s natječajima iz otvorenih podataka javnog sektora."));
    }
}
