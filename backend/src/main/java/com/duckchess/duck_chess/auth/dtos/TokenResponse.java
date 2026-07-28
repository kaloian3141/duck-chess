package com.duckchess.duck_chess.auth.dtos;

public record TokenResponse(
        String token, 
        String username, 
        long userId
) 
{}