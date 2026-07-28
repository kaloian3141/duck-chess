package com.duckchess.duck_chess.auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET =
            "test-secret-that-is-at-least-32-characters-long-abc";

    private JwtService service;

    @BeforeEach
    void setUp() 
    {
        service = new JwtService(SECRET, 7);
    }

    @Test
    void roundTripsUserIdAndUsername() 
    {
        String token = service.generate(42L, "alice");

        assertThat(service.extractUserId(token)).isEqualTo(42L);
        assertThat(service.extractUsername(token)).isEqualTo("alice");
    }

    @Test
    void rejectsTokenSignedWithDifferentSecret() 
    {
        JwtService other = new JwtService(
                "some-other-secret-that-is-also-32-chars-long-xyz", 7);
        String tokenFromOther = other.generate(42L, "alice");

        assertThatThrownBy(() -> service.extractUserId(tokenFromOther))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void rejectsExpiredToken() 
    {
        JwtService instantExpiry = new JwtService(SECRET, 0);
        String token = instantExpiry.generate(1L, "bob");

        assertThatThrownBy(() -> instantExpiry.extractUserId(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void rejectsMalformedToken() 
    {
        assertThatThrownBy(() -> service.extractUserId("not-a-real-jwt"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void differentUsersProduceDifferentTokens() 
    {
        String tokenA = service.generate(1L, "alice");
        String tokenB = service.generate(2L, "bob");

        assertThat(tokenA).isNotEqualTo(tokenB);
        assertThat(service.extractUserId(tokenA)).isEqualTo(1L);
        assertThat(service.extractUserId(tokenB)).isEqualTo(2L);
    }

    @Test
    void sameUserProducesDifferentTokensAtDifferentTimes() throws InterruptedException 
    {
        String first = service.generate(1L, "alice");
        Thread.sleep(1100);
        String second = service.generate(1L, "alice");

        assertThat(first).isNotEqualTo(second);
        assertThat(service.extractUserId(first)).isEqualTo(1L);
        assertThat(service.extractUserId(second)).isEqualTo(1L);
    }

    @Test
    void rejectsEmptyToken() 
    {
        assertThatThrownBy(() -> service.extractUserId(""))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void rejectsNullToken() 
    {
        assertThatThrownBy(() -> service.extractUserId(null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void rejectsTokenWithTamperedPayload() 
    {
        String token = service.generate(1L, "alice");
        String[] parts = token.split("\\.");
        char[] chars = parts[1].toCharArray();
        chars[0] = (chars[0] == 'A') ? 'B' : 'A';
        String tampered = parts[0] + "." + new String(chars) + "." + parts[2];

        assertThatThrownBy(() -> service.extractUserId(tampered))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void handlesLongUsername() 
    {
        String longName = "a".repeat(200);
        String token = service.generate(1L, longName);
        assertThat(service.extractUsername(token)).isEqualTo(longName);
    }

    @Test
    void handlesUnicodeUsername() 
    {
        String token = service.generate(1L, "калоян_🦆");
        assertThat(service.extractUsername(token)).isEqualTo("калоян_🦆");
    }

    @Test
    void handlesMaxLongUserId() 
    {
        String token = service.generate(Long.MAX_VALUE, "bigid");
        assertThat(service.extractUserId(token)).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void constructorRejectsShortSecret() 
    {
        assertThatThrownBy(() -> new JwtService("too-short", 7))
                .isInstanceOf(RuntimeException.class);
    }
}