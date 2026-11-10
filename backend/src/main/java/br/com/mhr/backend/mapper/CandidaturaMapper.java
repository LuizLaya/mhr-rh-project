package br.com.mhr.backend.mapper;

import br.com.mhr.backend.dto.CandidaturaDto;
import br.com.mhr.backend.dto.request.CriarCandidaturaRequest;
import br.com.mhr.backend.model.Candidatura;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Converte entidade {@link Candidatura} em {@link CandidaturaDto} (MapStruct gera a implementação em build time). */
@Mapper(componentModel = "spring")
public interface CandidaturaMapper {

    CandidaturaDto toDto(Candidatura entity);

    @Mapping(target = "vagaId", ignore = true)
    Candidatura toEntity(CriarCandidaturaRequest request);
}
