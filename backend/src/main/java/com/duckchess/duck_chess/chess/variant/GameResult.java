package com.duckchess.duck_chess.chess.variant;

import com.duckchess.duck_chess.chess.Color;

/**
 * Terminal state of a game.
 *
 * ONGOING means the game continues. Any other value means someone won,
 * the game was drawn, or it ended for some rule-specific reason.
 */
public enum GameResult {
    ONGOING,
    WHITE_WINS_CHECKMATE,
    BLACK_WINS_CHECKMATE,
    WHITE_WINS_KING_CAPTURED,     // duck chess
    BLACK_WINS_KING_CAPTURED,     // duck chess
    WHITE_WINS_FOWLING,           // duck chess — opponent had no legal move
    BLACK_WINS_FOWLING,           // duck chess
    DRAW_STALEMATE,               // standard chess only — no legal moves, not in check
    DRAW_FIFTY_MOVE_RULE,
    DRAW_THREEFOLD_REPETITION,
    DRAW_INSUFFICIENT_MATERIAL;

    public boolean isOver()
    {
        return this != ONGOING;
    }

    public boolean isDraw() 
    {
        return switch(this)
        {
            case DRAW_STALEMATE, DRAW_FIFTY_MOVE_RULE,
                 DRAW_THREEFOLD_REPETITION, DRAW_INSUFFICIENT_MATERIAL -> true;
            default -> false;
        };
    }

    public Color winner() 
    {
        return switch(this) 
        {
            case WHITE_WINS_CHECKMATE, WHITE_WINS_KING_CAPTURED, WHITE_WINS_FOWLING -> Color.WHITE;
            case BLACK_WINS_CHECKMATE, BLACK_WINS_KING_CAPTURED, BLACK_WINS_FOWLING -> Color.BLACK;
            default -> null;
        };
    }
}