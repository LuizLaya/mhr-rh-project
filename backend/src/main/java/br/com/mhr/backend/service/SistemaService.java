package br.com.mhr.backend.service;

import br.com.mhr.backend.dto.SistemaInfoDto;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/** Monta as informações de versão/ambiente da tela "Versão" do admin. */
@Service
@RequiredArgsConstructor
public class SistemaService {

    // BuildProperties só existe quando o jar foi gerado com `springBoot { buildInfo() }`
    // (ver build.gradle.kts) - em alguns contextos de teste isso não roda, por isso é
    // opcional (ObjectProvider) em vez de injetado direto.
    private final ObjectProvider<BuildProperties> buildPropertiesProvider;
    private final Environment environment;
    private final Flyway flyway;

    public SistemaInfoDto obterInfo() {
        BuildProperties build = buildPropertiesProvider.getIfAvailable();
        MigrationVersion versaoBanco = flyway.info().current() != null
                ? flyway.info().current().getVersion()
                : null;
        String[] perfisAtivos = environment.getActiveProfiles();

        return new SistemaInfoDto(
                build != null ? build.getVersion() : "dev",
                build != null ? build.getTime() : null,
                System.getProperty("java.version"),
                SpringBootVersion.getVersion(),
                versaoBanco != null ? versaoBanco.toString() : "desconhecida",
                perfisAtivos.length > 0 ? String.join(", ", perfisAtivos) : "default"
        );
    }
}
