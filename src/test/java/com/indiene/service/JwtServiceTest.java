package com.indiene.service;

import com.indiene.config.JwtProperties;
import com.indiene.model.TipoUsuario;
import com.indiene.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "x".repeat(64);
    private static final String OUTRO_SEGREDO = "y".repeat(64);
    private static final long EXPIRATION_MS = 3_600_000L;

    private JwtService jwtService;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(SECRET, EXPIRATION_MS);
        jwtService = new JwtService(properties);
        key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private Usuario usuario() {
        return Usuario.builder()
                .id(UUID.fromString("3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7"))
                .nome("Fulano")
                .email("fulano@x.com")
                .senha("$2a$hash")
                .tipo(TipoUsuario.DESENVOLVEDOR)
                .build();
    }

    @Test
    void gerarToken_produzTokenComClaimsCorretos() {
        Usuario usuario = usuario();
        long antes = System.currentTimeMillis();

        String token = jwtService.gerarToken(usuario);

        Jws<Claims> jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        Claims claims = jws.getPayload();

        assertThat(claims.getSubject()).isEqualTo(usuario.getId().toString());
        assertThat(claims.get("email", String.class)).isEqualTo("fulano@x.com");
        assertThat(claims.get("tipo", String.class)).isEqualTo("DESENVOLVEDOR");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime())
                .isEqualTo(EXPIRATION_MS);
        assertThat(claims.getExpiration().getTime()).isGreaterThan(antes);
    }

    @Test
    void gerarToken_comSegredoDiferente_falhaNaVerificacao() {
        String token = jwtService.gerarToken(usuario());
        SecretKey outraChave = Keys.hmacShaKeyFor(OUTRO_SEGREDO.getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> Jwts.parser().verifyWith(outraChave).build().parseSignedClaims(token))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void gerarToken_quandoExpiracaoNegativa_tokenJaExpirado() {
        JwtProperties expirado = new JwtProperties(SECRET, -1000L);
        JwtService service = new JwtService(expirado);

        String token = service.gerarToken(usuario());

        assertThatThrownBy(() -> Jwts.parser().verifyWith(key).build().parseSignedClaims(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void getExpirationMs_retornaValorConfigurado() {
        assertThat(jwtService.getExpirationMs()).isEqualTo(EXPIRATION_MS);
    }
}
