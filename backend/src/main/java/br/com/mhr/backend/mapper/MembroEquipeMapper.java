package br.com.mhr.backend.mapper;

import br.com.mhr.backend.dto.MembroEquipeDto;
import br.com.mhr.backend.dto.request.SalvarMembroEquipeRequest;
import br.com.mhr.backend.model.MembroEquipe;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/** Converte entidade {@link MembroEquipe} em {@link MembroEquipeDto} (MapStruct gera a implementação em build time). */
@Mapper(componentModel = "spring")
public interface MembroEquipeMapper {

    MembroEquipeDto toDto(MembroEquipe entity);

    MembroEquipe toEntity(SalvarMembroEquipeRequest request);

    void atualizar(SalvarMembroEquipeRequest request, @MappingTarget MembroEquipe entity);
}
