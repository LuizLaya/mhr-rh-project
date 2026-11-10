package br.com.mhr.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import br.com.mhr.backend.dto.UploadDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UploadServiceTest {

    @TempDir
    Path pastaTemporaria;

    private static final String BASE_URL = "http://localhost:8080";

    private UploadService novoServicoSemR2() {
        UploadService service = new UploadService(null);
        ReflectionTestUtils.setField(service, "uploadsDir", pastaTemporaria.toString());
        return service;
    }

    @Test
    void salvar_semR2Configurado_salvaEmDiscoEDevolveUrlAbsolutaComBaseUrl() throws IOException {
        UploadService service = novoServicoSemR2();
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "foto.png", "image/png", "conteudo".getBytes());

        UploadDto resultado = service.salvar(arquivo, BASE_URL);

        assertThat(resultado.url()).startsWith(BASE_URL + "/uploads/").endsWith(".png");
        String nomeArquivo = resultado.url().substring((BASE_URL + "/uploads/").length());
        assertThat(Files.exists(pastaTemporaria.resolve(nomeArquivo))).isTrue();
    }

    @Test
    void salvar_tipoNaoAceito_lancaIllegalArgumentExceptionSemSalvarNada() {
        UploadService service = novoServicoSemR2();
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "documento.pdf", "application/pdf", "conteudo".getBytes());

        assertThatThrownBy(() -> service.salvar(arquivo, BASE_URL)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void salvar_arquivoVazio_lancaIllegalArgumentException() {
        UploadService service = novoServicoSemR2();
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "foto.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> service.salvar(arquivo, BASE_URL)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void salvar_comR2Configurado_enviaProBucketEDevolveUrlPublicaSemUsarDisco() {
        S3Client s3Client = mock(S3Client.class);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());
        UploadService service = new UploadService(s3Client);
        ReflectionTestUtils.setField(service, "uploadsDir", pastaTemporaria.toString());
        ReflectionTestUtils.setField(service, "r2AccessKey", "chave-teste");
        ReflectionTestUtils.setField(service, "r2SecretKey", "segredo-teste");
        ReflectionTestUtils.setField(service, "r2Endpoint", "https://conta-teste.r2.cloudflarestorage.com");
        ReflectionTestUtils.setField(service, "r2Bucket", "mhr-uploads");
        ReflectionTestUtils.setField(service, "r2PublicUrl", "https://pub-teste.r2.dev");
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "logo.webp", "image/webp", "conteudo".getBytes());

        UploadDto resultado = service.salvar(arquivo, BASE_URL);

        assertThat(resultado.url()).startsWith("https://pub-teste.r2.dev/").endsWith(".webp");
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}
