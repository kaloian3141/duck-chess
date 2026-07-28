package com.duckchess.duck_chess.chess;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoardTest 
{

    @Test
    void newBoardIsEmpty() 
    {
        Board b = new Board();
        for(int sq = 0; sq < 64; sq++) 
        {
            assertThat(b.get(sq)).isEqualTo(Piece.EMPTY);
        }
        assertThat(b.sideToMove()).isEqualTo(Color.WHITE);
        assertThat(b.enPassantSquare()).isEqualTo(Square.NONE);
        assertThat(b.duckSquare()).isEqualTo(Square.NONE);
    }

    @Test
    void startingPositionIsSetup() 
    {
        Board b = Board.startingPosition();
        assertThat(b.get(Square.E1)).isEqualTo(Piece.WHITE_KING);
        assertThat(b.get(Square.E8)).isEqualTo(Piece.BLACK_KING);
        assertThat(b.sideToMove()).isEqualTo(Color.WHITE);
    }

    @Test
    void copyIsIndependent() 
    {
        Board original = Board.startingPosition();
        Board copy = original.copy();

        original.set(Square.E4, Piece.WHITE_PAWN);
        original.set(Square.E2, Piece.EMPTY);
        original.setSideToMove(Color.BLACK);

        assertThat(copy.get(Square.E4)).isEqualTo(Piece.EMPTY);
        assertThat(copy.get(Square.E2)).isEqualTo(Piece.WHITE_PAWN);
        assertThat(copy.sideToMove()).isEqualTo(Color.WHITE);
    }

    @Test
    void clearResetsEverything() 
    {
        Board b = Board.startingPosition();
        b.setDuckSquare(Square.D4);
        b.setHalfmoveClock(42);

        b.clear();

        for(int sq = 0; sq < 64; sq++) 
        {
            assertThat(b.get(sq)).isEqualTo(Piece.EMPTY);
        }
        assertThat(b.duckSquare()).isEqualTo(Square.NONE);
        assertThat(b.halfmoveClock()).isEqualTo(0);
        assertThat(b.fullmoveNumber()).isEqualTo(1);
    }
    @Test
    void kingSquareReturnsMinusOneWhenNoKing() 
    {
        Board b = new Board();
        assertThat(b.kingSquare(Color.WHITE)).isEqualTo(-1);
        assertThat(b.kingSquare(Color.BLACK)).isEqualTo(-1);
    }

    @Test
    void canCastleReflectsRights() 
    {
        Board b = Board.startingPosition();
        assertThat(b.canCastle(Board.CASTLE_WHITE_KING)).isTrue();
        assertThat(b.canCastle(Board.CASTLE_WHITE_QUEEN)).isTrue();
        assertThat(b.canCastle(Board.CASTLE_BLACK_KING)).isTrue();
        assertThat(b.canCastle(Board.CASTLE_BLACK_QUEEN)).isTrue();

        b.setCastlingRights(Board.CASTLE_WHITE_KING);
        assertThat(b.canCastle(Board.CASTLE_WHITE_KING)).isTrue();
        assertThat(b.canCastle(Board.CASTLE_WHITE_QUEEN)).isFalse();
        assertThat(b.canCastle(Board.CASTLE_BLACK_KING)).isFalse();
    }

    @Test
    void duckSquareCanBeSetAndCleared() 
    {
        Board b = Board.startingPosition();
        assertThat(b.duckSquare()).isEqualTo(Square.NONE);

        b.setDuckSquare(Square.D4);
        assertThat(b.duckSquare()).isEqualTo(Square.D4);

        b.setDuckSquare(Square.NONE);
        assertThat(b.duckSquare()).isEqualTo(Square.NONE);
    }

    @Test
    void copyPreservesAllState() 
    {
        Board original = Board.startingPosition();
        original.setDuckSquare(Square.E4);
        original.setEnPassantSquare(Square.D3);
        original.setCastlingRights(Board.CASTLE_WHITE_KING);
        original.setHalfmoveClock(15);
        original.setFullmoveNumber(30);
        original.setSideToMove(Color.BLACK);

        Board copy = original.copy();

        assertThat(copy.duckSquare()).isEqualTo(Square.E4);
        assertThat(copy.enPassantSquare()).isEqualTo(Square.D3);
        assertThat(copy.castlingRights()).isEqualTo(Board.CASTLE_WHITE_KING);
        assertThat(copy.halfmoveClock()).isEqualTo(15);
        assertThat(copy.fullmoveNumber()).isEqualTo(30);
        assertThat(copy.sideToMove()).isEqualTo(Color.BLACK);
    }

    @Test
    void toStringContainsBoardDiagram() 
    {
        Board b = Board.startingPosition();
        String s = b.toString();
        assertThat(s).contains("R N B Q K B N R");
        assertThat(s).contains("r n b q k b n r");
        assertThat(s).contains("a b c d e f g h");
        assertThat(s).contains("STM: WHITE");
    }

    @Test
    void toStringMentionsDuckWhenPresent() 
    {
        Board b = Board.startingPosition();
        assertThat(b.toString()).doesNotContain("Duck");
        b.setDuckSquare(Square.D4);
        assertThat(b.toString()).contains("Duck: d4");
    }
}