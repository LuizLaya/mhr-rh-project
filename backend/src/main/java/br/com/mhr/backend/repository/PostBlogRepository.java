package br.com.mhr.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.mhr.backend.model.PostBlog;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso a dados de {@link PostBlog}. */
public interface PostBlogRepository extends JpaRepository<PostBlog, UUID> {

    List<PostBlog> findByPublicadoTrueOrderByPublicadoEmDesc();

    Optional<PostBlog> findBySlugAndPublicadoTrue(String slug);

    long countByPublicadoTrue();
}
