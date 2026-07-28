package com.duckchess.duck_chess.chess;

/**
 * Piece encoding used by the board's byte[64].
 *
 * Byte layout:
 *   0        = empty square
 *   1..6     = white piece (PAWN..KING in enum order + 1)
 *   9..14    = black piece (PAWN..KING in enum order + 9)
 *   15       = duck (variant marker, ignored by standard chess)
 *
 * Bit layout: color bit at position 3, type in low 3 bits.
 *   bit 3 = 0 -> white, bit 3 = 1 -> black
 *   bits 0..2 = piece type ordinal + 1 (so 0 means empty)
 */
public final class Piece {

    private Piece() {}

    public static final byte EMPTY = 0;

    public static final byte WHITE_PAWN   = 1;
    public static final byte WHITE_KNIGHT = 2;
    public static final byte WHITE_BISHOP = 3;
    public static final byte WHITE_ROOK   = 4;
    public static final byte WHITE_QUEEN  = 5;
    public static final byte WHITE_KING   = 6;

    public static final byte BLACK_PAWN   = 9;
    public static final byte BLACK_KNIGHT = 10;
    public static final byte BLACK_BISHOP = 11;
    public static final byte BLACK_ROOK   = 12;
    public static final byte BLACK_QUEEN  = 13;
    public static final byte BLACK_KING   = 14;

    public static final byte DUCK = 15;

    private static final int COLOR_BIT = 0b1000;
    private static final int TYPE_MASK = 0b0111;

    public static byte encode(Color color, PieceType type) 
    {
        int typeBits = type.ordinal() + 1;
        int colorBit = (color == Color.BLACK) ? COLOR_BIT : 0;
        return(byte) 
            (colorBit | typeBits);
    }

    public static boolean isEmpty(byte piece) 
    {
        return piece == EMPTY;
    }

    public static boolean isDuck(byte piece) 
    {
        return piece == DUCK;
    }

    public static Color colorOf(byte piece) 
    {
        if(piece == EMPTY || piece == DUCK)
        {
            throw new IllegalArgumentException("no color for empty/duck");
        }
        return(piece & COLOR_BIT) != 0 ? Color.BLACK : Color.WHITE;
    }

    public static PieceType typeOf(byte piece) 
    {
        if(piece == EMPTY || piece == DUCK) 
        {
            throw new IllegalArgumentException("no type for empty/duck");
        }
        int typeBits = (piece & TYPE_MASK) - 1;
        return PieceType.values()[typeBits];
    }

    public static boolean isWhite(byte piece) 
    {
        return piece != EMPTY && piece != DUCK && (piece & COLOR_BIT) == 0;
    }

    public static boolean isBlack(byte piece) 
    {
        return piece != EMPTY && piece != DUCK && (piece & COLOR_BIT) != 0;
    }

    public static boolean isColor(byte piece, Color color) 
    {
        return color == Color.WHITE ? isWhite(piece) : isBlack(piece);
    }

    public static char toFenChar(byte piece)
    {
        if (piece == EMPTY) return '.';
        if (piece == DUCK) return 'D';
        char letter = typeOf(piece).letter();
        return isBlack(piece) ? Character.toLowerCase(letter) : letter;
    }

    public static byte fromFenChar(char c) 
    {
        return switch(c)
        {
            case 'P' -> WHITE_PAWN;
            case 'N' -> WHITE_KNIGHT;
            case 'B' -> WHITE_BISHOP;
            case 'R' -> WHITE_ROOK;
            case 'Q' -> WHITE_QUEEN;
            case 'K' -> WHITE_KING;
            case 'p' -> BLACK_PAWN;
            case 'n' -> BLACK_KNIGHT;
            case 'b' -> BLACK_BISHOP;
            case 'r' -> BLACK_ROOK;
            case 'q' -> BLACK_QUEEN;
            case 'k' -> BLACK_KING;
            case 'D', 'd' -> DUCK;
            default -> throw new IllegalArgumentException("invalid FEN char: " + c);
        };
    }
}