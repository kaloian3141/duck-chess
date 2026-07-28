package com.duckchess.duck_chess.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.verification")
public record AuthProperties(
        int expirationMinutes,
        int unverifiedUserExpirationDays,
        int passwordResetExpirationMinutes
) {}