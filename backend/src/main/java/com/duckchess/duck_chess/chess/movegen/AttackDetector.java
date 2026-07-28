package com.duckchess.duck_chess.chess.movegen;

import com.duckchess.duck_chess.chess.Board;
import com.duckchess.duck_chess.chess.Color;
import com.duckchess.duck_chess.chess.Piece;
import com.duckchess.duck_chess.chess.PieceType;
import com.duckchess.duck_chess.chess.Square;

/**
 * "Is this square attacked by color X?" — the primitive used for
 * check detection, castling legality, and pin analysis.
 *
 * Reads the board; never modifies it.
 */
public final class AttackDetector {

    private AttackDetector() {}

    /** 
     * True if any piece of the given attacker color threatens the target
     * square. The target square itself may be empty or occupied.
     *
     * Ignores the duck as an attacker (the duck attacks nothing).
     */
    public static boolean isAttacked(Board board, int targetSquare, Color attacker) 
    {
        int[] pawnAttackers = (attacker == Color.WHITE)
                ? AttackTables.BLACK_PAWN_ATTACKS[targetSquare]
                : AttackTables.WHITE_PAWN_ATTACKS[targetSquare];
        byte attackerPawn = (attacker == Color.WHITE) ? Piece.WHITE_PAWN : Piece.BLACK_PAWN;
        for(int sq : pawnAttackers) 
        {
            if (board.get(sq) == attackerPawn) 
                return true;
        }

        byte attackerKnight = (attacker == Color.WHITE) ? Piece.WHITE_KNIGHT : Piece.BLACK_KNIGHT;
        for(int sq : AttackTables.KNIGHT_ATTACKS[targetSquare]) 
        {
            if(board.get(sq) == attackerKnight) 
                return true;
        }

        byte attackerKing = (attacker == Color.WHITE) ? Piece.WHITE_KING : Piece.BLACK_KING;
        for(int sq : AttackTables.KING_ATTACKS[targetSquare]) 
        {
            if(board.get(sq) == attackerKing) 
                return true;
        }

        
        if(raySliderAttacks(board, targetSquare, attacker,
                AttackTables.BISHOP_DIRECTIONS, PieceType.BISHOP)) return true;
        if(raySliderAttacks(board, targetSquare, attacker,
                AttackTables.ROOK_DIRECTIONS, PieceType.ROOK)) return true;

        return false;
    }

   
    private static boolean raySliderAttacks(
            Board board, int targetSquare, Color attacker,
            int[][] directions, PieceType sliderType
        ) 
    {
        byte attackerSlider = Piece.encode(attacker, sliderType);
        byte attackerQueen  = Piece.encode(attacker, PieceType.QUEEN);

        int targetFile = Square.file(targetSquare);
        int targetRank = Square.rank(targetSquare);

        for(int[] dir : directions) 
        {
            int file = targetFile + dir[0];
            int rank = targetRank + dir[1];
            while(file >= 0 && file < 8 && rank >= 0 && rank < 8) 
            {
                int sq = Square.of(file, rank);
                byte piece = board.get(sq);
                if(piece == Piece.EMPTY)
                {
                    file += dir[0];
                    rank += dir[1];
                    continue;
                }
                if(piece == attackerSlider || piece == attackerQueen) 
                {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    public static boolean isInCheck(Board board, Color color)
    {
        int kingSq = board.kingSquare(color);
        if(kingSq < 0) 
            return false;
        return isAttacked(board, kingSq, color.opponent());
    }
}