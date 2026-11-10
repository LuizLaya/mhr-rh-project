package br.com.mhr.backend.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.mhr.backend.TestcontainersConfiguration;
import br.com.mhr.backend.dto.request.ConvidarUsuarioRequest;
import br.com.mhr.backend.dto.request.LoginRequest;
import br.com.mhr.backend.model.Usuario;
import br.com.mhr.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica de ponta a ponta (JWT real + filtro de segurança real) que só ADMIN acessa
 * {@code /api/admin/usuarios/**} — EDITOR é barrado com 403. Esse tipo de garantia
 * declarativa ({@code @PreAuthorize}) só se valida de verdade com o filtro de segurança
 * no ar, por isso é um teste de integração (Testcontainers), não um unit test isolado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class UsuarioAdminControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void limparUsuarios() {
        usuarioRepository.deleteAll();
    }

    @Test
    void editor_tentandoConvidarUsuario_recebe403() throws Exception {
        criarUsuario("editor@mhr.com.br", Usuario.Papel.EDITOR);
        String token = login("editor@mhr.com.br");

        mockMvc.perform(post("/api/admin/usuarios/convidar")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new ConvidarUsuarioRequest("Novo Usuario", "novo@mhr.com.br", Usuario.Papel.EDITOR))))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_convidandoUsuario_recebe201() throws Exception {
        criarUsuario("admin@mhr.com.br", Usuario.Papel.ADMIN);
        String token = login("admin@mhr.com.br");

        mockMvc.perform(post("/api/admin/usuarios/convidar")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new ConvidarUsuarioRequest("Novo Usuario", "novo2@mhr.com.br", Usuario.Papel.EDITOR))))
                .andExpect(status().isCreated());
    }

    @Test
    void semToken_recebeRespostaNaoAutorizada() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.assertj.core.api.Assertions.assertThat(status).isIn(401, 403);
                });
    }

    private void criarUsuario(String email, Usuario.Papel papel) {
        Usuario usuario = new Usuario();
        usuario.setNome("Teste");
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode("senha123"));
        usuario.setPapel(papel);
        usuarioRepository.save(usuario);
    }

    private String login(String email) throws Exception {
        String corpo = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "senha123"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(corpo).get("token").asText();
    }
}
