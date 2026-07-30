package com.duckchess.duck_chess.chess.movegen;

import com.duckchess.duck_chess.chess.Board;
import com.duckchess.duck_chess.chess.Color;
import com.duckchess.duck_chess.chess.Fen;
import com.duckchess.duck_chess.chess.Move;
import com.duckchess.duck_chess.chess.Piece;
import com.duckchess.duck_chess.chess.Square;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoveGeneratorTest 
{

    @Test
    void startingPositionHasTwentyMoves() 
    {
        // Classic sanity: 16 pawn moves (8 single + 8 double) + 4 knight moves
        Board b = Board.startingPosition();
        List<Integer> moves = MoveGenerator.generateLegal(b);
        assertThat(moves).hasSize(20);
    }

    @Test
    void generatesEnPassant() 
    {
        // Black just played d7-d5, white to move, ep target is d6
        Board b = Fen.parse("rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3");
        List<Integer> moves = MoveGenerator.generateLegal(b);

        int epMove = -1;
        for(int m : moves) 
        {
            if(Move.from(m) == Square.E5 && Move.to(m) == Square.D6 && Move.isEnPassant(m)) 
            {
                epMove = m;
                break;
            }
        }
        assertThat(epMove).as("expected e5xd6 en passant").isNotEqualTo(-1);
    }

    @Test
    void generatesFourPromotionsPerPromotionSquare() 
    {
        // White pawn on e7, empty e8 — 4 promotions
        Board b = Fen.parse("8/4P3/8/8/8/8/8/4K2k w - - 0 1");
        List<Integer> moves = MoveGenerator.generateLegal(b);
        long promos = moves.stream()
                .filter(m -> Move.from(m) == Square.E7 && Move.to(m) == Square.E8)
                .count();
        assertThat(promos).isEqualTo(4);
    }

    @Test
    void generatesCastlingWhenLegal() 
    {
        Board b = Fen.parse("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");
        List<Integer> moves = MoveGenerator.generateLegal(b);

        boolean kingside  = moves.stream().anyMatch(m -> m == Move.castle(Square.E1, Square.G1));
        boolean queenside = moves.stream().anyMatch(m -> m == Move.castle(Square.E1, Square.C1));
        assertThat(kingside).isTrue();
        assertThat(queenside).isTrue();
    }

    @Test
    void cannotCastleThroughCheck() 
    {
        // Black rook on f8 attacks f1 (open f-file), so kingside castling passes through check
        Board b = Fen.parse("4kr2/ppppp1pp/8/8/8/8/PPPPP1PP/R3K2R w KQ - 0 1");
        List<Integer> moves = MoveGenerator.generateLegal(b);
        boolean kingsideAllowed = moves.stream().anyMatch(m -> m == Move.castle(Square.E1, Square.G1));
        assertThat(kingsideAllowed).isFalse();
    }

    @Test
    void cannotCastleWhenBlockedByOwnPiece() 
    {
        Board b = Fen.parse("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R2NK2R w KQkq - 0 1");
        List<Integer> moves = MoveGenerator.generateLegal(b);
        boolean queenside = moves.stream().anyMatch(m -> m == Move.castle(Square.E1, Square.C1));
        assertThat(queenside).as("d1 knight blocks queenside").isFalse();
    }

    @Test
    void filtersPinnedPieceMoves() 
    {
        Board b = Fen.parse("3r3k/8/8/8/8/8/3B4/3K4 w - - 0 1");
        List<Integer> moves = MoveGenerator.generateLegal(b);
        long bishopMoves = moves.stream().filter(m -> Move.from(m) == Square.D2).count();
        assertThat(bishopMoves).isEqualTo(0);
    }

    @Test
    void makeAndUnmakeRoundTripsBoard() 
    {
        Board b = Board.startingPosition();
        String beforeFen = b.toFen();

        UndoInfo undo = new UndoInfo();
        int move = Move.doublePush(Square.E2, Square.E4);
        MoveGenerator.make(b, move, undo);
        MoveGenerator.unmake(b, move, undo);

        assertThat(b.toFen()).isEqualTo(beforeFen);
    }

    @Test
    void makeAndUnmakePreservesFenAcrossManyMoves() 
    {
        Board b = Board.startingPosition();
        String beforeFen = b.toFen();

        List<Integer> moves = MoveGenerator.generateLegal(b);
        for(int m : moves) 
        {
            UndoInfo u = new UndoInfo();
            MoveGenerator.make(b, m, u);
            MoveGenerator.unmake(b, m, u);
            assertThat(b.toFen())
                    .as("FEN changed after make/unmake of " + Move.toUci(m))
                    .isEqualTo(beforeFen);
        }
    }

    @Test
    void doublePushSetsEnPassantSquare() 
    {
        Board b = Board.startingPosition();
        UndoInfo undo = new UndoInfo();
        MoveGenerator.make(b, Move.doublePush(Square.E2, Square.E4), undo);
        assertThat(b.enPassantSquare()).isEqualTo(Square.E3);
    }

    @Test
    void singlePushClearsEnPassantSquare() 
    {
        Board b = Fen.parse("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
        UndoInfo undo = new UndoInfo();
        MoveGenerator.make(b, Move.quiet(Square.E7, Square.E6), undo);
        assertThat(b.enPassantSquare()).isEqualTo(Square.NONE);
    }

    @Test
    void kingMoveLosesCastlingRights() 
    {
        Board b = Fen.parse("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");
        UndoInfo undo = new UndoInfo();
        MoveGenerator.make(b, Move.quiet(Square.E1, Square.E2), undo);
        assertThat(b.canCastle(Board.CASTLE_WHITE_KING)).isFalse();
        assertThat(b.canCastle(Board.CASTLE_WHITE_QUEEN)).isFalse();
        assertThat(b.canCastle(Board.CASTLE_BLACK_KING)).isTrue();
    }

    @Test
    void rookMoveLosesOneCastlingRight() 
    {
        Board b = Fen.parse("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");
        UndoInfo undo = new UndoInfo();
        MoveGenerator.make(b, Move.quiet(Square.H1, Square.H2), undo);
        assertThat(b.canCastle(Board.CASTLE_WHITE_KING)).isFalse();
        assertThat(b.canCastle(Board.CASTLE_WHITE_QUEEN)).isTrue();
    }

    @Test
    void kingsideCastleMovesRook() 
    {
        Board b = Fen.parse("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");
        UndoInfo undo = new UndoInfo();
        MoveGenerator.make(b, Move.castle(Square.E1, Square.G1), undo);
        assertThat(b.get(Square.G1)).isEqualTo(com.duckchess.duck_chess.chess.Piece.WHITE_KING);
        assertThat(b.get(Square.F1)).isEqualTo(com.duckchess.duck_chess.chess.Piece.WHITE_ROOK);
        assertThat(b.get(Square.H1)).isEqualTo(com.duckchess.duck_chess.chess.Piece.EMPTY);
        assertThat(b.get(Square.E1)).isEqualTo(com.duckchess.duck_chess.chess.Piece.EMPTY);
    }

    @Test
    void promotionReplacesPiece() 
    {
        Board b = Fen.parse("8/4P3/8/8/8/8/8/4K2k w - - 0 1");
        UndoInfo undo = new UndoInfo();
        MoveGenerator.make(b, Move.promotion(Square.E7, Square.E8, Move.PROMO_QUEEN, false), undo);
        assertThat(b.get(Square.E8)).isEqualTo(com.duckchess.duck_chess.chess.Piece.WHITE_QUEEN);
    }

    // -----------------------------------------------------------------------
    // Pawn tests
    // -----------------------------------------------------------------------

    @Test
    void whitePawnCanDoublePushFromStartingRank() 
    {
        Board b = Board.startingPosition();
        List<Integer> moves = MoveGenerator.generateLegal(b);

        long e2Moves = moves.stream().filter(m -> Move.from(m) == Square.E2).count();
        assertThat(e2Moves).isEqualTo(2);
        assertThat(moves).anyMatch(m -> Move.from(m) == Square.E2 && Move.to(m) == Square.E4 && Move.isDoublePush(m));
        assertThat(moves).anyMatch(m -> Move.from(m) == Square.E2 && Move.to(m) == Square.E3 && !Move.isDoublePush(m));
    }

    @Test
    void pawnCannotDoublePushIfBlocked() 
    {
        // White pawn on e2, own piece on e3 — no push at all
        Board b = Fen.parse("4k3/8/8/8/8/4N3/4P3/4K3 w - - 0 1");
        List<Integer> moves = MoveGenerator.generateLegal(b);
        long e2Moves = moves.stream().filter(m -> Move.from(m) == Square.E2).count();
        assertThat(e2Moves).isEqualTo(0);
    }

    @Test
    void pawnCanSinglePushButNotDoubleIfSecondSquareBlocked() 
    {
        // e3 empty, e4 blocked
        Board b = Fen.parse("4k3/8/8/8/4n3/8/4P3/4K3 w - - 0 1");
        List<Integer> moves = MoveGenerator.generateLegal(b);
        long e2Moves = moves.stream().filter(m -> Move.from(m) == Square.E2).count();
        assertThat(e2Moves).isEqualTo(1);
        assertThat(moves).anyMatch(m -> Move.to(m) == Square.E3);
        assertThat(moves).noneMatch(m -> Move.to(m) == Square.E4);
    }

    @Test
    void pawnCapturesDiagonallyOnly() 
    {
        // White pawn on e4, black pieces on d5, e5, f5
        Board b = Fen.parse("4k3/8/8/3nnn2/4P3/8/8/4K3 w - - 0 1");
        List<Integer> moves = MoveGenerator.generateLegal(b);
        long e4Moves = moves.stream().filter(m -> Move.from(m) == Square.E4).count();
        assertThat(e4Moves).isEqualTo(2);
        assertThat(moves).anyMatch(m -> Move.from(m) == Square.E4 && Move.to(m) == Square.D5 && Move.isCapture(m));
        assertThat(moves).anyMatch(m -> Move.from(m) == Square.E4 && Move.to(m) == Square.F5 && Move.isCapture(m));
    }

    @Test
    void pawnDoesNotCaptureOwnPieces() 
    {
        Board b = Fen.parse("4k3/8/8/3N1N2/4P3/8/8/4K3 w - - 0 1");
        List<Integer> moves = MoveGenerator.generateLegal(b);
        assertThat(moves).noneMatch(m -> Move.from(m) == Square.E4 && Move.to(m) == Square.D5);
        assertThat(moves).noneMatch(m -> Move.from(m) == Square.E4 && Move.to(m) == Square.F5);
    }

    @Test
    void blackPawnMovesDownward() 
    {
        Board b = Fen.parse("4k3/4p3/8/8/8/8/8/4K3 b - - 0 1");
        List<Integer> moves = MoveGenerator.generateLegal(b);
        assertThat(moves).anyMatch(m -> Move.from(m) == Square.E7 && Move.to(m) == Square.E6);
        assertThat(moves).anyMatch(m -> Move.from(m) == Square.E7 && Move.to(m) == Square.E5 && Move.isDoublePush(m));
    }

    @Test
    void enPassantCaptureRemovesPawn() 
    {
        Board b = Fen.parse("rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3");
        UndoInfo undo = new UndoInfo();
        int epMove = Move.enPassant(Square.E5, Square.D6);
        MoveGenerator.make(b, epMove, undo);
        assertThat(b.get(Square.D5)).isEqualTo(Piece.EMPTY);
        assertThat(b.get(Square.D6)).isEqualTo(Piece.WHITE_PAWN);
        assertThat(b.get(Square.E5)).isEqualTo(Piece.EMPTY);
    }

    @Test
    void enPassantUnmakeRestoresPawn() 
    {
        Board b = Fen.parse("rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3");
        String before = b.toFen();
        UndoInfo undo = new UndoInfo();
        int epMove = Move.enPassant(Square.E5, Square.D6);
        MoveGenerator.make(b, epMove, undo);
        MoveGenerator.unmake(b, epMove, undo);
        assertThat(b.toFen()).isEqualTo(before);
    }

    @Test
    void enPassantOnlyAvailableImmediately()
    {
        Board b = Fen.parse("rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq - 0 3");
        List<Integer> moves = MoveGenerator.generateLegal(b);
        assertThat(moves).noneMatch(Move::isEnPassant);
    }

    @Test
    void promotionByCaptureGeneratesFourVariants() 
    {
        Board b = Fen.parse("3n3k/4P3/8/8/8/8/8/4K3 w - - 0 1");
        List<Integer> moves = MoveGenerator.generateLegal(b);
        long capturePromos = moves.stream()
                .filter(m -> Move.from(m) == Square.E7 && Move.to(m) == Square.D8)
                .count();
        assertThat(capturePromos).isEqualTo(4);
        assertThat(moves).allMatch(m -> {
            if(Move.from(m) == Square.E7 && Move.to(m) == Square.D8)
            {
                return Move.isCapture(m) && Move.isPromotion(m);
            }
            return true;
        });
    }

    @Test
    void blackPromotionUsesBlackPieces() 
    {
        Board b = Fen.parse("4k3/8/8/8/8/8/4p3/4K3 b - - 0 1");
        MoveGenerator.generateLegal(b);
        Board b2 = Fen.parse("4k3/8/8/8/8/8/4p3/K7 b - - 0 1");
        List<Integer> moves2 = MoveGenerator.generateLegal(b2);
        long promos = moves2.stream()
                .filter(m -> Move.from(m) == Square.E2 && Move.to(m) == Square.E1)
                .count();
        assertThat(promos).isEqualTo(4);
    }

    @Test
    void promotionUnmakeRestoresPawn() 
    {
        Board b = Fen.parse("8/4P3/8/8/8/8/8/4K2k w - - 0 1");
        String before = b.toFen();
        UndoInfo undo = new UndoInfo();
        int promo = Move.promotion(Square.E7, Square.E8, Move.PROMO_KNIGHT, false);
        MoveGenerator.make(b, promo, undo);
        MoveGenerator.unmake(b, promo, undo);
        assertThat(b.toFen()).isEqualTo(before);
    }

    // -----------------------------------------------------------------------
    // Knight tests
    // -----------------------------------------------------------------------

    @Test
    void knightGeneratesAllEightMovesInCenter() 
    {
        Board b = Fen.parse("4k3/8/8/8/4N3/8/8/4K3 w - - 0 1");
        List<Integer> knightMoves = MoveGenerator.generateLegal(b).stream()
                .filter(m -> Move.from(m) == Square.E4)
                .toList();
        assertThat(knightMoves).hasSize(8);
    }

    @Test
    void knightIgnoresBlockersOnItsPath() 
    {
        // Knight on e4, own pieces surrounding it — still moves normally
        Board b = Fen.parse("4k3/8/8/3PPP2/3PN1P1/3PPP2/8/4K3 w - - 0 1");
        List<Integer> knightMoves = MoveGenerator.generateLegal(b).stream()
                .filter(m -> Move.from(m) == Square.E4)
                .toList();
        assertThat(knightMoves).hasSize(8);
    }

    @Test
    void knightCannotLandOnOwnPiece() 
    {
        // Knight on e4, own pawn on f6 (a knight target)
        Board b = Fen.parse("4k3/8/5P2/8/4N3/8/8/4K3 w - - 0 1");
        List<Integer> moves = MoveGenerator.generateLegal(b);
        assertThat(moves).noneMatch(m -> Move.from(m) == Square.E4 && Move.to(m) == Square.F6);
    }

    @Test
    void knightCapturesEnemyPiece() 
    {
        Board b = Fen.parse("4k3/8/5p2/8/4N3/8/8/4K3 w - - 0 1");
        List<Integer> moves = MoveGenerator.generateLegal(b);
        assertThat(moves).anyMatch(m ->
                Move.from(m) == Square.E4 && Move.to(m) == Square.F6 && Move.isCapture(m));
    }

    // -----------------------------------------------------------------------
    // Slider tests
    // -----------------------------------------------------------------------

    @Test
    void bishopGeneratesFullDiagonalOnEmptyBoard() 
    {
        Board b = Fen.parse("4k3/8/8/8/4B3/8/8/4K3 w - - 0 1");
        List<Integer> bishopMoves = MoveGenerator.generateLegal(b).stream()
                .filter(m -> Move.from(m) == Square.E4)
                .toList();
        assertThat(bishopMoves).hasSize(13);
    }

    @Test
    void rookStopsAtOwnPieceButCanCaptureEnemyOnPath() 
    {
        // Rook on a1, own knight on a4, enemy queen on d1, kings out of the line
        Fen.parse("4k3/8/8/8/N7/8/8/R2q4 w - - 0 1");
        Board b2 = Fen.parse("4k3/6K1/8/8/N7/8/8/R2q4 w - - 0 1");
        List<Integer> rookMoves = MoveGenerator.generateLegal(b2).stream()
                .filter(m -> Move.from(m) == Square.A1)
                .toList();
        assertThat(rookMoves).hasSize(5);
        assertThat(rookMoves).noneMatch(m -> Move.to(m) == Square.A4);
        assertThat(rookMoves).anyMatch(m -> Move.to(m) == Square.D1 && Move.isCapture(m));
        assertThat(rookMoves).noneMatch(m -> Move.to(m) == Square.E1);
    }

    @Test
    void queenCombinesRookAndBishopMoves() 
    {
        // Isolate the queen — kings out of the e-file so nothing blocks
        Board b = Fen.parse("7k/8/8/8/4Q3/8/8/K7 w - - 0 1");
        List<Integer> queenMoves = MoveGenerator.generateLegal(b).stream()
                .filter(m -> Move.from(m) == Square.E4)
                .toList();
        assertThat(queenMoves).hasSize(27);
    }

    // -----------------------------------------------------------------------
    // King & castling tests
    // -----------------------------------------------------------------------

    @Test
    void kingHasEightMovesInOpenPosition()
    {
        Board b = Fen.parse("8/8/8/8/4K3/8/8/7k w - - 0 1");
        List<Integer> kingMoves = MoveGenerator.generateLegal(b).stream()
                .filter(m -> Move.from(m) == Square.E4)
                .toList();
        assertThat(kingMoves).hasSize(8);
    }

    @Test
    void kingCannotMoveIntoCheck() 
    {
        // Black rook on e8 attacks the e-file; king can't move to e2
        Board b = Fen.parse("4r3/8/8/8/8/8/8/4K2k w - - 0 1");
        List<Integer> kingMoves = MoveGenerator.generateLegal(b).stream()
                .filter(m -> Move.from(m) == Square.E1)
                .toList();
        assertThat(kingMoves).noneMatch(m -> Move.to(m) == Square.E2);
        assertThat(kingMoves).anyMatch(m -> Move.to(m) == Square.D2);
        assertThat(kingMoves).anyMatch(m -> Move.to(m) == Square.F2);
    }

    @Test
    void kingCannotCastleWhileInCheck() 
    {
        // Black rook on e8 checks white king on e1
        Board b = Fen.parse("4r2k/8/8/8/8/8/8/R3K2R w KQ - 0 1");
        List<Integer> moves = MoveGenerator.generateLegal(b);
        assertThat(moves).noneMatch(Move::isCastle);
    }

    @Test
    void queensideCastleBlockedByBSquareOccupancy() 
    {
        // Piece on b1 blocks queenside castle (b-square must be empty too, even though king doesn't traverse it)
        Board b = Fen.parse("r3k3/8/8/8/8/8/8/RN2K3 w Q - 0 1");
        List<Integer> moves = MoveGenerator.generateLegal(b);
        assertThat(moves).noneMatch(Move::isCastle);
    }

    @Test
    void rookCaptureRevokesCastlingRight() 
    {
        // White rook captures black rook on h8, revoking black kingside right
        Board b = Fen.parse("4k2r/8/8/8/8/8/8/4K2R w Kk - 0 1");
        UndoInfo undo = new UndoInfo();
        MoveGenerator.make(b, Move.capture(Square.H1, Square.H8), undo);
        assertThat(b.canCastle(Board.CASTLE_BLACK_KING)).isFalse();
        assertThat(b.canCastle(Board.CASTLE_WHITE_KING)).isFalse();
    }

    @Test
    void castleUnmakeRestoresRookAndKing()
    {
        Board b = Fen.parse("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");
        String before = b.toFen();
        UndoInfo undo = new UndoInfo();
        int castleMove = Move.castle(Square.E1, Square.G1);
        MoveGenerator.make(b, castleMove, undo);
        MoveGenerator.unmake(b, castleMove, undo);
        assertThat(b.toFen()).isEqualTo(before);
    }

    @Test
    void queensideCastleUnmakeRestoresState() 
    {
        Board b = Fen.parse("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");
        String before = b.toFen();
        UndoInfo undo = new UndoInfo();
        int castleMove = Move.castle(Square.E1, Square.C1);
        MoveGenerator.make(b, castleMove, undo);
        MoveGenerator.unmake(b, castleMove, undo);
        assertThat(b.toFen()).isEqualTo(before);
    }

    @Test
    void blackCastlesAreGeneratedToo() 
    {
        Board b = Fen.parse("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R b KQkq - 0 1");
        List<Integer> moves = MoveGenerator.generateLegal(b);
        assertThat(moves).anyMatch(m -> m == Move.castle(Square.E8, Square.G8));
        assertThat(moves).anyMatch(m -> m == Move.castle(Square.E8, Square.C8));
    }

    // -----------------------------------------------------------------------
    // Legality / check tests
    // -----------------------------------------------------------------------

    @Test
    void mustResolveCheck() 
    {
        // Black rook on e8 checks white king on e1
        // Only legal moves are king moves out of check, or blocking on e-file
        Board b = Fen.parse("4r3/8/8/8/8/8/4N3/4K2k w - - 0 1");
        List<Integer> moves = MoveGenerator.generateLegal(b);
        for(int m : moves)
        {
            UndoInfo undo = new UndoInfo();
            MoveGenerator.make(b, m, undo);
            boolean stillInCheck = AttackDetector.isInCheck(b, Color.WHITE);
            MoveGenerator.unmake(b, m, undo);
            assertThat(stillInCheck).as("move %s doesn't resolve check", Move.toUci(m)).isFalse();
        }
        assertThat(moves).isNotEmpty();
    }

    @Test
    void checkmatePositionHasZeroLegalMoves() 
    {
        // Simple back-rank mate: black king on h8, black pawns on g7,h7, white queen on h1
        Board b = Fen.parse("rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3");
        List<Integer> moves = MoveGenerator.generateLegal(b);
        assertThat(moves).isEmpty();
    }

    @Test
    void stalematePositionHasZeroLegalMoves() 
    {
        // Classic stalemate: black king on a8, white king on c7, white queen on b6
        Board b = Fen.parse("k7/8/1QK5/8/8/8/8/8 b - - 0 1");
        List<Integer> moves = MoveGenerator.generateLegal(b);
        assertThat(moves).isEmpty();
        assertThat(AttackDetector.isInCheck(b, Color.BLACK)).isFalse();
    }

}