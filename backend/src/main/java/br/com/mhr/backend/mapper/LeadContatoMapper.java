package br.com.mhr.backend.mapper;

import br.com.mhr.backend.dto.LeadContatoDto;
import br.com.mhr.backend.dto.request.CriarLeadContatoRequest;
import br.com.mhr.backend.model.LeadContato;
import org.mapstruct.Mapper;

/** Converte entidade {@link LeadContato} em {@link LeadContatoDto} (MapStruct gera a implementação em build time). */
@Mapper(componentModel = "spring")
public interface LeadContatoMapper {

    LeadContatoDto toDto(LeadContato entity);

    LeadContato toEntity(CriarLeadContatoRequest request);
}
