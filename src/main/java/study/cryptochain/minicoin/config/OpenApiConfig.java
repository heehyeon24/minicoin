package study.cryptochain.minicoin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI miniCoinOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("MiniCoin Blockchain API")
                        .description("블록체인 구조 시각화를 위한 MiniCoin REST API 문서입니다.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("MiniCoin Dev Team")
                                .email("dev@minicoin.local"))
                        .license(new License().name("Apache 2.0"))
                );
    }
}
