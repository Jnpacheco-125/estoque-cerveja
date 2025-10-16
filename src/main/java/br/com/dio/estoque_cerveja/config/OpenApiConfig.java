package br.com.dio.estoque_cerveja.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Estoque de Cervejas 🍺")
                        .description("Gerenciamento de cervejas com controle de estoque, tipos e quantidades máximas.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe Estoque Cerveja")
                                .email("contato@estoquecerveja.com")
                                .url("https://github.com/seu-repositorio")));
    }
}
