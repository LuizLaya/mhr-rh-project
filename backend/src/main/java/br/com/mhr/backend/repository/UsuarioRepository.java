package br.com.mhr.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.mhr.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso a dados de {@link Usuario}. */
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByAtivoTrue();

    List<Usuario> findByAtivoTrueAndPapel(Usuario.Papel papel);

    List<Usuario> findByAtivoTrue();
}
