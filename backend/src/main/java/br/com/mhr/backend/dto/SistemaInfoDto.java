package br.com.mhr.backend.dto;

import java.time.Instant;

/** Informações de versão/ambiente mostradas na tela "Versão" do admin (aba Sistema). */
public record SistemaInfoDto(
        String versaoAplicacao,
        Instant dataBuild,
        String versaoJava,
        String versaoSpringBoot,
        String versaoBanco,
        String ambiente
) {
}
