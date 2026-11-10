package br.com.mhr.backend.service;

import br.com.mhr.backend.service.turnstile.TurnstileRespostaDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Verifica o token do Cloudflare Turnstile (anti-spam) nos formulários públicos
 * (contato e candidatura), chamando {@code POST .../turnstile/v0/siteverify}.
 *
 * <p>Por padrão usa a chave secreta de teste publicada pela Cloudflare
 * ({@code 1x0000...AA}, sempre aprova), então funciona em desenvolvimento local
 * sem precisar de conta na Cloudflare - troque {@code TURNSTILE_SECRET_KEY} por
 * uma chave real (dashboard da Cloudflare > Turnstile) antes de qualquer uso em
 * produção, senão o anti-spam não protege nada de verdade.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TurnstileService {

    private static final String URL_VERIFICACAO = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    private final LogSistemaService logSistemaService;

    // RestClient.builder() (fabrica estatica) em vez de injetar RestClient.Builder:
    // o conjunto de dependencias deste projeto nao autoconfigura esse bean (mesmo
    // caso ja visto em PncpClientConfig).
    private final RestClient restClient = RestClient.builder().build();

    @Value("${app.turnstile.secret-key}")
    private String secretKey;

    /** @return {@code true} se o Cloudflare confirmou que o token é válido (ou seja, que não é um bot). */
    public boolean verificar(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            TurnstileRespostaDto resposta = restClient.post()
                    .uri(URL_VERIFICACAO)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body("secret=" + secretKey + "&response=" + token)
                    .retrieve()
                    .body(TurnstileRespostaDto.class);
            return resposta != null && resposta.success();
        } catch (Exception e) {
            log.error("Falha ao verificar token do Turnstile", e);
            logSistemaService.erro("TURNSTILE", "Falha ao verificar token: " + e.getMessage(), null);
            return false;
        }
    }
}
