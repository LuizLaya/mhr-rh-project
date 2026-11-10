package br.com.mhr.backend.controller.admin;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.RecursoNaoEncontradoException;
import br.com.mhr.backend.dto.LogSistemaDto;
import br.com.mhr.backend.mapper.LogSistemaMapper;
import br.com.mhr.backend.repository.LogSistemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consulta e limpeza manual dos últimos eventos do sistema (login, e-mail, erros).
 * Restrito a ADMIN, mesmo padrão de {@link UsuarioAdminController} - é informação
 * operacional interna, não algo que um EDITOR precisa ver ou apagar.
 *
 * <p>A limpeza automática semanal ({@code LogSistemaService#limparAntigos()}) continua
 * existindo; isto aqui é só para o admin tirar ruído da tela antes disso (ex.: uma
 * sequência de erros já resolvidos, como os 429 de teste do PNCP).</p>
 */
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class LogSistemaAdminController {

    private final LogSistemaRepository repository;
    private final LogSistemaMapper mapper;

    @GetMapping
    public List<LogSistemaDto> listarRecentes() {
        return repository.findTop200ByOrderByCriadoEmDesc().stream().map(mapper::toDto).toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Log nao encontrado: " + id);
        }
        repository.deleteById(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void limparTodos() {
        repository.deleteAll();
    }
}
