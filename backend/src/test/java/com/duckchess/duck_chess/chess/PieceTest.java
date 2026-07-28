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
}