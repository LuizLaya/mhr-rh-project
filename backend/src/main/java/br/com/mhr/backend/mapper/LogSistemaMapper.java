package br.com.mhr.backend.mapper;

import br.com.mhr.backend.dto.LogSistemaDto;
import br.com.mhr.backend.model.LogSistema;
import org.mapstruct.Mapper;

/** Converte entidade {@link LogSistema} em {@link LogSistemaDto} (MapStruct gera a implementação em build time). */
@Mapper(componentModel = "spring")
public interface LogSistemaMapper {

    LogSistemaDto toDto(LogSistema entity);
}
