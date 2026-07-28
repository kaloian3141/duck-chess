package com.duckchess.duck_chess.auth;

import com.duckchess.duck_chess.auth.dtos.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController 
{

    private final AuthService auth;

    public AuthController(AuthService auth) 
    {
        this.auth = auth;
    }

    @PostMapping("/register")
    public MessageResponse register(@Valid @RequestBody RegisterRequest req) 
    {
        return auth.register(req);
    }

    @PostMapping("/verify-email")
    public MessageResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest req) 
    {
        return auth.verifyEmail(req);
    }

    @PostMapping("/resend-verification")
    public MessageResponse resendVerification(
            @RequestParam @NotBlank @Email String email) 
    {
        return auth.resendVerification(email);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) 
    {
        return auth.login(req);
    }
}