package com.duckchess.duck_chess.chess;
import java.util.Arrays;
/**
 * Chess position: piece placement + game state.
 *
 * Mutable — the intended lifecycle is make/unmake, not copy-per-move.
 * Board's byte[64] IS the source of truth; other fields cache game state.
 */
public final class Board 
{

    /** Piece byte at each square index. */
    private final byte[] squares = new byte[64];

    /** Whose turn is it. */
    private Color sideToMove;

    /** Castling rights bitmask: bit 0 = white kingside, 1 = white queenside,
     *  bit 2 = black kingside, 3 = black queenside. */
    private int castlingRights;

    public static final int CASTLE_WHITE_KING  = 1;
    public static final int CASTLE_WHITE_QUEEN = 2;
    public static final int CASTLE_BLACK_KING  = 4;
    public static final int CASTLE_BLACK_QUEEN = 8;
    public static final int CASTLE_ALL = 15;

    /** En-passant target square (0..63), or Square.NONE if none available. */
    private int enPassantSquare;

    /** Halfmove clock for the 50-move rule. */
    private int halfmoveClock;

    /** Fullmove number (starts at 1, incremented after black's move). */
    private int fullmoveNumber;

    /** Duck's square, or Square.NONE for standard chess. */
    private int duckSquare;

    public Board() 
    {
        clear();
    }


    public byte get(int square) 
    {
        return squares[square];
    }

    public void set(int square, byte piece) 
    {
        squares[square] = piece;
    }

    public Color sideToMove()
    { 
        return sideToMove; 
    }

    public int castlingRights()
    { 
        return castlingRights;
    }
    public int enPassantSquare()
    {
        return enPassantSquare;
    }
    public int halfmoveClock()
    {
        return halfmoveClock;
    }
    public int fullmoveNumber()
    {
        return fullmoveNumber;
    }
    public int duckSquare()
    {
        return duckSquare;
    }

    public void setSideToMove(Color color)
    { 
        this.sideToMove = color;
    }
    public void setCastlingRights(int rights)
    { 
        this.castlingRights = rights;
    }
    public void setEnPassantSquare(int square)
    {
        this.enPassantSquare = square;
    }
    public void setHalfmoveClock(int clock)
    { 
        this.halfmoveClock = clock; 
    }
    public void setFullmoveNumber(int number)
    { 
        this.fullmoveNumber = number; 
    }
    public void setDuckSquare(int square)
    { 
        this.duckSquare = square;
    }

    public boolean canCastle(int right) 
    {
        return (castlingRights & right) != 0;
    }

    /** Locate the king of the given color, or -1 if not on the board. */
    public int kingSquare(Color color)
    {
        byte target = (color == Color.WHITE) ? Piece.WHITE_KING : Piece.BLACK_KING;
        for(int sq = 0; sq < 64; sq++) 
        {
            if(squares[sq] == target) 
                return sq;
        }
        return -1;
    }


    public void clear() 
    {
        Arrays.fill(squares, Piece.EMPTY);
        sideToMove = Color.WHITE;
        castlingRights = 0;
        enPassantSquare = Square.NONE;
        halfmoveClock = 0;
        fullmoveNumber = 1;
        duckSquare = Square.NONE;
    }

    public static Board startingPosition() 
    {
        return Fen.parse(Fen.STARTING);
    }

    public static Board fromFen(String fen)
    {
        return Fen.parse(fen);
    }

    public String toFen() 
    {
        return Fen.serialize(this);
    }

    public Board copy() 
    {
        Board b = new Board();
        System.arraycopy(this.squares, 0, b.squares, 0, 64);
        b.sideToMove = this.sideToMove;
        b.castlingRights = this.castlingRights;
        b.enPassantSquare = this.enPassantSquare;
        b.halfmoveClock = this.halfmoveClock;
        b.fullmoveNumber = this.fullmoveNumber;
        b.duckSquare = this.duckSquare;
        return b;
    }

    @Override
    public String toString() 
    {
        StringBuilder sb = new StringBuilder();
        for(int rank = 7; rank >= 0; rank--) 
        {
            sb.append(rank + 1).append(' ');
            for(int file = 0; file < 8; file++) 
            {
                sb.append(Piece.toFenChar(squares[Square.of(file, rank)]));
                sb.append(' ');
            }
            sb.append('\n');
        }
        sb.append("  a b c d e f g h\n");
        sb.append("STM: ").append(sideToMove);
        sb.append("  Castle: ").append(castlingRights);
        sb.append("  EP: ").append(Square.name(enPassantSquare));
        sb.append("  Clocks: ").append(halfmoveClock).append('/').append(fullmoveNumber);
        if(duckSquare != Square.NONE) 
        {
            sb.append("  Duck: ").append(Square.name(duckSquare));
        }
        return sb.toString();
    }
}