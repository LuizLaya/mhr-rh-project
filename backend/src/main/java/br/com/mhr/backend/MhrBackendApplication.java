package br.com.mhr.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Ponto de entrada da API do site institucional e portal de vagas da MHR.
 *
 * <p>{@code @EnableScheduling} liga a limpeza semanal do log do sistema (ver
 * {@link br.com.mhr.backend.service.LogSistemaService#limparAntigos()}).
 */
@SpringBootApplication
@EnableScheduling
public class MhrBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MhrBackendApplication.class, args);
	}

}
