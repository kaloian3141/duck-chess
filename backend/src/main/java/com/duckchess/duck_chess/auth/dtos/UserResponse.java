package com.duckchess.duck_chess.auth.dtos;

public record UserResponse(
        Long userId,
        String username,
        String email,
        int currentRating,
        int gamesPlayed,
        int wins,
        int losses,
        int draws
) {}