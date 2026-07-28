package com.duckchess.duck_chess.auth;

import com.duckchess.duck_chess.auth.dtos.*;
import com.duckchess.duck_chess.common.exceptions.AuthException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class AuthService
{

    private final UserRepository users;
    private final EmailVerificationRepository verifications;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final MailService mail;
    private final AuthProperties props;
    private final SecureRandom random = new SecureRandom();
    private final PasswordResetRepository resets;

    public AuthService(
            UserRepository users,
            EmailVerificationRepository verifications,
            PasswordResetRepository resets,
            PasswordEncoder encoder,
            JwtService jwt,
            MailService mail,
            AuthProperties props
    ) 
    {
        this.users = users;
        this.verifications = verifications;
        this.resets = resets;
        this.encoder = encoder;
        this.jwt = jwt;
        this.mail = mail;
        this.props = props;
    }

    @Transactional
    public MessageResponse register(RegisterRequest req) 
    {
        if(users.existsByEmail(req.email())) 
        {
            throw new AuthException("email already registered");
        }
        if(users.existsByUsername(req.username())) 
        {
            throw new AuthException("username already taken");
        }

        UserEntity user = UserEntity.builder()
                .username(req.username())
                .email(req.email())
                .passwordHash(encoder.encode(req.password()))
                .emailVerified(false)
                .currentRating(1200)
                .build();
        users.save(user);

        issueAndSendCode(user);
        return new MessageResponse("registered; check your email for verification code");
    }

    @Transactional
    public MessageResponse verifyEmail(VerifyEmailRequest req) 
    {
        UserEntity user = users.findByEmail(req.email())
                .orElseThrow(() -> new AuthException("no account for this email"));

        if(user.isEmailVerified()) 
        {
            throw new AuthException("email already verified");
        }

        EmailVerificationEntity v = verifications
                .findFirstByUserIdAndVerifiedAtIsNullOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new AuthException("no active verification code"));

        if(v.getExpiresAt().isBefore(OffsetDateTime.now())) 
        {
            throw new AuthException("verification code expired");
        }
        if(!v.getCode().equals(req.code())) 
        {
            throw new AuthException("invalid verification code");
        }

        v.setVerifiedAt(OffsetDateTime.now());
        user.setEmailVerified(true);

        return new MessageResponse("email verified");
    }

    @Transactional
    public MessageResponse resendVerification(String email) 
    {
        UserEntity user = users.findByEmail(email)
                .orElseThrow(() -> new AuthException("no account for this email"));

        if(user.isEmailVerified()) 
        {
            throw new AuthException("email already verified");
        }

        issueAndSendCode(user);
        return new MessageResponse("verification code sent");
    }

    @Transactional(noRollbackFor = AuthException.class)
    public TokenResponse login(LoginRequest req) 
    {
        Optional<UserEntity> lookup = req.usernameOrEmail().contains("@")
                ? users.findByEmail(req.usernameOrEmail())
                : users.findByUsername(req.usernameOrEmail());

        UserEntity user = lookup.orElseThrow(
                () -> new AuthException("invalid credentials"));

        if(!encoder.matches(req.password(), user.getPasswordHash())) 
        {
            throw new AuthException("invalid credentials");
        }
        if(!user.isEmailVerified()) 
        {
            issueAndSendCode(user);
            throw new AuthException("email not verified");
        }

        String token = jwt.generate(user.getId(), user.getUsername());
        return new TokenResponse(token, user.getUsername(), user.getId());
    }

    private void issueAndSendCode(UserEntity user) 
    {
        String code = String.format("%06d", random.nextInt(1_000_000));

        EmailVerificationEntity v = EmailVerificationEntity.builder()
                .userId(user.getId())
                .code(code)
                .expiresAt(OffsetDateTime.now().plusMinutes(props.expirationMinutes()))
                .build();
        verifications.save(v);

        mail.sendVerificationCode(user.getEmail(), code);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) 
    {
        UserEntity user = users.findById(userId)
            .orElseThrow(() -> new AuthException("user not found"));
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getCurrentRating(),
            user.getGamesPlayed(),
            user.getWins(),
            user.getLosses(),
            user.getDraws()
        );
    }

    @Transactional
    public MessageResponse forgotPassword(String email) 
    {
        users.findByEmail(email).ifPresent(this::issueAndSendResetCode);
        return new MessageResponse(
                "if that email is registered, a reset code has been sent");
    }

    @Transactional
    public MessageResponse resetPassword(String email, String code, String newPassword) 
    {
        UserEntity user = users.findByEmail(email)
                .orElseThrow(() -> new AuthException("invalid reset request"));

        PasswordResetEntity reset = resets
                .findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new AuthException("invalid reset request"));

        if(reset.getExpiresAt().isBefore(OffsetDateTime.now())) 
        {
            throw new AuthException("reset code expired");
        }
        if(!reset.getCode().equals(code)) 
        {
            throw new AuthException("invalid reset request");
        }

        user.setPasswordHash(encoder.encode(newPassword));
        reset.setUsedAt(OffsetDateTime.now());

        return new MessageResponse("password reset; log in with your new password");
    }

    private void issueAndSendResetCode(UserEntity user) 
    {
        String code = String.format("%06d", random.nextInt(1_000_000));

        PasswordResetEntity reset = PasswordResetEntity.builder()
                .userId(user.getId())
                .code(code)
                .expiresAt(OffsetDateTime.now().plusMinutes(props.passwordResetExpirationMinutes()))
                .build();
        resets.save(reset);

        mail.sendPasswordResetCode(user.getEmail(), code);
    }
}