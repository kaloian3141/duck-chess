package com.duckchess.duck_chess.chess.movegen;

import com.duckchess.duck_chess.chess.Square;

/**
 * Precomputed attack tables for pieces whose attacks don't depend on
 * board occupancy — knights and kings.
 *
 * For sliding pieces (bishop, rook, queen), attacks depend on which
 * squares are blocked, so those are computed on demand by ray-walking
 * in MoveGenerator.
 *
 * Pawns are handled inline in MoveGenerator too, because their attacks
 * differ from their moves.
 */
public final class AttackTables 
{

    private AttackTables() {}

    public static final int[][] KNIGHT_ATTACKS = new int[64][];

    public static final int[][] KING_ATTACKS = new int[64][];

    public static final int[][] WHITE_PAWN_ATTACKS = new int[64][];
    public static final int[][] BLACK_PAWN_ATTACKS = new int[64][];

    private static final int[][] KNIGHT_OFFSETS = {
        { 1,  2}, { 2,  1}, { 2, -1}, { 1, -2},
        {-1, -2}, {-2, -1}, {-2,  1}, {-1,  2}
    };

    private static final int[][] KING_OFFSETS = {
        {-1, -1}, {0, -1}, {1, -1},
        {-1,  0},          {1,  0},
        {-1,  1}, {0,  1}, {1,  1}
    };

    static {
        for(int sq = 0; sq < 64; sq++) 
        {
            KNIGHT_ATTACKS[sq] = generateOffsetAttacks(sq, KNIGHT_OFFSETS);
            KING_ATTACKS[sq] = generateOffsetAttacks(sq, KING_OFFSETS);
            WHITE_PAWN_ATTACKS[sq] = pawnAttacks(sq, +1);
            BLACK_PAWN_ATTACKS[sq] = pawnAttacks(sq, -1);
        }
    }

    private static int[] generateOffsetAttacks(int sq, int[][] offsets) 
    {
        int file = Square.file(sq);
        int rank = Square.rank(sq);
        int[] buffer = new int[offsets.length];
        int count = 0;
        for(int[] off : offsets) 
        {
            int newFile = file + off[0];
            int newRank = rank + off[1];
            if(newFile >= 0 && newFile < 8 && newRank >= 0 && newRank < 8) 
            {
                buffer[count++] = Square.of(newFile, newRank);
            }
        }
        int[] result = new int[count];
        System.arraycopy(buffer, 0, result, 0, count);
        return result;
    }

    private static int[] pawnAttacks(int sq, int direction) 
    {
        int file = Square.file(sq);
        int rank = Square.rank(sq);
        int newRank = rank + direction;
        if(newRank < 0 || newRank > 7) 
            return new int[0];

        int[] buffer = new int[2];
        int count = 0;
        if(file > 0) 
            buffer[count++] = Square.of(file - 1, newRank);
        if(file < 7) 
            buffer[count++] = Square.of(file + 1, newRank);
        int[] result = new int[count];
        System.arraycopy(buffer, 0, result, 0, count);
        return result;
    }

    public static final int[][] BISHOP_DIRECTIONS = {
        { 1,  1}, { 1, -1}, {-1,  1}, {-1, -1}
    };

    public static final int[][] ROOK_DIRECTIONS = {
        { 1,  0}, {-1,  0}, { 0,  1}, { 0, -1}
    };

    public static final int[][] QUEEN_DIRECTIONS = {
        { 1,  0}, {-1,  0}, { 0,  1}, { 0, -1},
        { 1,  1}, { 1, -1}, {-1,  1}, {-1, -1}
    };
}