package com.duckchess.duck_chess.chess;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoveTest 
{

    @Test
    void quietMoveRoundTrips() 
    {
        int move = Move.quiet(Square.E2, Square.E4);
        assertThat(Move.from(move)).isEqualTo(Square.E2);
        assertThat(Move.to(move)).isEqualTo(Square.E4);
        assertThat(Move.isCapture(move)).isFalse();
        assertThat(Move.isPromotion(move)).isFalse();
        assertThat(Move.isCastle(move)).isFalse();
    }

    @Test
    void captureFlagIsSet() 
    {
        int move = Move.capture(Square.E4, Square.D5);
        assertThat(Move.isCapture(move)).isTrue();
        assertThat(Move.from(move)).isEqualTo(Square.E4);
        assertThat(Move.to(move)).isEqualTo(Square.D5);
    }

    @Test
    void enPassantIsAlsoACapture()
    {
        int move = Move.enPassant(Square.E5, Square.D6);
        assertThat(Move.isEnPassant(move)).isTrue();
        assertThat(Move.isCapture(move)).isTrue();
    }

    @Test
    void promotionRoundTrips() 
    {
        int move = Move.promotion(Square.E7, Square.E8, Move.PROMO_QUEEN, false);
        assertThat(Move.from(move)).isEqualTo(Square.E7);
        assertThat(Move.to(move)).isEqualTo(Square.E8);
        assertThat(Move.isPromotion(move)).isTrue();
        assertThat(Move.promotion(move)).isEqualTo(Move.PROMO_QUEEN);
        assertThat(Move.isCapture(move)).isFalse();
    }

    @Test
    void capturePromotionHasBothFlags() 
    {
        int move = Move.promotion(Square.E7, Square.D8, Move.PROMO_KNIGHT, true);
        assertThat(Move.isPromotion(move)).isTrue();
        assertThat(Move.isCapture(move)).isTrue();
        assertThat(Move.promotion(move)).isEqualTo(Move.PROMO_KNIGHT);
    }

    @Test
    void castleFlagIsSet() 
    {
        int move = Move.castle(Square.E1, Square.G1);
        assertThat(Move.isCastle(move)).isTrue();
        assertThat(Move.from(move)).isEqualTo(Square.E1);
        assertThat(Move.to(move)).isEqualTo(Square.G1);
    }

    @Test
    void doublePushIsMarked() 
    {
        int move = Move.doublePush(Square.E2, Square.E4);
        assertThat(Move.isDoublePush(move)).isTrue();
    }

    @Test
    void uciRoundTripQuiet() 
    {
        int move = Move.quiet(Square.E2, Square.E4);
        assertThat(Move.toUci(move)).isEqualTo("e2e4");
        int parsed = Move.fromUci("e2e4");
        assertThat(Move.from(parsed)).isEqualTo(Square.E2);
        assertThat(Move.to(parsed)).isEqualTo(Square.E4);
    }

    @Test
    void uciRoundTripPromotion() 
    {
        int move = Move.promotion(Square.E7, Square.E8, Move.PROMO_QUEEN, false);
        assertThat(Move.toUci(move)).isEqualTo("e7e8q");
        int parsed = Move.fromUci("e7e8q");
        assertThat(Move.from(parsed)).isEqualTo(Square.E7);
        assertThat(Move.to(parsed)).isEqualTo(Square.E8);
        assertThat(Move.promotion(parsed)).isEqualTo(Move.PROMO_QUEEN);
    }

    @Test
    void uciRejectsGarbage() 
    {
        assertThatThrownBy(() -> Move.fromUci("xxxx"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Move.fromUci("e2"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void allSquareCombinationsFitInEncoding()
    {
        for(int from = 0; from < 64; from++)
        {
            for(int to = 0; to < 64; to++)
            {
                int move = Move.quiet(from, to);
                assertThat(Move.from(move)).isEqualTo(from);
                assertThat(Move.to(move)).isEqualTo(to);
            }
        }
    }

    @Test
    void doesNotSetFlagsOnQuietMove() 
    {
        int move = Move.quiet(Square.G1, Square.F3);
        assertThat(Move.isCapture(move)).isFalse();
        assertThat(Move.isCastle(move)).isFalse();
        assertThat(Move.isEnPassant(move)).isFalse();
        assertThat(Move.isDoublePush(move)).isFalse();
        assertThat(Move.isPromotion(move)).isFalse();
    }

    @Test
    void allPromotionTypesRoundTrip() 
    {
        int[] codes = {Move.PROMO_KNIGHT, Move.PROMO_BISHOP, Move.PROMO_ROOK, Move.PROMO_QUEEN};
        for(int code : codes)
        {
            int move = Move.promotion(Square.A7, Square.A8, code, false);
            assertThat(Move.promotion(move)).isEqualTo(code);
            assertThat(Move.isPromotion(move)).isTrue();
        }
    }

    @Test
    void promotionZeroMeansNoPromotion() 
    {
        int move = Move.quiet(Square.E2, Square.E4);
        assertThat(Move.promotion(move)).isEqualTo(Move.NO_PROMO);
        assertThat(Move.isPromotion(move)).isFalse();
    }

    @Test
    void flagsDoNotBleedIntoSquareBits()
    {
        int move = Square.E1
            | (Square.G1 << 6)
            | Move.FLAG_CAPTURE
            | Move.FLAG_CASTLE
            | Move.FLAG_EP
            | Move.FLAG_DOUBLE;
        assertThat(Move.from(move)).isEqualTo(Square.E1);
        assertThat(Move.to(move)).isEqualTo(Square.G1);
    }

    @Test
    void nullMoveHasSpecialUci() 
    {
        assertThat(Move.toUci(Move.NULL_MOVE)).isEqualTo("0000");
    }

    @Test
    void uciWithoutPromotionForNonPromotionMoves() 
    {
        int move = Move.quiet(Square.E2, Square.E4);
        assertThat(Move.toUci(move)).hasSize(4);
        int move2 = Move.castle(Square.E1, Square.G1);
        assertThat(Move.toUci(move2)).hasSize(4);
    }

    @Test
    void fromUciDoesNotSetFlags() 
    {
        int parsed = Move.fromUci("e2e4");
        assertThat(Move.isCapture(parsed)).isFalse();
        assertThat(Move.isDoublePush(parsed)).isFalse();
    }

    @Test
    void extremeSquareCombinations() 
    {
        int move = Move.quiet(Square.A1, Square.H8);
        assertThat(Move.from(move)).isEqualTo(Square.A1);
        assertThat(Move.to(move)).isEqualTo(Square.H8);
        int move2 = Move.quiet(Square.H8, Square.A1);
        assertThat(Move.from(move2)).isEqualTo(Square.H8);
        assertThat(Move.to(move2)).isEqualTo(Square.A1);
    }
}