package br.com.mhr.backend.dto;

/** URL pública do arquivo recém-salvo por {@code POST /api/admin/uploads}. */
public record UploadDto(String url) {
}
