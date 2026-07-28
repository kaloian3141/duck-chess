package com.duckchess.duck_chess.chess;

/**
 * FEN parsing and serialization.
 *
 * Standard chess FEN has 6 fields separated by spaces:
 *   1. Piece placement (rank 8 down to rank 1, / between ranks)
 *   2. Side to move ("w" or "b")
 *   3. Castling rights ("KQkq" or "-")
 *   4. En passant target square (e.g. "e3" or "-")
 *   5. Halfmove clock
 *   6. Fullmove number
 */
public final class Fen 
{

    private Fen() {}

    public static final String STARTING =
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public static Board parse(String fen) 
    {
        if(fen == null) 
            throw new IllegalArgumentException("null FEN");
        String[] parts = fen.trim().split("\\s+");
        if(parts.length < 4 || parts.length > 6)
        {
            throw new IllegalArgumentException("FEN must have 4-6 fields, got " + parts.length);
        }

        Board b = new Board();
        parsePlacement(parts[0], b);
        b.setSideToMove(parts[1].equals("w") ? Color.WHITE : Color.BLACK);
        b.setCastlingRights(parseCastling(parts[2]));
        b.setEnPassantSquare(parts[3].equals("-") ? Square.NONE : Square.fromName(parts[3]));
        b.setHalfmoveClock(parts.length >= 5 ? Integer.parseInt(parts[4]) : 0);
        b.setFullmoveNumber(parts.length >= 6 ? Integer.parseInt(parts[5]) : 1);
        return b;
    }

    private static void parsePlacement(String placement, Board board)
    {
        String[] ranks = placement.split("/");
        if(ranks.length != 8)
        {
            throw new IllegalArgumentException("placement must have 8 ranks");
        }
        for(int i = 0; i < 8; i++)
        {
            int rank = 7 - i;
            int file = 0;
            for(char c : ranks[i].toCharArray())
            {
                if(Character.isDigit(c)) 
                {
                    file += (c - '0');
                } 
                else 
                {
                    if(file >= 8) 
                        throw new IllegalArgumentException("rank overflow: " + ranks[i]);
                    board.set(Square.of(file, rank), Piece.fromFenChar(c));
                    file++;
                }
            }
            if(file != 8) 
                throw new IllegalArgumentException("rank underflow: " + ranks[i]);
        }
    }

    private static int parseCastling(String s)
    {
        if(s.equals("-")) 
            return 0;
        int rights = 0;
        for(char c : s.toCharArray())
            {
            switch(c)
            {
                case 'K' -> rights |= Board.CASTLE_WHITE_KING;
                case 'Q' -> rights |= Board.CASTLE_WHITE_QUEEN;
                case 'k' -> rights |= Board.CASTLE_BLACK_KING;
                case 'q' -> rights |= Board.CASTLE_BLACK_QUEEN;
                default -> throw new IllegalArgumentException("bad castling char: " + c);
            }
        }
        return rights;
    }

    public static String serialize(Board board)
    {
        StringBuilder sb = new StringBuilder();

        for(int rank = 7; rank >= 0; rank--)
        {
            int emptyRun = 0;
            for(int file = 0; file < 8; file++)
            {
                byte piece = board.get(Square.of(file, rank));
                if(piece == Piece.EMPTY)
                {
                    emptyRun++;
                } 
                else 
                {
                    if(emptyRun > 0) 
                    { 
                        sb.append(emptyRun); emptyRun = 0; 
                    }
                    sb.append(Piece.toFenChar(piece));
                }
            }
            if(emptyRun > 0) 
                sb.append(emptyRun);
            if(rank > 0) 
                sb.append('/');
        }

        sb.append(' ').append(board.sideToMove() == Color.WHITE ? 'w' : 'b');
        sb.append(' ').append(serializeCastling(board.castlingRights()));
        sb.append(' ').append(Square.name(board.enPassantSquare()));
        sb.append(' ').append(board.halfmoveClock());
        sb.append(' ').append(board.fullmoveNumber());

        return sb.toString();
    }

    private static String serializeCastling(int rights) 
    {
        if(rights == 0) 
            return "-";
        StringBuilder sb = new StringBuilder();
        if((rights & Board.CASTLE_WHITE_KING) != 0) 
            sb.append('K');
        if((rights & Board.CASTLE_WHITE_QUEEN) != 0) 
            sb.append('Q');
        if((rights & Board.CASTLE_BLACK_KING) != 0) 
            sb.append('k');
        if((rights & Board.CASTLE_BLACK_QUEEN) != 0) 
            sb.append('q');
        return sb.toString();
    }
}