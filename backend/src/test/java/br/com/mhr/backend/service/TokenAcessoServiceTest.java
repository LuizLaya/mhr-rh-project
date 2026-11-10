package br.com.mhr.backend.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import br.com.mhr.backend.model.TokenRedefinicaoSenha;
import br.com.mhr.backend.repository.TokenRedefinicaoSenhaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenAcessoServiceTest {

    @Mock
    private TokenRedefinicaoSenhaRepository repository;

    @InjectMocks
    private TokenAcessoService service;

    @Test
    void gerarToken_salvaApenasOHashNuncaOTokenBruto() {
        UUID usuarioId = UUID.randomUUID();

        String tokenBruto = service.gerarToken(usuarioId);

        ArgumentCaptor<TokenRedefinicaoSenha> captor = ArgumentCaptor.forClass(TokenRedefinicaoSenha.class);
        verify(repository).save(captor.capture());
        TokenRedefinicaoSenha salvo = captor.getValue();

        assertThat(salvo.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(salvo.getTokenHash()).isNotEqualTo(tokenBruto);
        assertThat(salvo.getExpiraEm()).isAfter(Instant.now());
    }

    @Test
    void consumir_comTokenValido_marcaComoUsadoEDevolveIdDoUsuario() {
        UUID usuarioId = UUID.randomUUID();
        TokenRedefinicaoSenha token = new TokenRedefinicaoSenha();
        token.setUsuarioId(usuarioId);
        when(repository.findByTokenHashAndUsadoFalseAndExpiraEmAfter(anyString(), any(Instant.class)))
                .thenReturn(Optional.of(token));

        UUID resultado = service.consumir("qualquer-token-bruto");

        assertThat(resultado).isEqualTo(usuarioId);
        assertThat(token.isUsado()).isTrue();
        verify(repository).save(token);
    }

    @Test
    void consumir_comTokenInexistenteOuJaUsadoOuExpirado_lancaIllegalArgumentException() {
        when(repository.findByTokenHashAndUsadoFalseAndExpiraEmAfter(anyString(), any(Instant.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consumir("token-invalido"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalido ou expirado");
    }

    @Test
    void gerarToken_doisChamadasGeramTokensDiferentes() {
        UUID usuarioId = UUID.randomUUID();

        String primeiro = service.gerarToken(usuarioId);
        String segundo = service.gerarToken(usuarioId);

        assertThat(primeiro).isNotEqualTo(segundo);
    }

    @Test
    void gerarToken_expiraEmTrintaMinutos() {
        UUID usuarioId = UUID.randomUUID();

        service.gerarToken(usuarioId);

        ArgumentCaptor<TokenRedefinicaoSenha> captor = ArgumentCaptor.forClass(TokenRedefinicaoSenha.class);
        verify(repository).save(captor.capture());

        Instant esperado = Instant.now().plus(30, ChronoUnit.MINUTES);
        assertThat(captor.getValue().getExpiraEm()).isCloseTo(esperado, within(500, ChronoUnit.MILLIS));
    }
}
