package br.com.mhr.backend.mapper;

import br.com.mhr.backend.dto.ClienteDto;
import br.com.mhr.backend.dto.request.SalvarClienteRequest;
import br.com.mhr.backend.model.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/** Converte entidade {@link Cliente} em {@link ClienteDto} (MapStruct gera a implementação em build time). */
@Mapper(componentModel = "spring")
public interface ClienteMapper {

    ClienteDto toDto(Cliente entity);

    Cliente toEntity(SalvarClienteRequest request);

    void atualizar(SalvarClienteRequest request, @MappingTarget Cliente entity);
}
