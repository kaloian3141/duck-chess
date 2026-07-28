package com.duckchess.duck_chess.chess;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PieceTest {

    @Test
    void encodesAndDecodesEveryPieceCorrectly() 
    {
        for(Color color : Color.values()) 
        {
            for(PieceType type : PieceType.values()) 
            {
                byte encoded = Piece.encode(color, type);
                assertThat(Piece.colorOf(encoded)).isEqualTo(color);
                assertThat(Piece.typeOf(encoded)).isEqualTo(type);
                assertThat(Piece.isColor(encoded, color)).isTrue();
                assertThat(Piece.isColor(encoded, color.opponent())).isFalse();
            }
        }
    }

    @Test
    void constantsMatchEncoding() 
    {
        assertThat(Piece.WHITE_PAWN).isEqualTo(Piece.encode(Color.WHITE, PieceType.PAWN));
        assertThat(Piece.WHITE_KING).isEqualTo(Piece.encode(Color.WHITE, PieceType.KING));
        assertThat(Piece.BLACK_PAWN).isEqualTo(Piece.encode(Color.BLACK, PieceType.PAWN));
        assertThat(Piece.BLACK_KING).isEqualTo(Piece.encode(Color.BLACK, PieceType.KING));
    }

    @Test
    void emptyAndDuckHaveNoColorOrType() 
    {
        assertThat(Piece.isEmpty(Piece.EMPTY)).isTrue();
        assertThat(Piece.isDuck(Piece.DUCK)).isTrue();
        assertThat(Piece.isColor(Piece.EMPTY, Color.WHITE)).isFalse();
        assertThat(Piece.isColor(Piece.EMPTY, Color.BLACK)).isFalse();
        assertThat(Piece.isColor(Piece.DUCK, Color.WHITE)).isFalse();
        assertThat(Piece.isColor(Piece.DUCK, Color.BLACK)).isFalse();

        assertThatThrownBy(() -> Piece.colorOf(Piece.EMPTY))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Piece.typeOf(Piece.DUCK))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fenRoundTrips() 
    {
        for(Color color : Color.values()) 
        {
            for(PieceType type : PieceType.values()) 
            {
                byte encoded = Piece.encode(color, type);
                char fenChar = Piece.toFenChar(encoded);
                byte roundTripped = Piece.fromFenChar(fenChar);
                assertThat(roundTripped).isEqualTo(encoded);
            }
        }
    }

    @Test
    void fenParsingRejectsGarbage() 
    {
        assertThatThrownBy(() -> Piece.fromFenChar('x'))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Piece.fromFenChar('1'))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isWhiteAndIsBlackAreMutuallyExclusive() 
    {
        for(Color color : Color.values()) 
        {
            for(PieceType type : PieceType.values()) 
            {
                byte encoded = Piece.encode(color, type);
                boolean w = Piece.isWhite(encoded);
                boolean b = Piece.isBlack(encoded);
                assertThat(w ^ b).isTrue();
            }
        }
        assertThat(Piece.isWhite(Piece.EMPTY)).isFalse();
        assertThat(Piece.isBlack(Piece.EMPTY)).isFalse();
        assertThat(Piece.isWhite(Piece.DUCK)).isFalse();
        assertThat(Piece.isBlack(Piece.DUCK)).isFalse();
    }

    @Test
    void colorBitDistinguishesWhiteFromBlack() 
    {
        for(PieceType type : PieceType.values()) 
        {
            byte white = Piece.encode(Color.WHITE, type);
            byte black = Piece.encode(Color.BLACK, type);
            assertThat(black - white).isEqualTo(8);
            assertThat(white & 0b1000).isEqualTo(0);
            assertThat(black & 0b1000).isEqualTo(0b1000);
        }
    }

    @Test
    void typeIsPreservedAcrossColors() 
    {
        for(PieceType type : PieceType.values()) 
        {
            assertThat(Piece.typeOf(Piece.encode(Color.WHITE, type))).isEqualTo(type);
            assertThat(Piece.typeOf(Piece.encode(Color.BLACK, type))).isEqualTo(type);
        }
    }

    @Test
    void fenCharsAreCorrectCase() 
    {
        assertThat(Piece.toFenChar(Piece.WHITE_KING)).isEqualTo('K');
        assertThat(Piece.toFenChar(Piece.BLACK_KING)).isEqualTo('k');
        assertThat(Piece.toFenChar(Piece.WHITE_KNIGHT)).isEqualTo('N');
        assertThat(Piece.toFenChar(Piece.BLACK_KNIGHT)).isEqualTo('n');
    }

    @Test
    void emptyAndDuckHaveDistinctFenChars() 
    {
        assertThat(Piece.toFenChar(Piece.EMPTY)).isEqualTo('.');
        assertThat(Piece.toFenChar(Piece.DUCK)).isEqualTo('D');
        assertThat(Piece.fromFenChar('D')).isEqualTo(Piece.DUCK);
        assertThat(Piece.fromFenChar('d')).isEqualTo(Piece.DUCK);
    }

    @Test
    void isColorReturnsFalseForOppositeColor() 
    {
        assertThat(Piece.isColor(Piece.WHITE_PAWN, Color.BLACK)).isFalse();
        assertThat(Piece.isColor(Piece.BLACK_QUEEN, Color.WHITE)).isFalse();
    }

    @Test
    void allTwelvePieceConstantsAreDistinct() 
    {
        byte[] all = {
            Piece.WHITE_PAWN, Piece.WHITE_KNIGHT, Piece.WHITE_BISHOP,
            Piece.WHITE_ROOK, Piece.WHITE_QUEEN, Piece.WHITE_KING,
            Piece.BLACK_PAWN, Piece.BLACK_KNIGHT, Piece.BLACK_BISHOP,
            Piece.BLACK_ROOK, Piece.BLACK_QUEEN, Piece.BLACK_KING,
            Piece.EMPTY, Piece.DUCK
        };
        for(int i = 0; i < all.length; i++) 
        {
            for(int j = i + 1; j < all.length; j++) 
            {
                assertThat(all[i]).isNotEqualTo(all[j]);
            }
        }
    }
}