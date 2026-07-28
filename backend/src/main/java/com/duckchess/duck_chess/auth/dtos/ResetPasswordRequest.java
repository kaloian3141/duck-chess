package com.duckchess.duck_chess.auth.dtos;

import jakarta.validation.constraints.*;

public record ResetPasswordRequest(
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "^\\d{6}$", message = "code must be 6 digits") String code,
        @NotBlank @Size(min = 8, max = 128) String newPassword
) 
{}
