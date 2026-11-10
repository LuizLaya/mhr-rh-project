package br.com.mhr.backend.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.dto.UploadDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Salva imagens enviadas pelo admin (foto de depoimento, capa de post, logo de
 * cliente). Usa o storage de objeto R2 (ver ArmazenamentoR2Config) quando as
 * variaveis R2_* estao preenchidas - sobrevive a um redeploy, ao contrario de
 * disco local - e cai de volta pra disco local (servido em {@code /uploads/**},
 * ver WebConfig) quando não estão, o que mantém o desenvolvimento local
 * funcionando sem precisar de conta na Cloudflare.
 */
@Service
public class UploadService {

    private static final List<String> TIPOS_ACEITOS = List.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final S3Client s3Client;

    @Value("${app.uploads.dir}")
    private String uploadsDir;

    @Value("${app.storage.r2.access-key:}")
    private String r2AccessKey = "";

    @Value("${app.storage.r2.secret-key:}")
    private String r2SecretKey = "";

    @Value("${app.storage.r2.bucket:}")
    private String r2Bucket = "";

    @Value("${app.storage.r2.endpoint:}")
    private String r2Endpoint = "";

    @Value("${app.storage.r2.public-url:}")
    private String r2PublicUrl = "";

    public UploadService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    private boolean r2Habilitado() {
        return !r2AccessKey.isBlank() && !r2SecretKey.isBlank() && !r2Bucket.isBlank()
                && !r2Endpoint.isBlank() && !r2PublicUrl.isBlank();
    }

    /**
     * @param baseUrl esquema+host da própria API (ex.: "http://localhost:8080"),
     *                usado só para montar a URL quando o arquivo cai no fallback
     *                de disco local - ignorado quando salva no R2, cuja URL
     *                pública já é absoluta e não tem nenhuma relação com o host
     *                do backend.
     */
    public UploadDto salvar(MultipartFile arquivo, String baseUrl) {
        if (arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio");
        }
        String tipo = arquivo.getContentType();
        if (tipo == null || !TIPOS_ACEITOS.contains(tipo)) {
            throw new IllegalArgumentException("Tipo de arquivo nao aceito (use JPEG, PNG, WEBP ou GIF)");
        }

        String nomeArquivo = UUID.randomUUID() + extensaoPara(tipo);

        return r2Habilitado()
                ? salvarNoR2(arquivo, nomeArquivo, tipo)
                : salvarEmDisco(arquivo, nomeArquivo, baseUrl);
    }

    private UploadDto salvarNoR2(MultipartFile arquivo, String nomeArquivo, String tipo) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(r2Bucket).key(nomeArquivo).contentType(tipo).build(),
                    RequestBody.fromInputStream(arquivo.getInputStream(), arquivo.getSize()));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao enviar o arquivo para o storage", e);
        }
        return new UploadDto(r2PublicUrl + "/" + nomeArquivo);
    }

    private UploadDto salvarEmDisco(MultipartFile arquivo, String nomeArquivo, String baseUrl) {
        try {
            Path pasta = Path.of(uploadsDir);
            Files.createDirectories(pasta);
            arquivo.transferTo(pasta.resolve(nomeArquivo));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao salvar o arquivo enviado", e);
        }
        return new UploadDto(baseUrl + "/uploads/" + nomeArquivo);
    }

    private String extensaoPara(String tipoMime) {
        return switch (tipoMime) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> "";
        };
    }
}
