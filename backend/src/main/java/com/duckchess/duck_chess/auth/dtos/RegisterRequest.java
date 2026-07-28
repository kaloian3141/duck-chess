package com.duckchess.duck_chess.auth.dtos;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank
        @Size(min = 3, max = 32)
        @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "username can only contain letters, digits, and underscore")
        String username,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 8, max = 128)
        String password
) 
{}