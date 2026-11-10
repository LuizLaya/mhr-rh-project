package br.com.mhr.backend.service;

import java.util.Properties;

import br.com.mhr.backend.dto.SistemaInfoDto;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SistemaServiceTest {

    @Mock
    private ObjectProvider<BuildProperties> buildPropertiesProvider;
    @Mock
    private Environment environment;
    @Mock
    private Flyway flyway;
    @Mock
    private MigrationInfoService migrationInfoService;
    @Mock
    private MigrationInfo migrationInfo;

    @InjectMocks
    private SistemaService service;

    @Test
    void obterInfo_comBuildPropertiesEMigracaoAplicada_preencheTodosOsCampos() {
        Properties propsBuild = new Properties();
        propsBuild.setProperty("version", "1.2.3");
        propsBuild.setProperty("time", "2026-01-01T10:00:00Z");
        BuildProperties buildProperties = new BuildProperties(propsBuild);

        when(buildPropertiesProvider.getIfAvailable()).thenReturn(buildProperties);
        when(flyway.info()).thenReturn(migrationInfoService);
        when(migrationInfoService.current()).thenReturn(migrationInfo);
        when(migrationInfo.getVersion()).thenReturn(MigrationVersion.fromVersion("9"));
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

        SistemaInfoDto resultado = service.obterInfo();

        assertThat(resultado.versaoAplicacao()).isEqualTo("1.2.3");
        assertThat(resultado.dataBuild()).isNotNull();
        assertThat(resultado.versaoBanco()).isEqualTo("9");
        assertThat(resultado.ambiente()).isEqualTo("prod");
        assertThat(resultado.versaoJava()).isEqualTo(System.getProperty("java.version"));
    }

    @Test
    void obterInfo_semBuildPropertiesNemMigracaoAplicada_devolveValoresPadrao() {
        when(buildPropertiesProvider.getIfAvailable()).thenReturn(null);
        when(flyway.info()).thenReturn(migrationInfoService);
        when(migrationInfoService.current()).thenReturn(null);
        when(environment.getActiveProfiles()).thenReturn(new String[0]);

        SistemaInfoDto resultado = service.obterInfo();

        assertThat(resultado.versaoAplicacao()).isEqualTo("dev");
        assertThat(resultado.dataBuild()).isNull();
        assertThat(resultado.versaoBanco()).isEqualTo("desconhecida");
        assertThat(resultado.ambiente()).isEqualTo("default");
    }
}
