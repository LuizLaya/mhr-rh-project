package br.com.mhr.backend.mapper;

import br.com.mhr.backend.dto.VagaDto;
import br.com.mhr.backend.dto.request.SalvarVagaRequest;
import br.com.mhr.backend.model.Vaga;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/** Converte entidade {@link Vaga} em {@link VagaDto} (MapStruct gera a implementação em build time). */
@Mapper(componentModel = "spring")
public interface VagaMapper {

    VagaDto toDto(Vaga entity);

    Vaga toEntity(SalvarVagaRequest request);

    void atualizar(SalvarVagaRequest request, @MappingTarget Vaga entity);
}
