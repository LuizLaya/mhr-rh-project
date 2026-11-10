package br.com.mhr.backend.mapper;

import br.com.mhr.backend.dto.FaqDto;
import br.com.mhr.backend.dto.request.SalvarFaqRequest;
import br.com.mhr.backend.model.Faq;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/** Converte entidade {@link Faq} em {@link FaqDto} (MapStruct gera a implementação em build time). */
@Mapper(componentModel = "spring")
public interface FaqMapper {

    FaqDto toDto(Faq entity);

    Faq toEntity(SalvarFaqRequest request);

    void atualizar(SalvarFaqRequest request, @MappingTarget Faq entity);
}
