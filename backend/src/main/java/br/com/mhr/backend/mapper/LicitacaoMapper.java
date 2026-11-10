package br.com.mhr.backend.mapper;

import br.com.mhr.backend.dto.LicitacaoDto;
import br.com.mhr.backend.dto.request.SalvarLicitacaoRequest;
import br.com.mhr.backend.model.Licitacao;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/** Converte entidade {@link Licitacao} em {@link LicitacaoDto} (MapStruct gera a implementação em build time). */
@Mapper(componentModel = "spring")
public interface LicitacaoMapper {

    LicitacaoDto toDto(Licitacao entity);

    Licitacao toEntity(SalvarLicitacaoRequest request);

    void atualizar(SalvarLicitacaoRequest request, @MappingTarget Licitacao entity);
}
