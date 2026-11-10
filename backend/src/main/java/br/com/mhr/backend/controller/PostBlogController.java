package br.com.mhr.backend.controller;

import java.util.List;

import br.com.mhr.backend.dto.PostBlogDto;
import br.com.mhr.backend.service.PostBlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints públicos do blog ({@code /api/posts-blog}), só retornam posts publicados. */
@RestController
@RequestMapping("/api/posts-blog")
@RequiredArgsConstructor
public class PostBlogController {

    private final PostBlogService service;

    @GetMapping
    public List<PostBlogDto> listarPublicados() {
        return service.listarPublicados();
    }

    @GetMapping("/{slug}")
    public PostBlogDto buscarPorSlug(@PathVariable String slug) {
        return service.buscarPorSlug(slug);
    }
}
