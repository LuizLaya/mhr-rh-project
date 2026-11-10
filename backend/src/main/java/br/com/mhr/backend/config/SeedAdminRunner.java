package br.com.mhr.backend.config;

import br.com.mhr.backend.model.Usuario;
import br.com.mhr.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Garante que exista pelo menos um usuário ADMIN para o primeiro login no painel.
 * Credenciais vêm de {@code ADMIN_EMAIL}/{@code ADMIN_PASSWORD}; os valores padrão
 * abaixo servem só para desenvolvimento local e devem ser trocados em produção.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeedAdminRunner implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.default-email}")
    private String defaultEmail;

    @Value("${app.admin.default-password}")
    private String defaultPassword;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        Usuario admin = new Usuario();
        admin.setNome("Administrador MHR");
        admin.setEmail(defaultEmail);
        admin.setSenhaHash(passwordEncoder.encode(defaultPassword));
        admin.setPapel(Usuario.Papel.ADMIN);
        usuarioRepository.save(admin);

        log.warn("Nenhum usuario encontrado: criado admin padrao '{}'. " +
                "Troque a senha ou defina ADMIN_EMAIL/ADMIN_PASSWORD antes de ir para producao.", defaultEmail);
    }
}
