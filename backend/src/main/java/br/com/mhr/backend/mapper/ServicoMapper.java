package br.com.mhr.backend.mapper;

import br.com.mhr.backend.dto.ServicoDto;
import br.com.mhr.backend.dto.request.SalvarServicoRequest;
import br.com.mhr.backend.model.Servico;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/** Converte entidade {@link Servico} em {@link ServicoDto} (MapStruct gera a implementação em build time). */
@Mapper(componentModel = "spring")
public interface ServicoMapper {

    ServicoDto toDto(Servico entity);

    Servico toEntity(SalvarServicoRequest request);

    void atualizar(SalvarServicoRequest request, @MappingTarget Servico entity);
}
