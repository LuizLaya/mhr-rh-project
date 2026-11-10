package br.com.mhr.backend.service;

import java.util.UUID;

import br.com.mhr.backend.model.Usuario;
import br.com.mhr.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Convida alguém a acessar o painel admin: cria o {@link Usuario} já (sem senha
 * utilizável) e manda um e-mail com o link de ativação. A pessoa só consegue entrar
 * depois de abrir esse link e definir a própria senha — mesmo endpoint usado por
 * "esqueci minha senha" ({@code POST /api/auth/redefinir-senha}).
 */
@Service
@RequiredArgsConstructor
public class ConviteUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TokenAcessoService tokenAcessoService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend-admin-url}")
    private String frontendAdminUrl;

    @Transactional
    public ResultadoConvite convidar(String nome, String email, Usuario.Papel papel, Usuario convidadoPor) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ja existe um usuario cadastrado com esse e-mail");
        }

        Usuario novo = new Usuario();
        novo.setNome(nome);
        novo.setEmail(email);
        novo.setPapel(papel);
        // Senha aleatoria e inutilizavel: ninguem consegue logar com ela, so serve
        // para a coluna nunca ficar nula ate o convite ser aceito.
        novo.setSenhaHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        novo.setSenhaDefinida(false);
        novo.setAtivo(true);
        novo.setConvidadoPor(convidadoPor.getId());
        usuarioRepository.save(novo);

        String tokenBruto = tokenAcessoService.gerarToken(novo.getId());
        String link = frontendAdminUrl + "/aceitar-convite?token=" + tokenBruto;
        boolean emailEnviado = emailService.enviarConviteAdmin(email, convidadoPor.getNome(), link);

        return new ResultadoConvite(novo, emailEnviado);
    }

    /**
     * Resultado de um convite: o usuário criado e se o e-mail realmente saiu. Quem
     * convida (um ADMIN de verdade, autenticado) pode ver esse status sem risco de
     * segurança — diferente de "esqueci minha senha", que nunca revela nada ao
     * chamador para não permitir enumeração de e-mails cadastrados.
     */
    public record ResultadoConvite(Usuario usuario, boolean emailEnviado) {
    }
}
