package com.duckchess.duck_chess.chess.movegen;

import com.duckchess.duck_chess.chess.Board;
import com.duckchess.duck_chess.chess.Fen;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PerftTest {

    @Test
    void startingPositionDepth1() 
    {
        Board b = Board.startingPosition();
        assertThat(Perft.count(b, 1)).isEqualTo(20L);
    }

    @Test
    void startingPositionDepth2() 
    {
        Board b = Board.startingPosition();
        assertThat(Perft.count(b, 2)).isEqualTo(400L);
    }

    @Test
    void startingPositionDepth3() 
    {
        Board b = Board.startingPosition();
        assertThat(Perft.count(b, 3)).isEqualTo(8_902L);
    }

    @Test
    void startingPositionDepth4() 
    {
        Board b = Board.startingPosition();
        assertThat(Perft.count(b, 4)).isEqualTo(197_281L);
    }

    private static final String KIWIPETE =
        "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";

    @Test
    void kiwipeteDepth1() 
    {
        Board b = Fen.parse(KIWIPETE);
        assertThat(Perft.count(b, 1)).isEqualTo(48L);
    }

    @Test
    void kiwipeteDepth2() 
    {
        Board b = Fen.parse(KIWIPETE);
        assertThat(Perft.count(b, 2)).isEqualTo(2_039L);
    }

    @Test
    void kiwipeteDepth3() 
    {
        Board b = Fen.parse(KIWIPETE);
        assertThat(Perft.count(b, 3)).isEqualTo(97_862L);
    }


    private static final String POSITION_3 =
        "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1";

    @Test
    void position3Depth1() 
    {
        Board b = Fen.parse(POSITION_3);
        assertThat(Perft.count(b, 1)).isEqualTo(14L);
    }

    @Test
    void position3Depth2() 
    {
        Board b = Fen.parse(POSITION_3);
        assertThat(Perft.count(b, 2)).isEqualTo(191L);
    }

    @Test
    void position3Depth3() 
    {
        Board b = Fen.parse(POSITION_3);
        assertThat(Perft.count(b, 3)).isEqualTo(2_812L);
    }

    @Test
    void position3Depth4() 
    {
        Board b = Fen.parse(POSITION_3);
        assertThat(Perft.count(b, 4)).isEqualTo(43_238L);
    }


    private static final String POSITION_4 =
        "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1";

    @Test
    void position4Depth1() 
    {
        Board b = Fen.parse(POSITION_4);
        assertThat(Perft.count(b, 1)).isEqualTo(6L);
    }

    @Test
    void position4Depth2() 
    {
        Board b = Fen.parse(POSITION_4);
        assertThat(Perft.count(b, 2)).isEqualTo(264L);
    }

    @Test
    void position4Depth3() 
    {
        Board b = Fen.parse(POSITION_4);
        assertThat(Perft.count(b, 3)).isEqualTo(9_467L);
    }


    private static final String POSITION_5 =
        "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8";

    @Test
    void position5Depth1() 
    {
        Board b = Fen.parse(POSITION_5);
        assertThat(Perft.count(b, 1)).isEqualTo(44L);
    }

    @Test
    void position5Depth2() 
    {
        Board b = Fen.parse(POSITION_5);
        assertThat(Perft.count(b, 2)).isEqualTo(1_486L);
    }

    @Test
    void position5Depth3() 
    {
        Board b = Fen.parse(POSITION_5);
        assertThat(Perft.count(b, 3)).isEqualTo(62_379L);
    }
}