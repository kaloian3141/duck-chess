package com.duckchess.duck_chess.chess.variant;

import com.duckchess.duck_chess.chess.Board;
import com.duckchess.duck_chess.chess.Color;
import com.duckchess.duck_chess.chess.Piece;
import com.duckchess.duck_chess.chess.PieceType;
import com.duckchess.duck_chess.chess.movegen.AttackDetector;
import com.duckchess.duck_chess.chess.movegen.MoveGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StandardChess implements Variant {

    public static final StandardChess INSTANCE = new StandardChess();

    private StandardChess() {}

    @Override
    public String name() 
    {
        return "STANDARD";
    }

    @Override
    public List<Integer> legalMoves(Board board)
    {
        return MoveGenerator.generateLegal(board);
    }

    @Override
    public GameResult result(Board board, List<Integer> legalMoves, List<Long> pastHashes)
    {
        if(legalMoves.isEmpty())
        {
            if(AttackDetector.isInCheck(board, board.sideToMove()))
            {
                return board.sideToMove() == Color.WHITE
                        ? GameResult.BLACK_WINS_CHECKMATE
                        : GameResult.WHITE_WINS_CHECKMATE;
            }
            return GameResult.DRAW_STALEMATE;
        }

        if(board.halfmoveClock() >= 100)
        {
            return GameResult.DRAW_FIFTY_MOVE_RULE;
        }

        if(isThreefoldRepetition(board, pastHashes))
        {
            return GameResult.DRAW_THREEFOLD_REPETITION;
        }

        if(isInsufficientMaterial(board))
        {
            return GameResult.DRAW_INSUFFICIENT_MATERIAL;
        }

        return GameResult.ONGOING;
    }

    private static boolean isThreefoldRepetition(Board board, List<Long> pastHashes)
    {
        long current = board.hash();
        Map<Long, Integer> counts = new HashMap<>();
        counts.put(current, 1);
        for(long h : pastHashes)
        {
            int c = counts.getOrDefault(h, 0) + 1;
            counts.put(h, c);
            if(c >= 3) return true;
        }
        return false;
    }

    /**
     * FIDE insufficient material:
     *   K vs K
     *   K + minor (bishop or knight) vs K
     *   K + bishop vs K + bishop with bishops on same color
     * More complex cases (e.g., K+N+N vs K, which is technically a draw but hard
     * to force) are left as ongoing — matches how most engines rule.
     */
    private static boolean isInsufficientMaterial(Board board)
    {
        int whitePawns = 0, blackPawns = 0;
        int whiteRooks = 0, blackRooks = 0;
        int whiteQueens = 0, blackQueens = 0;
        int whiteKnights = 0, blackKnights = 0;
        int whiteBishopLightSquares = 0, whiteBishopDarkSquares = 0;
        int blackBishopLightSquares = 0, blackBishopDarkSquares = 0;

        for(int sq = 0; sq < 64; sq++) 
        {
            byte p = board.get(sq);
            if(p == Piece.EMPTY || p == Piece.DUCK) continue;
            PieceType t = Piece.typeOf(p);
            boolean white = Piece.isWhite(p);
            switch(t) 
            {
                case PAWN -> { if(white) whitePawns++; else blackPawns++; }
                case ROOK -> { if(white) whiteRooks++; else blackRooks++; }
                case QUEEN -> { if(white) whiteQueens++; else blackQueens++; }
                case KNIGHT -> { if(white) whiteKnights++; else blackKnights++; }
                case BISHOP -> {
                    boolean lightSquare = ((com.duckchess.duck_chess.chess.Square.file(sq)
                            + com.duckchess.duck_chess.chess.Square.rank(sq)) % 2) == 0;
                    if(white) 
                    { 
                        if(lightSquare) 
                            whiteBishopLightSquares++; 
                        else 
                            whiteBishopDarkSquares++; 
                    }
                    else
                    { 
                        if(lightSquare) 
                            blackBishopLightSquares++; 
                        else 
                            blackBishopDarkSquares++; 
                    }
                }
                default -> {}
            }
        }

        if(whitePawns + blackPawns + whiteRooks + blackRooks + whiteQueens + blackQueens > 0) 
        {
            return false;
        }
        int whiteMinors = whiteKnights + whiteBishopLightSquares + whiteBishopDarkSquares;
        int blackMinors = blackKnights + blackBishopLightSquares + blackBishopDarkSquares;

        // K vs K
        if(whiteMinors == 0 && blackMinors == 0) 
            return true;

        // K + minor vs K
        if((whiteMinors == 1 && blackMinors == 0) || (whiteMinors == 0 && blackMinors == 1)) 
        {
            return true;
        }

        // K + bishop vs K + bishop, same-colored bishops
        if(whiteMinors == 1 && blackMinors == 1
                && whiteKnights == 0 && blackKnights == 0) 
        {
            // Both sides have exactly one bishop; check colors match
            boolean whiteLight = whiteBishopLightSquares == 1;
            boolean blackLight = blackBishopLightSquares == 1;
            if(whiteLight == blackLight) 
                return true;
        }

        return false;
    }
}