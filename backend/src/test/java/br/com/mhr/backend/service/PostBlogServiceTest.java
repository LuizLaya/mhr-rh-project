package br.com.mhr.backend.service;

import java.util.UUID;

import br.com.mhr.backend.dto.PostBlogDto;
import br.com.mhr.backend.dto.request.SalvarPostBlogRequest;
import br.com.mhr.backend.mapper.PostBlogMapper;
import br.com.mhr.backend.model.PostBlog;
import br.com.mhr.backend.repository.PostBlogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostBlogServiceTest {

    @Mock
    private PostBlogRepository repository;
    @Mock
    private PostBlogMapper mapper;

    @InjectMocks
    private PostBlogService service;

    @Test
    void criar_marcandoComoPublicado_definePublicadoEm() {
        PostBlog post = new PostBlog();
        post.setPublicado(true);
        when(mapper.toEntity(any())).thenReturn(post);
        when(repository.save(post)).thenReturn(post);
        when(mapper.toDto(post)).thenReturn(new PostBlogDto(null, null, null, null, null, null, null, true, null));

        service.criar(new SalvarPostBlogRequest("Titulo", "slug", "resumo", "conteudo", null, null, true));

        assertThat(post.getPublicadoEm()).isNotNull();
    }

    @Test
    void atualizar_comPublicadoEmJaDefinido_naoSobrescreveADataOriginal() {
        UUID id = UUID.randomUUID();
        PostBlog post = new PostBlog();
        java.time.Instant dataOriginal = java.time.Instant.now().minusSeconds(3600);
        post.setPublicado(true);
        post.setPublicadoEm(dataOriginal);
        when(repository.findById(id)).thenReturn(java.util.Optional.of(post));
        when(repository.save(post)).thenReturn(post);
        when(mapper.toDto(post)).thenReturn(new PostBlogDto(null, null, null, null, null, null, null, true, dataOriginal));

        service.atualizar(id, new SalvarPostBlogRequest("Titulo", "slug", "resumo", "conteudo", null, null, true));

        assertThat(post.getPublicadoEm()).isEqualTo(dataOriginal);
    }
}
