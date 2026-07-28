package com.duckchess.duck_chess.chess.movegen;

import com.duckchess.duck_chess.chess.Board;
import com.duckchess.duck_chess.chess.Color;
import com.duckchess.duck_chess.chess.Fen;
import com.duckchess.duck_chess.chess.Piece;
import com.duckchess.duck_chess.chess.Square;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AttackDetectorTest 
{

    @Test
    void startingPositionHasNoChecks() 
    {
        Board b = Board.startingPosition();
        assertThat(AttackDetector.isInCheck(b, Color.WHITE)).isFalse();
        assertThat(AttackDetector.isInCheck(b, Color.BLACK)).isFalse();
    }

    @Test
    void queenChecksAlongDiagonal() 
    {
        // White queen on h4, black king on e7 — queen attacks e7 via h4-e7? No, that's not a line.
        Board b = Fen.parse("4k3/8/8/7Q/8/8/8/4K3 b - - 0 1");
        assertThat(AttackDetector.isInCheck(b, Color.BLACK)).isTrue();
        assertThat(AttackDetector.isInCheck(b, Color.WHITE)).isFalse();
    }

    @Test
    void rookChecksAlongFile() 
    {
        Board b = Fen.parse("4k3/8/8/8/8/8/8/4K2R b - - 0 1");
        assertThat(AttackDetector.isInCheck(b, Color.BLACK)).isFalse();
        Fen.parse("4k3/8/8/8/8/8/8/4K1R1 w - - 0 1");
        Board b3 = Fen.parse("4k3/8/8/8/8/8/8/4R2K b - - 0 1");
        assertThat(AttackDetector.isInCheck(b3, Color.BLACK)).isTrue();
    }

    @Test
    void knightChecksIgnoringBlockers() 
    {
        // White knight on f6 attacks e8
        Board b = Fen.parse("4k3/8/5N2/8/8/8/8/4K3 b - - 0 1");
        assertThat(AttackDetector.isInCheck(b, Color.BLACK)).isTrue();
    }

    @Test
    void pawnChecksDiagonally() 
    {
        // White pawn on d7 attacks e8 (king)
        Board b = Fen.parse("4k3/3P4/8/8/8/8/8/4K3 b - - 0 1");
        assertThat(AttackDetector.isInCheck(b, Color.BLACK)).isTrue();
    }

    @Test
    void pawnDoesNotCheckStraightAhead()
    {
        // White pawn on e7 in front of black king on e8 — no check, pawns don't attack forward
        Board b = Fen.parse("4k3/4P3/8/8/8/8/8/4K3 b - - 0 1");
        assertThat(AttackDetector.isInCheck(b, Color.BLACK)).isFalse();
    }

    @Test
    void blockedSliderDoesNotCheck() 
    {
        // White rook on e1, black king on e8, but a black pawn on e5 blocks
        Board b = Fen.parse("4k3/8/8/4p3/8/8/8/4R2K b - - 0 1");
        assertThat(AttackDetector.isInCheck(b, Color.BLACK)).isFalse();
    }

    @Test
    void duckBlocksSliderAttacks() 
    {
        // Same rook-vs-king file, but with a duck blocking on e5
        Board b = Fen.parse("4k3/8/8/8/8/8/8/4R2K b - - 0 1");
        b.setDuckSquare(Square.E5);
        b.set(Square.E5, Piece.DUCK);
        assertThat(AttackDetector.isInCheck(b, Color.BLACK)).isFalse();
    }

    @Test
    void kingAttacksAdjacentSquares() 
    {
        // White king on e1, target d2 — adjacent, so attacked
        Board b = Fen.parse("8/8/8/8/8/8/8/4K3 w - - 0 1");
        assertThat(AttackDetector.isAttacked(b, Square.D2, Color.WHITE)).isTrue();
        assertThat(AttackDetector.isAttacked(b, Square.E2, Color.WHITE)).isTrue();
        assertThat(AttackDetector.isAttacked(b, Square.F2, Color.WHITE)).isTrue();
        assertThat(AttackDetector.isAttacked(b, Square.E3, Color.WHITE)).isFalse();
    }

   @Test
    void bishopAndQueenAttackDiagonally() {
        // White bishop on a1 attacks h8 through empty diagonal
        Board b = Fen.parse("7k/8/8/8/8/8/8/B3K3 w - - 0 1");
        assertThat(AttackDetector.isAttacked(b, Square.H8, Color.WHITE)).isTrue();
        assertThat(AttackDetector.isAttacked(b, Square.D4, Color.WHITE)).isTrue();

        // Blocker on e5 (on the a1-h8 diagonal) breaks the attack on h8
        Board b2 = Fen.parse("7k/8/8/4p3/8/8/8/B3K3 w - - 0 1");
        assertThat(AttackDetector.isAttacked(b2, Square.H8, Color.WHITE)).isFalse();
        assertThat(AttackDetector.isAttacked(b2, Square.E5, Color.WHITE)).isTrue();
        assertThat(AttackDetector.isAttacked(b2, Square.F6, Color.WHITE)).isFalse();
    }
}