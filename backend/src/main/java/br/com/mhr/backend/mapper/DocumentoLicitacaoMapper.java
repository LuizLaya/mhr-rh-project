package br.com.mhr.backend.mapper;

import br.com.mhr.backend.dto.DocumentoLicitacaoDto;
import br.com.mhr.backend.model.DocumentoLicitacao;
import org.mapstruct.Mapper;

/** Converte entidade {@link DocumentoLicitacao} em {@link DocumentoLicitacaoDto} (MapStruct gera a implementação em build time). */
@Mapper(componentModel = "spring")
public interface DocumentoLicitacaoMapper {

    DocumentoLicitacaoDto toDto(DocumentoLicitacao entity);
}
