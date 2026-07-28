package com.duckchess.duck_chess.auth;

import com.duckchess.duck_chess.auth.dtos.*;
import com.duckchess.duck_chess.common.exceptions.AuthException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static com.duckchess.duck_chess.auth.AuthTestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest 
{

    private UserRepository users;
    private EmailVerificationRepository verifications;
    private PasswordEncoder encoder;
    private JwtService jwt;
    private MailService mail;
    private AuthService service;

    @BeforeEach
    void setUp() 
    {
        users = mock(UserRepository.class);
        verifications = mock(EmailVerificationRepository.class);
        encoder = mock(PasswordEncoder.class);
        jwt = mock(JwtService.class);
        mail = mock(MailService.class);
        AuthProperties props = new AuthProperties(60, 7);

        service = new AuthService(users, verifications, encoder, jwt, mail, props);
    }


    @Test
    void registerHashesPasswordAndSendsCode() 
    {
        when(encoder.encode("secret12")).thenReturn("hashed");
        when(users.save(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        MessageResponse resp = service.register(
                new RegisterRequest("alice", "alice@example.com", "secret12"));

        assertThat(resp.message()).contains("registered");

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(users).save(userCaptor.capture());
        UserEntity saved = userCaptor.getValue();
        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.isEmailVerified()).isFalse();

        ArgumentCaptor<EmailVerificationEntity> verCaptor =
                ArgumentCaptor.forClass(EmailVerificationEntity.class);
        verify(verifications).save(verCaptor.capture());
        assertThat(verCaptor.getValue().getCode()).matches("\\d{6}");
        assertThat(verCaptor.getValue().getExpiresAt())
                .isAfter(OffsetDateTime.now().plusMinutes(59));

        verify(mail).sendVerificationCode(eq("alice@example.com"), anyString());
    }

    @Test
    void registerRejectsDuplicateEmail() 
    {
        when(users.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(
                new RegisterRequest("alice", "alice@example.com", "secret12")))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("email");

        verify(users, never()).save(any());
        verify(mail, never()).sendVerificationCode(anyString(), anyString());
    }

    @Test
    void registerRejectsDuplicateUsername() 
    {
        when(users.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> service.register(
                new RegisterRequest("alice", "alice@example.com", "secret12")))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("username");

        verify(users, never()).save(any());
    }

    // ---------------- verifyEmail ----------------

    @Test
    void verifyEmailMarksUserAndVerification() 
    {
        UserEntity user = unverifiedUser();
        EmailVerificationEntity v = activeCodeFor(user.getId(), "123456");

        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(verifications.findFirstByUserIdAndVerifiedAtIsNullOrderByCreatedAtDesc(user.getId()))
                .thenReturn(Optional.of(v));

        MessageResponse resp = service.verifyEmail(
                new VerifyEmailRequest(user.getEmail(), "123456"));

        assertThat(resp.message()).contains("verified");
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(v.getVerifiedAt()).isNotNull();
    }

    @Test
    void verifyEmailRejectsWrongCode() 
    {
        UserEntity user = unverifiedUser();
        EmailVerificationEntity v = activeCodeFor(user.getId(), "123456");

        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(verifications.findFirstByUserIdAndVerifiedAtIsNullOrderByCreatedAtDesc(user.getId()))
                .thenReturn(Optional.of(v));

        assertThatThrownBy(() -> service.verifyEmail(
                new VerifyEmailRequest(user.getEmail(), "999999")))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("invalid");

        assertThat(user.isEmailVerified()).isFalse();
        assertThat(v.getVerifiedAt()).isNull();
    }

    @Test
    void verifyEmailRejectsExpiredCode() 
    {
        UserEntity user = unverifiedUser();
        EmailVerificationEntity v = expiredCodeFor(user.getId(), "123456");

        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(verifications.findFirstByUserIdAndVerifiedAtIsNullOrderByCreatedAtDesc(user.getId()))
                .thenReturn(Optional.of(v));

        assertThatThrownBy(() -> service.verifyEmail(
                new VerifyEmailRequest(user.getEmail(), "123456")))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("expired");

        assertThat(user.isEmailVerified()).isFalse();
    }

    @Test
    void verifyEmailRejectsUnknownEmail() 
    {
        when(users.findByEmail("nobody@x.y")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifyEmail(
                new VerifyEmailRequest("nobody@x.y", "123456")))
                .isInstanceOf(AuthException.class);
    }

    @Test
    void verifyEmailRejectsAlreadyVerifiedUser() 
    {
        UserEntity user = verifiedUser();
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.verifyEmail(
                new VerifyEmailRequest(user.getEmail(), "123456")))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("already");
    }

    @Test
    void verifyEmailRejectsWhenNoActiveCode() 
    {
        UserEntity user = unverifiedUser();
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(verifications.findFirstByUserIdAndVerifiedAtIsNullOrderByCreatedAtDesc(user.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifyEmail(
                new VerifyEmailRequest(user.getEmail(), "123456")))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("no active");
    }

    // ---------------- resendVerification ----------------

    @Test
    void resendVerificationCreatesFreshCode() 
    {
        UserEntity user = unverifiedUser();
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        MessageResponse resp = service.resendVerification(user.getEmail());

        assertThat(resp.message()).contains("sent");
        verify(verifications).save(any(EmailVerificationEntity.class));
        verify(mail).sendVerificationCode(eq(user.getEmail()), anyString());
    }

    @Test
    void resendVerificationRejectsAlreadyVerifiedUser() 
    {
        UserEntity user = verifiedUser();
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.resendVerification(user.getEmail()))
                .isInstanceOf(AuthException.class);

        verify(verifications, never()).save(any());
        verify(mail, never()).sendVerificationCode(anyString(), anyString());
    }

    // ---------------- login ----------------

    @Test
    void loginByEmailReturnsToken() 
    {
        UserEntity user = verifiedUser();
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(encoder.matches("secret12", user.getPasswordHash())).thenReturn(true);
        when(jwt.generate(user.getId(), user.getUsername())).thenReturn("token-value");

        TokenResponse resp = service.login(
                new LoginRequest(user.getEmail(), "secret12"));

        assertThat(resp.token()).isEqualTo("token-value");
        assertThat(resp.username()).isEqualTo(user.getUsername());
        assertThat(resp.userId()).isEqualTo(user.getId());
        verify(users, never()).findByUsername(anyString());
    }

    @Test
    void loginByUsernameReturnsToken() 
    {
        UserEntity user = verifiedUser();
        when(users.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(encoder.matches("secret12", user.getPasswordHash())).thenReturn(true);
        when(jwt.generate(user.getId(), user.getUsername())).thenReturn("token-value");

        TokenResponse resp = service.login(
                new LoginRequest(user.getUsername(), "secret12"));

        assertThat(resp.token()).isEqualTo("token-value");
        verify(users, never()).findByEmail(anyString());
    }

    @Test
    void loginWithUnknownUserGivesGenericError() 
    {
        when(users.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(
                new LoginRequest("ghost@x.y", "secret12")))
                .isInstanceOf(AuthException.class)
                .hasMessage("invalid credentials");
    }

    @Test
    void loginWithWrongPasswordGivesGenericError() 
    {
        UserEntity user = verifiedUser();
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(encoder.matches("wrong", user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> service.login(
                new LoginRequest(user.getEmail(), "wrong")))
                .isInstanceOf(AuthException.class)
                .hasMessage("invalid credentials");

        verify(jwt, never()).generate(anyLong(), anyString());
    }

    @Test
    void loginBlocksUnverifiedUser() 
    {
        UserEntity user = unverifiedUser();
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(encoder.matches("secret12", user.getPasswordHash())).thenReturn(true);

        assertThatThrownBy(() -> service.login(
                new LoginRequest(user.getEmail(), "secret12")))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("not verified");

        verify(jwt, never()).generate(anyLong(), anyString());
    }

    @Test
    void loginEnumerationProtection() 
    {
        when(users.findByEmail("ghost@x.y")).thenReturn(Optional.empty());
        UserEntity user = verifiedUser();
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(encoder.matches("wrong", user.getPasswordHash())).thenReturn(false);

        AuthException ghostError = null;
        AuthException wrongError = null;
        try 
        { 
            service.login(new LoginRequest("ghost@x.y", "any")); 
        }
        catch(AuthException e) 
        { 
            ghostError = e; 
        }
        try
        { 
            service.login(new LoginRequest(user.getEmail(), "wrong"));
        }
        catch(AuthException e) 
        { 
            wrongError = e; 
        }

        assertThat(ghostError).isNotNull();
        assertThat(wrongError).isNotNull();
        assertThat(ghostError.getMessage()).isEqualTo(wrongError.getMessage());
    }
}