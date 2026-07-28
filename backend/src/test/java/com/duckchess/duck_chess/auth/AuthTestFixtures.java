package com.duckchess.duck_chess.auth;

import java.time.OffsetDateTime;

/**
 * Reusable test fixtures for the auth feature. Every method returns a
 * NEW instance so tests can safely mutate them without cross-test bleed.
 */
public final class AuthTestFixtures 
{

    private AuthTestFixtures() {}

    // ---------- users ----------

    public static UserEntity verifiedUser() 
    {
        return UserEntity.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .passwordHash("hashed-password")
                .emailVerified(true)
                .currentRating(1200)
                .build();
    }

    public static UserEntity unverifiedUser() 
    {
        return UserEntity.builder()
                .id(2L)
                .username("bob")
                .email("bob@example.com")
                .passwordHash("hashed-password")
                .emailVerified(false)
                .currentRating(1200)
                .build();
    }

    public static UserEntity userWith(Long id, String username, String email, boolean verified) 
    {
        return UserEntity.builder()
                .id(id)
                .username(username)
                .email(email)
                .passwordHash("hashed-password")
                .emailVerified(verified)
                .currentRating(1200)
                .build();
    }

    // ---------- verifications ----------

    public static EmailVerificationEntity activeCodeFor(Long userId, String code) 
    {
        return EmailVerificationEntity.builder()
                .id(100L)
                .userId(userId)
                .code(code)
                .expiresAt(OffsetDateTime.now().plusMinutes(30))
                .build();
    }

    public static EmailVerificationEntity expiredCodeFor(Long userId, String code) 
    {
        return EmailVerificationEntity.builder()
                .id(101L)
                .userId(userId)
                .code(code)
                .expiresAt(OffsetDateTime.now().minusMinutes(1))
                .build();
    }


        // ---------- password resets ----------

    public static PasswordResetEntity activeResetFor(Long userId, String code) 
    {
        return PasswordResetEntity.builder()
                .id(200L)
                .userId(userId)
                .code(code)
                .expiresAt(OffsetDateTime.now().plusMinutes(15))
                .build();
    }

    public static PasswordResetEntity expiredResetFor(Long userId, String code) 
    {
        return PasswordResetEntity.builder()
                .id(201L)
                .userId(userId)
                .code(code)
                .expiresAt(OffsetDateTime.now().minusMinutes(1))
                .build();
    }

    public static PasswordResetEntity usedResetFor(Long userId, String code) 
    {
        return PasswordResetEntity.builder()
                .id(202L)
                .userId(userId)
                .code(code)
                .expiresAt(OffsetDateTime.now().plusMinutes(15))
                .usedAt(OffsetDateTime.now().minusMinutes(1))
                .build();
    }
}