package br.com.mhr.backend.mapper;

import br.com.mhr.backend.dto.UsuarioDto;
import br.com.mhr.backend.model.Usuario;
import org.mapstruct.Mapper;

/** Converte entidade {@link Usuario} em {@link UsuarioDto} (MapStruct gera a implementação em build time). */
@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioDto toDto(Usuario entity);
}
