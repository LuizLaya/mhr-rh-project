package br.com.mhr.backend.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.RecursoNaoEncontradoException;
import br.com.mhr.backend.dto.PostBlogDto;
import br.com.mhr.backend.dto.request.SalvarPostBlogRequest;
import br.com.mhr.backend.mapper.PostBlogMapper;
import br.com.mhr.backend.model.PostBlog;
import br.com.mhr.backend.repository.PostBlogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Regras de negócio de {@link PostBlog}, usadas pelos controllers público e admin. */
@Service
@RequiredArgsConstructor
public class PostBlogService {

    private final PostBlogRepository repository;
    private final PostBlogMapper mapper;

    public List<PostBlogDto> listarPublicados() {
        return repository.findByPublicadoTrueOrderByPublicadoEmDesc().stream()
                .map(mapper::toDto)
                .toList();
    }

    public PostBlogDto buscarPorSlug(String slug) {
        PostBlog post = repository.findBySlugAndPublicadoTrue(slug)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Post nao encontrado: " + slug));
        return mapper.toDto(post);
    }

    public List<PostBlogDto> listarTodos() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public PostBlogDto criar(SalvarPostBlogRequest request) {
        PostBlog post = mapper.toEntity(request);
        marcarDataDePublicacao(post);
        return mapper.toDto(repository.save(post));
    }

    public PostBlogDto atualizar(UUID id, SalvarPostBlogRequest request) {
        PostBlog post = buscarEntidadeOuFalhar(id);
        mapper.atualizar(request, post);
        marcarDataDePublicacao(post);
        return mapper.toDto(repository.save(post));
    }

    public void excluir(UUID id) {
        repository.delete(buscarEntidadeOuFalhar(id));
    }

    // Define publicado_em na primeira vez que o post e marcado como publicado;
    // despublicar depois nao apaga a data original.
    private void marcarDataDePublicacao(PostBlog post) {
        if (post.isPublicado() && post.getPublicadoEm() == null) {
            post.setPublicadoEm(Instant.now());
        }
    }

    private PostBlog buscarEntidadeOuFalhar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Post nao encontrado: " + id));
    }
}
