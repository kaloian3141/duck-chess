package com.duckchess.duck_chess.chess;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FenTest
{

    @Test
    void startingPositionParsesCorrectly()
    {
        Board b = Fen.parse(Fen.STARTING);

        assertThat(b.get(Square.A1)).isEqualTo(Piece.WHITE_ROOK);
        assertThat(b.get(Square.E1)).isEqualTo(Piece.WHITE_KING);
        assertThat(b.get(Square.H8)).isEqualTo(Piece.BLACK_ROOK);
        assertThat(b.get(Square.E8)).isEqualTo(Piece.BLACK_KING);
        assertThat(b.get(Square.E2)).isEqualTo(Piece.WHITE_PAWN);
        assertThat(b.get(Square.E7)).isEqualTo(Piece.BLACK_PAWN);
        assertThat(b.get(Square.E4)).isEqualTo(Piece.EMPTY);

        assertThat(b.sideToMove()).isEqualTo(Color.WHITE);
        assertThat(b.castlingRights()).isEqualTo(Board.CASTLE_ALL);
        assertThat(b.enPassantSquare()).isEqualTo(Square.NONE);
        assertThat(b.halfmoveClock()).isEqualTo(0);
        assertThat(b.fullmoveNumber()).isEqualTo(1);
    }

    @Test
    void startingPositionRoundTrips()
    {
        Board b = Fen.parse(Fen.STARTING);
        assertThat(Fen.serialize(b)).isEqualTo(Fen.STARTING);
    }

    @Test
    void afterE4RoundTrips()
    {
        String fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1";
        Board b = Fen.parse(fen);
        assertThat(b.get(Square.E4)).isEqualTo(Piece.WHITE_PAWN);
        assertThat(b.get(Square.E2)).isEqualTo(Piece.EMPTY);
        assertThat(b.sideToMove()).isEqualTo(Color.BLACK);
        assertThat(b.enPassantSquare()).isEqualTo(Square.E3);
        assertThat(Fen.serialize(b)).isEqualTo(fen);
    }

    @Test
    void kiwipetePositionParses()
    {
        String fen = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
        Board b = Fen.parse(fen);
        assertThat(b.get(Square.E1)).isEqualTo(Piece.WHITE_KING);
        assertThat(b.get(Square.E8)).isEqualTo(Piece.BLACK_KING);
        assertThat(b.castlingRights()).isEqualTo(Board.CASTLE_ALL);
        assertThat(Fen.serialize(b)).isEqualTo(fen);
    }

    @Test
    void emptyBoardRoundTrips() 
    {
        String fen = "8/8/8/8/8/8/8/8 w - - 0 1";
        Board b = Fen.parse(fen);
        for(int sq = 0; sq < 64; sq++)
        {
            assertThat(b.get(sq)).isEqualTo(Piece.EMPTY);
        }
        assertThat(b.castlingRights()).isEqualTo(0);
        assertThat(Fen.serialize(b)).isEqualTo(fen);
    }

    @Test
    void partialCastlingRoundTrips()
    {
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w K - 0 1";
        Board b = Fen.parse(fen);
        assertThat(b.canCastle(Board.CASTLE_WHITE_KING)).isTrue();
        assertThat(b.canCastle(Board.CASTLE_WHITE_QUEEN)).isFalse();
        assertThat(b.canCastle(Board.CASTLE_BLACK_KING)).isFalse();
        assertThat(Fen.serialize(b)).isEqualTo(fen);
    }

    @Test
    void kingSquareIsFound() 
    {
        Board b = Fen.parse(Fen.STARTING);
        assertThat(b.kingSquare(Color.WHITE)).isEqualTo(Square.E1);
        assertThat(b.kingSquare(Color.BLACK)).isEqualTo(Square.E8);
    }

    @Test
    void rejectsMalformedFen() 
    {
        assertThatThrownBy(() -> Fen.parse(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Fen.parse("only three fields here"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Fen.parse("bad/placement/here w - - 0 1"))
                .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    void enPassantSquareRoundTripsForBothColors() 
    {
        String whiteJustDoubled = "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2";
        Board b = Fen.parse(whiteJustDoubled);
        assertThat(b.enPassantSquare()).isEqualTo(Square.E6);
        assertThat(Fen.serialize(b)).isEqualTo(whiteJustDoubled);

        String blackJustDoubled = "rnbqkbnr/pppp1ppp/8/4p3/8/8/PPPPPPPP/RNBQKBNR w KQkq e6 0 1";
        Board b2 = Fen.parse(blackJustDoubled);
        assertThat(b2.enPassantSquare()).isEqualTo(Square.E6);
        assertThat(Fen.serialize(b2)).isEqualTo(blackJustDoubled);
    }

    @Test
    void noCastlingRightsRoundTrip()
    {
        String fen = "4k3/8/8/8/8/8/8/4K3 w - - 0 1";
        Board b = Fen.parse(fen);
        assertThat(b.castlingRights()).isEqualTo(0);
        assertThat(Fen.serialize(b)).isEqualTo(fen);
    }

    @Test
    void variedClocksRoundTrip() 
    {
        String fen = "4k3/8/8/8/8/8/8/4K3 b - - 49 100";
        Board b = Fen.parse(fen);
        assertThat(b.halfmoveClock()).isEqualTo(49);
        assertThat(b.fullmoveNumber()).isEqualTo(100);
        assertThat(b.sideToMove()).isEqualTo(Color.BLACK);
        assertThat(Fen.serialize(b)).isEqualTo(fen);
    }

    @Test
    void shortFenWithoutClocksParses() 
    {
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -";
        Board b = Fen.parse(fen);
        assertThat(b.halfmoveClock()).isEqualTo(0);
        assertThat(b.fullmoveNumber()).isEqualTo(1);
    }

    @Test
    void allPromotionCandidatePositionsRoundTrip() 
    {
        String white = "8/P7/8/8/8/8/8/4K2k w - - 0 1";
        String black = "4K2k/8/8/8/8/8/p7/8 b - - 0 1";
        assertThat(Fen.serialize(Fen.parse(white))).isEqualTo(white);
        assertThat(Fen.serialize(Fen.parse(black))).isEqualTo(black);
    }

    @Test
    void extremeEmptyRunsSerializeCorrectly() 
    {
        String fen = "K7/8/8/8/8/8/8/7k w - - 0 1";
        Board b = Fen.parse(fen);
        assertThat(b.get(Square.A8)).isEqualTo(Piece.WHITE_KING);
        assertThat(b.get(Square.H1)).isEqualTo(Piece.BLACK_KING);
        assertThat(Fen.serialize(b)).isEqualTo(fen);
    }

    @Test
    void mixedEmptyRunsInOneRank() 
    {
        String fen = "8/8/8/8/8/8/8/K6k w - - 0 1";
        Board b = Fen.parse(fen);
        assertThat(b.get(Square.A1)).isEqualTo(Piece.WHITE_KING);
        assertThat(b.get(Square.H1)).isEqualTo(Piece.BLACK_KING);
        assertThat(b.get(Square.D1)).isEqualTo(Piece.EMPTY);
        assertThat(Fen.serialize(b)).isEqualTo(fen);
    }

    @Test
    void anyValidFenParsesAndSerializesIdentically() 
    {
        String[] positions = {
            "r1bqkbnr/pppp1ppp/2n5/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R b KQkq - 3 3",
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
            "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1",
            "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1"
        };
        for(String pos : positions) 
        {
            assertThat(Fen.serialize(Fen.parse(pos))).as("Round-trip failed for: %s", pos).isEqualTo(pos);
        }
    }
}