package com.duckchess.duck_chess.auth.dtos;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank String usernameOrEmail,
        @NotBlank String password
) 
{}