package com.duckchess.duck_chess.auth.dtos;

import jakarta.validation.constraints.*;

public record VerifyEmailRequest(
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "^\\d{6}$", message = "code must be 6 digits") String code
) 
{}