package br.com.mhr.backend.security;

import br.com.mhr.backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    // >= 32 bytes, exigido pelo algoritmo HMAC-SHA usado em Jwts.builder().signWith(chave).
    private static final String SEGREDO = "teste-de-segredo-bem-longo-para-jwt-funcionar-ok";

    private JwtService jwtService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SEGREDO, 60);

        usuario = new Usuario();
        usuario.setEmail("admin@mhr.com.br");
        usuario.setNome("Administrador MHR");
        usuario.setPapel(Usuario.Papel.ADMIN);
    }

    @Test
    void gerarToken_incluiEmailComoSubjectEClaimsDeNomeEPapel() {
        String token = jwtService.gerarToken(usuario);

        Claims claims = jwtService.validar(token);

        assertThat(claims.getSubject()).isEqualTo("admin@mhr.com.br");
        assertThat(claims.get("papel", String.class)).isEqualTo("ADMIN");
        assertThat(claims.get("nome", String.class)).isEqualTo("Administrador MHR");
    }

    @Test
    void validar_comTokenAdulterado_lancaJwtException() {
        String token = jwtService.gerarToken(usuario);
        String adulterado = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> jwtService.validar(adulterado)).isInstanceOf(JwtException.class);
    }

    @Test
    void validar_comTokenExpirado_lancaExpiredJwtException() {
        JwtService servicoComExpiracaoCurta = new JwtService(SEGREDO, 0);
        String token = servicoComExpiracaoCurta.gerarToken(usuario);

        assertThatThrownBy(() -> servicoComExpiracaoCurta.validar(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void expiracaoMinutos_devolveOValorConfigurado() {
        assertThat(jwtService.expiracaoMinutos()).isEqualTo(60);
    }
}
