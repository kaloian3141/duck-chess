package com.duckchess.duck_chess.chess;

/**
 * Move encoding — everything about a move packed into a single int.
 *
 * Bit layout (bits 0..21 used, bits 22..31 unused):
 *   bits  0..5   from-square (0..63)      — 6 bits
 *   bits  6..11  to-square   (0..63)      — 6 bits
 *   bits 12..14  promotion piece type     — 3 bits (0 = no promotion)
 *   bits 15..18  flags                    — 4 bits
 *
 * Promotion codes (bits 12..14):
 *   0 = none, 1 = knight, 2 = bishop, 3 = rook, 4 = queen
 *   (king/pawn can't be promotion targets, so no code for them)
 *
 * Flag bits (bits 15..18):
 *   bit 15 = capture
 *   bit 16 = double pawn push (from rank 2 to 4, or 7 to 5)
 *   bit 17 = en passant capture
 *   bit 18 = castling
 */
public final class Move {

    private Move() {}

    // Bit widths
    private static final int TO_SHIFT      = 6;
    private static final int PROMO_SHIFT   = 12;
    private static final int FLAGS_SHIFT   = 15;

    private static final int SQUARE_MASK   = 0x3F;    // 6 bits
    private static final int PROMO_MASK    = 0x7;     // 3 bits

    // Promotion codes
    public static final int NO_PROMO       = 0;
    public static final int PROMO_KNIGHT   = 1;
    public static final int PROMO_BISHOP   = 2;
    public static final int PROMO_ROOK     = 3;
    public static final int PROMO_QUEEN    = 4;

    // Flag bits (already shifted)
    public static final int FLAG_CAPTURE   = 1 << FLAGS_SHIFT;
    public static final int FLAG_DOUBLE    = 1 << (FLAGS_SHIFT + 1);
    public static final int FLAG_EP        = 1 << (FLAGS_SHIFT + 2);
    public static final int FLAG_CASTLE    = 1 << (FLAGS_SHIFT + 3);

    public static final int NULL_MOVE      = 0;

    // ---- Encoders ----

    public static int quiet(int from, int to) 
    {
        return from | (to << TO_SHIFT);
    }

    public static int capture(int from, int to) 
    {
        return from | (to << TO_SHIFT) | FLAG_CAPTURE;
    }

    public static int doublePush(int from, int to) 
    {
        return from | (to << TO_SHIFT) | FLAG_DOUBLE;
    }

    public static int enPassant(int from, int to) 
    {
        return from | (to << TO_SHIFT) | FLAG_CAPTURE | FLAG_EP;
    }

    public static int castle(int from, int to) 
    {
        return from | (to << TO_SHIFT) | FLAG_CASTLE;
    }

    public static int promotion(int from, int to, int promoCode, boolean isCapture) 
    {
        int move = from | (to << TO_SHIFT) | (promoCode << PROMO_SHIFT);
        if(isCapture) move |= FLAG_CAPTURE;
        return move;
    }

    // ---- Decoders ----

    public static int from(int move) 
    {
        return move & SQUARE_MASK;
    }

    public static int to(int move) 
    {
        return (move >>> TO_SHIFT) & SQUARE_MASK;
    }

    public static int promotion(int move)
    {
        return (move >>> PROMO_SHIFT) & PROMO_MASK;
    }

    public static boolean isCapture(int move) 
    {
        return (move & FLAG_CAPTURE) != 0;
    }

    public static boolean isDoublePush(int move) 
    {
        return (move & FLAG_DOUBLE) != 0;
    }

    public static boolean isEnPassant(int move)
    {
        return (move & FLAG_EP) != 0;
    }

    public static boolean isCastle(int move) 
    {
        return (move & FLAG_CASTLE) != 0;
    }

    public static boolean isPromotion(int move) 
    {
        return promotion(move) != NO_PROMO;
    }

    public static String toUci(int move) 
    {
        if(move == NULL_MOVE) return "0000";
        String s = Square.name(from(move)) + Square.name(to(move));
        if(isPromotion(move)) 
        {
            s += promoLetter(promotion(move));
        }
        return s;
    }

    private static char promoLetter(int code) 
    {
        return switch(code) 
        {
            case PROMO_KNIGHT -> 'n';
            case PROMO_BISHOP -> 'b';
            case PROMO_ROOK   -> 'r';
            case PROMO_QUEEN  -> 'q';
            default -> throw new IllegalArgumentException("bad promo code: " + code);
        };
    }

    public static int fromUci(String uci) 
    {
        if(uci == null || uci.length() < 4 || uci.length() > 5) 
        {
            throw new IllegalArgumentException("invalid UCI move: " + uci);
        }
        int from = Square.fromName(uci.substring(0, 2));
        int to   = Square.fromName(uci.substring(2, 4));
        int move = from | (to << TO_SHIFT);
        if(uci.length() == 5) 
        {
            int promo = switch (uci.charAt(4)) {
                case 'n' -> PROMO_KNIGHT;
                case 'b' -> PROMO_BISHOP;
                case 'r' -> PROMO_ROOK;
                case 'q' -> PROMO_QUEEN;
                default -> throw new IllegalArgumentException("bad promo letter: " + uci);
            };
            move |= (promo << PROMO_SHIFT);
        }
        return move;
    }
}