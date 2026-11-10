package br.com.mhr.backend.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * Cria o cliente S3 (compativel com Cloudflare R2) usado por {@code UploadService}
 * para salvar imagens num storage de objeto, em vez de disco local.
 *
 * <p>Sempre registra o bean, mesmo sem nenhuma variavel R2_* configurada (ver
 * application.yml) - construir um {@link S3Client} nao faz nenhuma chamada de
 * rede nem valida as credenciais, entao e seguro criar um mesmo com valores em
 * branco. Quem decide se usa esse cliente de verdade ou cai pro disco local e
 * o {@code UploadService}, olhando se as variaveis estao preenchidas. (Uma
 * primeira versao tentava deixar esse bean condicionalmente ausente retornando
 * {@code null} do metodo @Bean - funciona num teste isolado, mas quebra a
 * resolucao de dependencia num ApplicationContext completo, onde o Spring
 * nunca chega a achar o bean como candidato de autowire.)</p>
 */
@Configuration
public class ArmazenamentoR2Config {

    @Bean
    public S3Client s3Client(
            @Value("${app.storage.r2.access-key:}") String accessKey,
            @Value("${app.storage.r2.secret-key:}") String secretKey,
            @Value("${app.storage.r2.endpoint:}") String endpoint) {
        // AwsBasicCredentials.create valida (em runtime, não só em tipo) que as duas
        // strings não estão em branco - sem configuração nenhuma, usa só um placeholder
        // não-vazio pra passar nessa validação; nunca chega a ser usado de verdade,
        // porque UploadService só chama esse client quando r2Habilitado() é true.
        URI endpointUri = URI.create(endpoint.isBlank() ? "https://r2-nao-configurado.invalid" : endpoint);
        String accessKeyEfetivo = accessKey.isBlank() ? "nao-configurado" : accessKey;
        String secretKeyEfetivo = secretKey.isBlank() ? "nao-configurado" : secretKey;
        return S3Client.builder()
                .endpointOverride(endpointUri)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyEfetivo, secretKeyEfetivo)))
                .region(Region.of("auto"))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }
}
