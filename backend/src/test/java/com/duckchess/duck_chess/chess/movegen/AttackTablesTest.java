package com.duckchess.duck_chess.chess.movegen;

import com.duckchess.duck_chess.chess.Square;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AttackTablesTest 
{

    @Test
    void knightInCenterHasEightMoves() 
    {
        assertThat(AttackTables.KNIGHT_ATTACKS[Square.E4]).hasSize(8);
    }

    @Test
    void knightInCornerHasTwoMoves() 
    {
        int[] a1 = AttackTables.KNIGHT_ATTACKS[Square.A1];
        assertThat(a1).hasSize(2);
        assertThat(a1).contains(Square.B3, Square.C2);
    }

    @Test
    void knightOnEdgeHasFourMoves() 
    {
        int[] a4 = AttackTables.KNIGHT_ATTACKS[Square.A4];
        assertThat(a4).hasSize(4);
        assertThat(a4).contains(Square.B2, Square.C3, Square.C5, Square.B6);
    }

    @Test
    void kingInCenterHasEightMoves() 
    {
        assertThat(AttackTables.KING_ATTACKS[Square.E4]).hasSize(8);
    }

    @Test
    void kingInCornerHasThreeMoves() 
    {
        int[] a1 = AttackTables.KING_ATTACKS[Square.A1];
        assertThat(a1).hasSize(3);
        assertThat(a1).contains(Square.A2, Square.B1, Square.B2);
    }

    @Test
    void whitePawnAttacksDiagonallyForward() 
    {
        int[] fromE4 = AttackTables.WHITE_PAWN_ATTACKS[Square.E4];
        assertThat(fromE4).hasSize(2);
        assertThat(fromE4).contains(Square.D5, Square.F5);
    }

    @Test
    void blackPawnAttacksDiagonallyForward() 
    {
        int[] fromE5 = AttackTables.BLACK_PAWN_ATTACKS[Square.E5];
        assertThat(fromE5).hasSize(2);
        assertThat(fromE5).contains(Square.D4, Square.F4);
    }

    @Test
    void whitePawnOnEighthRankHasNoAttacks() 
    {
        assertThat(AttackTables.WHITE_PAWN_ATTACKS[Square.E8]).isEmpty();
    }

    @Test
    void blackPawnOnFirstRankHasNoAttacks() 
    {
        assertThat(AttackTables.BLACK_PAWN_ATTACKS[Square.E1]).isEmpty();
    }

    @Test
    void pawnOnEdgeAttacksOnlyOneSquare() 
    {
        int[] whiteA4 = AttackTables.WHITE_PAWN_ATTACKS[Square.A4];
        assertThat(whiteA4).hasSize(1);
        assertThat(whiteA4).contains(Square.B5);

        int[] whiteH4 = AttackTables.WHITE_PAWN_ATTACKS[Square.H4];
        assertThat(whiteH4).hasSize(1);
        assertThat(whiteH4).contains(Square.G5);
    }
}