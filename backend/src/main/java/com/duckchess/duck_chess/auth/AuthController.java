package com.duckchess.duck_chess.auth;

import com.duckchess.duck_chess.auth.dtos.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController 
{

    private final AuthService authService;

    public AuthController(AuthService authService) 
    {
        this.authService = authService;
    }

    @PostMapping("/register")
    public MessageResponse register(@Valid @RequestBody RegisterRequest req) 
    {
        return authService.register(req);
    }

    @PostMapping("/verify-email")
    public MessageResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest req) 
    {
        return authService.verifyEmail(req);
    }

    @PostMapping("/resend-verification")
    public MessageResponse resendVerification(
            @RequestParam @NotBlank @Email String email) 
    {
        return authService.resendVerification(email);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) 
    {
        return authService.login(req);
    }
    @GetMapping("/me")
    public UserResponse me(Authentication auth) 
    {
        Long userId = (Long) auth.getPrincipal();
        return authService.getCurrentUser(userId);
    }
}