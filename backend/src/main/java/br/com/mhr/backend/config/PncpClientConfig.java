package br.com.mhr.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP para a API pública de consulta do PNCP (Portal Nacional de
 * Contratações Públicas).
 *
 * <p>{@code RestClient.builder()} (factory estática, não injeta
 * {@code RestClient.Builder} do Spring — este projeto não tem o módulo de
 * autoconfiguração de RestClient do Spring Boot 4 nas dependências) ainda
 * assim monta o conversor Jackson com suporte a {@code java.time} corretamente:
 * o {@code Jackson2ObjectMapperBuilder} por baixo escaneia o classpath e
 * registra o {@code JavaTimeModule} sozinho sempre que
 * {@code jackson-datatype-jsr310} está presente (já é dependência transitiva
 * de qualquer starter web do Spring Boot) — sem isso, desserializar os campos
 * {@code LocalDateTime} do PNCP falharia.</p>
 */
@Configuration
public class PncpClientConfig {

    @Bean
    public RestClient pncpRestClient() {
        return RestClient.builder().baseUrl("https://pncp.gov.br/api/consulta").build();
    }
}
