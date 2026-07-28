package com.duckchess.duck_chess.chess;

public enum PieceType {
    PAWN,
    KNIGHT,
    BISHOP,
    ROOK,
    QUEEN,
    KING;

    public char letter() {
        return switch (this) {
            case PAWN   -> 'P';
            case KNIGHT -> 'N';
            case BISHOP -> 'B';
            case ROOK   -> 'R';
            case QUEEN  -> 'Q';
            case KING   -> 'K';
        };
    }
}