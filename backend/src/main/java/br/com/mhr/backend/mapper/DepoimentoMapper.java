package br.com.mhr.backend.mapper;

import br.com.mhr.backend.dto.DepoimentoDto;
import br.com.mhr.backend.dto.request.SalvarDepoimentoRequest;
import br.com.mhr.backend.model.Depoimento;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/** Converte entidade {@link Depoimento} em {@link DepoimentoDto} (MapStruct gera a implementação em build time). */
@Mapper(componentModel = "spring")
public interface DepoimentoMapper {

    DepoimentoDto toDto(Depoimento entity);

    Depoimento toEntity(SalvarDepoimentoRequest request);

    void atualizar(SalvarDepoimentoRequest request, @MappingTarget Depoimento entity);
}
