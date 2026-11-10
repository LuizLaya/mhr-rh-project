package br.com.mhr.backend.mapper;

import br.com.mhr.backend.dto.PostBlogDto;
import br.com.mhr.backend.dto.request.SalvarPostBlogRequest;
import br.com.mhr.backend.model.PostBlog;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/** Converte entidade {@link PostBlog} em {@link PostBlogDto} (MapStruct gera a implementação em build time). */
@Mapper(componentModel = "spring")
public interface PostBlogMapper {

    PostBlogDto toDto(PostBlog entity);

    PostBlog toEntity(SalvarPostBlogRequest request);

    void atualizar(SalvarPostBlogRequest request, @MappingTarget PostBlog entity);
}
