package com.duckchess.duck_chess.chess;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SquareTest 
{

    @Test
    void indexEndpointsMatchAlgebraic() 
    {
        assertThat(Square.A1).isEqualTo(0);
        assertThat(Square.H1).isEqualTo(7);
        assertThat(Square.A8).isEqualTo(56);
        assertThat(Square.H8).isEqualTo(63);
    }

    @Test
    void fileAndRankExtractionRoundTrip() 
    {
        for(int sq = 0; sq < 64; sq++) 
        {
            int file = Square.file(sq);
            int rank = Square.rank(sq);
            assertThat(Square.of(file, rank)).isEqualTo(sq);
            assertThat(file).isBetween(0, 7);
            assertThat(rank).isBetween(0, 7);
        }
    }

    @Test
    void nameAndFromNameRoundTrip() 
    {
        for(int sq = 0; sq < 64; sq++) 
        {
            String name = Square.name(sq);
            assertThat(name).hasSize(2);
            assertThat(Square.fromName(name)).isEqualTo(sq);
        }
    }

    @Test
    void wellKnownNames() 
    {
        assertThat(Square.name(Square.A1)).isEqualTo("a1");
        assertThat(Square.name(Square.E4)).isEqualTo("e4");
        assertThat(Square.name(Square.H8)).isEqualTo("h8");
        assertThat(Square.name(Square.NONE)).isEqualTo("-");
    }

    @Test
    void fromNameRejectsGarbage() 
    {
        assertThatThrownBy(() -> Square.fromName("z9"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Square.fromName("a"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Square.fromName(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isValidChecksRange() {
        assertThat(Square.isValid(0)).isTrue();
        assertThat(Square.isValid(63)).isTrue();
        assertThat(Square.isValid(-1)).isFalse();
        assertThat(Square.isValid(64)).isFalse();
    }
}