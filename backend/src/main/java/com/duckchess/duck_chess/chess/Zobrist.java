package com.duckchess.duck_chess.chess;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
/**
 * Zobrist hashing for chess positions.
 *
 * Each atom of position state gets a random long; the position's hash is
 * the XOR of all applicable atoms. XOR is its own inverse, so make/unmake
 * updates are incremental — one XOR per changed atom.
 *
 * Two positions with the same hash are (almost certainly) the same
 * position. Collisions have probability ~2^-64.
 */
public final class Zobrist 
{

    private Zobrist() {}

    // Piece codes match the byte encoding in Piece.java (0-15).
    // Empty (0) and duck (15) are handled specially: empty contributes nothing,
    // duck goes through DUCK_SQUARE.
    // Indices 7 and 8 are unused (gap between white and black pieces).
    private static final long[][] PIECE_SQUARE = new long[16][64];

    private static final long BLACK_TO_MOVE;

    private static final long[] CASTLING = new long[4];  // KQkq indices matching Board.CASTLE_* bit positions

    private static final long[] EN_PASSANT_FILE = new long[8];

    private static final long[] DUCK_SQUARE = new long[64];

    static 
    {
        // Deterministic seed so hashes are reproducible across runs.
        // (For search purposes, only the algebra matters, not which specific seed.)
        RandomGeneratorFactory<RandomGenerator> factory = RandomGeneratorFactory.of("L64X128MixRandom");
        RandomGenerator rng = factory.create(0xDEADBEEFCAFEBABEL);

        for(int piece = 0; piece < 16; piece++) 
        {
            for(int sq = 0; sq < 64; sq++)
            {
                PIECE_SQUARE[piece][sq] = rng.nextLong();
            }
        }

        BLACK_TO_MOVE = rng.nextLong();

        for(int i = 0; i < 4; i++)
        {
            CASTLING[i] = rng.nextLong();
        }
        for(int i = 0; i < 8; i++) 
        {
            EN_PASSANT_FILE[i] = rng.nextLong();
        }
        for(int i = 0; i < 64; i++)
        {
            DUCK_SQUARE[i] = rng.nextLong();
        }
    }

    public static long computeHash(Board board) 
    {
        long h = 0L;

        for(int sq = 0; sq < 64; sq++)
        {
            byte piece = board.get(sq);
            if(piece != Piece.EMPTY)
            {
                h ^= PIECE_SQUARE[piece][sq];
            }
        }

        if(board.sideToMove() == Color.BLACK)
        {
            h ^= BLACK_TO_MOVE;
        }

        int rights = board.castlingRights();
        if((rights & Board.CASTLE_WHITE_KING) != 0) h ^= CASTLING[0];
        if((rights & Board.CASTLE_WHITE_QUEEN) != 0) h ^= CASTLING[1];
        if((rights & Board.CASTLE_BLACK_KING) != 0) h ^= CASTLING[2];
        if((rights & Board.CASTLE_BLACK_QUEEN) != 0) h ^= CASTLING[3];

        int ep = board.enPassantSquare();
        if(ep != Square.NONE)
        {
            h ^= EN_PASSANT_FILE[Square.file(ep)];
        }

        int duck = board.duckSquare();
        if(duck != Square.NONE)
        {
            h ^= DUCK_SQUARE[duck];
        }

        return h;
    }

    // ---- Atom accessors — used by make/unmake for incremental updates ----

    public static long pieceSquare(byte piece, int square)
    {
        return PIECE_SQUARE[piece][square];
    }

    public static long blackToMove()
    {
        return BLACK_TO_MOVE;
    }

    public static long castling(int rightBit)
    {
        // rightBit is Board.CASTLE_WHITE_KING = 1, WHITE_QUEEN = 2, BLACK_KING = 4, BLACK_QUEEN = 8
        return switch(rightBit)
        {
            case Board.CASTLE_WHITE_KING -> CASTLING[0];
            case Board.CASTLE_WHITE_QUEEN -> CASTLING[1];
            case Board.CASTLE_BLACK_KING -> CASTLING[2];
            case Board.CASTLE_BLACK_QUEEN -> CASTLING[3];
            default -> throw new IllegalArgumentException("bad castling right: " + rightBit);
        };
    }

    /** XOR-in the difference between old and new castling rights. */
    public static long castlingDelta(int oldRights, int newRights)
    {
        long delta = 0L;
        int changed = oldRights ^ newRights;
        if((changed & Board.CASTLE_WHITE_KING) != 0) delta ^= CASTLING[0];
        if((changed & Board.CASTLE_WHITE_QUEEN) != 0) delta ^= CASTLING[1];
        if((changed & Board.CASTLE_BLACK_KING) != 0) delta ^= CASTLING[2];
        if((changed & Board.CASTLE_BLACK_QUEEN) != 0) delta ^= CASTLING[3];
        return delta;
    }

    public static long enPassantFile(int file) 
    {
        return EN_PASSANT_FILE[file];
    }

    public static long duckSquare(int square) 
    {
        return DUCK_SQUARE[square];
    }
}