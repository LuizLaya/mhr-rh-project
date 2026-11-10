package br.com.mhr.backend.service;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.RecursoNaoEncontradoException;
import br.com.mhr.backend.dto.ConvidarUsuarioResponseDto;
import br.com.mhr.backend.dto.UsuarioDto;
import br.com.mhr.backend.mapper.UsuarioMapper;
import br.com.mhr.backend.model.Usuario;
import br.com.mhr.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Gestão de usuários do painel admin: listar, convidar e ativar/desativar acesso.
 * Usada só por {@code UsuarioAdminController}, que já é restrito a ADMIN
 * (@PreAuthorize) — este service não repete essa checagem, confia na camada acima.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;
    private final ConviteUsuarioService conviteUsuarioService;

    public List<UsuarioDto> listar() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public ConvidarUsuarioResponseDto convidar(String nome, String email, Usuario.Papel papel, String emailConvidadoPor) {
        Usuario convidadoPor = buscarPorEmailOuFalhar(emailConvidadoPor);
        ConviteUsuarioService.ResultadoConvite resultado = conviteUsuarioService.convidar(nome, email, papel, convidadoPor);
        return new ConvidarUsuarioResponseDto(mapper.toDto(resultado.usuario()), resultado.emailEnviado());
    }

    public UsuarioDto atualizarAcesso(UUID id, boolean ativo, String emailSolicitante) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado: " + id));

        Usuario solicitante = buscarPorEmailOuFalhar(emailSolicitante);
        if (!ativo && usuario.getId().equals(solicitante.getId())) {
            throw new IllegalArgumentException("Voce nao pode desativar sua propria conta");
        }

        usuario.setAtivo(ativo);
        return mapper.toDto(repository.save(usuario));
    }

    // JwtAuthenticationFilter autentica usando o e-mail como "username" (ver AuthController.login).
    private Usuario buscarPorEmailOuFalhar(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado nao encontrado no banco"));
    }
}
