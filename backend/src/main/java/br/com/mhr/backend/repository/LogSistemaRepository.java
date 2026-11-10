package br.com.mhr.backend.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.model.LogSistema;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso a dados de {@link LogSistema}. */
public interface LogSistemaRepository extends JpaRepository<LogSistema, UUID> {

    List<LogSistema> findTop200ByOrderByCriadoEmDesc();

    long deleteByCriadoEmBefore(Instant limite);
}
