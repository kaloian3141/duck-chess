package com.duckchess.duck_chess.chess.variant;
import com.duckchess.duck_chess.chess.movegen.MoveGenerator;
import com.duckchess.duck_chess.chess.Board;
import com.duckchess.duck_chess.chess.Fen;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StandardChessTest 
{

    private final Variant chess = StandardChess.INSTANCE;

    @Test
    void startingPositionIsOngoing() 
    {
        Board b = Board.startingPosition();
        List<Integer> moves = chess.legalMoves(b);
        assertThat(chess.result(b, moves, List.of())).isEqualTo(GameResult.ONGOING);
    }

    @Test
    void foolsMateIsWhiteLoss() 
    {
        // 1. f3 e5 2. g4 Qh4# — black just delivered mate
        Board b = Fen.parse("rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3");
        List<Integer> moves = chess.legalMoves(b);
        assertThat(moves).isEmpty();
        assertThat(chess.result(b, moves, List.of())).isEqualTo(GameResult.BLACK_WINS_CHECKMATE);
    }

    @Test
    void stalematePositionIsDraw() 
    {
        // Classic stalemate: black king on a8, white king on c7, white queen on b6
        Board b = Fen.parse("k7/8/1QK5/8/8/8/8/8 b - - 0 1");
        List<Integer> moves = chess.legalMoves(b);
        assertThat(moves).isEmpty();
        assertThat(chess.result(b, moves, List.of())).isEqualTo(GameResult.DRAW_STALEMATE);
    }

    @Test
    void fiftyMoveRuleTriggers() 
    {
        // Halfmove clock at 100 → 50-move rule
        Board b = Fen.parse("4k3/8/4K3/8/8/8/8/8 w - - 100 60");
        List<Integer> moves = chess.legalMoves(b);
        assertThat(chess.result(b, moves, List.of())).isEqualTo(GameResult.DRAW_FIFTY_MOVE_RULE);
    }

    @Test
    void threefoldRepetitionTriggers() 
    {
        Board b = Board.startingPosition();
        long h = b.hash();
        // Two prior occurrences of the same hash means current makes three total
        List<Long> past = List.of(h, h);
        assertThat(chess.result(b, chess.legalMoves(b), past))
                .isEqualTo(GameResult.DRAW_THREEFOLD_REPETITION);
    }

    @Test
    void twoOccurrencesIsNotYetThreefold() 
    {
        Board b = Board.startingPosition();
        long h = b.hash();
        List<Long> past = List.of(h);   // this + one prior = two occurrences
        assertThat(chess.result(b, chess.legalMoves(b), past))
                .isEqualTo(GameResult.ONGOING);
    }

    @Test
    void kingVsKingIsInsufficientMaterial() 
    {
        Board b = Fen.parse("4k3/8/4K3/8/8/8/8/8 w - - 0 1");
        assertThat(chess.result(b, chess.legalMoves(b), List.of()))
                .isEqualTo(GameResult.DRAW_INSUFFICIENT_MATERIAL);
    }

    @Test
    void kingBishopVsKingIsInsufficientMaterial() 
    {
        Board b = Fen.parse("4k3/8/4K3/8/4B3/8/8/8 w - - 0 1");
        assertThat(chess.result(b, chess.legalMoves(b), List.of()))
                .isEqualTo(GameResult.DRAW_INSUFFICIENT_MATERIAL);
    }

    @Test
    void kingKnightVsKingIsInsufficientMaterial() 
    {
        Board b = Fen.parse("4k3/8/4K3/8/4N3/8/8/8 w - - 0 1");
        assertThat(chess.result(b, chess.legalMoves(b), List.of()))
                .isEqualTo(GameResult.DRAW_INSUFFICIENT_MATERIAL);
    }

    @Test
    void kingPawnVsKingIsNotInsufficient() 
    {
        // A pawn can promote — game continues
        Board b = Fen.parse("4k3/8/4K3/4P3/8/8/8/8 w - - 0 1");
        assertThat(chess.result(b, chess.legalMoves(b), List.of()))
                .isEqualTo(GameResult.ONGOING);
    }

    @Test
    void kingRookVsKingIsNotInsufficient() 
    {
        Board b = Fen.parse("4k3/8/4K3/8/4R3/8/8/8 w - - 0 1");
        assertThat(chess.result(b, chess.legalMoves(b), List.of()))
                .isEqualTo(GameResult.ONGOING);
    }

    @Test
    void winnerIsCorrect() 
    {
        assertThat(GameResult.WHITE_WINS_CHECKMATE.winner()).isEqualTo(com.duckchess.duck_chess.chess.Color.WHITE);
        assertThat(GameResult.BLACK_WINS_CHECKMATE.winner()).isEqualTo(com.duckchess.duck_chess.chess.Color.BLACK);
        assertThat(GameResult.DRAW_STALEMATE.winner()).isNull();
        assertThat(GameResult.ONGOING.winner()).isNull();
    }

    @Test
    void resultFlagsAreConsistent() 
    {
        assertThat(GameResult.ONGOING.isOver()).isFalse();
        assertThat(GameResult.WHITE_WINS_CHECKMATE.isOver()).isTrue();
        assertThat(GameResult.DRAW_STALEMATE.isOver()).isTrue();
        assertThat(GameResult.DRAW_STALEMATE.isDraw()).isTrue();
        assertThat(GameResult.WHITE_WINS_CHECKMATE.isDraw()).isFalse();
    }

    @Test
    void backRankMateIsWhiteWin() 
    {
        // White queen mates black king trapped on back rank
        Fen.parse("6k1/5ppp/8/8/8/8/8/6K1 w - - 0 1");
        // Need white queen delivering mate:
        Fen.parse("6k1/5ppp/8/8/8/8/8/1Q4K1 w - - 0 1");
        // Actually, set up a real back-rank mate: black king g8, black pawns f7 g7 h7, white rook on d8
        Board b3 = Fen.parse("3R2k1/5ppp/8/8/8/8/8/6K1 b - - 0 1");
        List<Integer> moves = chess.legalMoves(b3);
        assertThat(moves).isEmpty();
        assertThat(chess.result(b3, moves, List.of())).isEqualTo(GameResult.WHITE_WINS_CHECKMATE);
    }

    @Test
    void smotheredMateIsWhiteWin() 
    {
        Board b = Fen.parse("rnbqkb1r/pp1pnppp/2pNp3/8/8/8/PPPPPPPP/R1BQKBNR b KQkq - 0 1");
        List<Integer> moves = chess.legalMoves(b);
        assertThat(moves).isEmpty();
        assertThat(chess.result(b, moves, List.of())).isEqualTo(GameResult.WHITE_WINS_CHECKMATE);
    }

    // -----------------------------------------------------------------------
    // Insufficient material — edge cases
    // -----------------------------------------------------------------------

    @Test
    void bishopVsBishopSameColorIsInsufficient() 
    {
        // Both bishops on light squares → same-color bishops → draw
        // Light squares: a1 dark, b1 light. Let's put white bishop on f1 (light), black on b7 (light).
        // f1: file 5 + rank 0 = 5 (odd) → dark. Let me check: light = (file+rank)%2 == 0.
        // f1 = (5+0)%2 = 1 → dark. Try c1 (2+0=2, light). And f8 (5+7=12, light).
        Fen.parse("4k3/8/8/8/8/8/8/2B1K3 w - - 0 1");
        // Only one bishop. Add a black bishop on same-color square: c1 is light, so we need a black bishop on any other light square.
        // f8 file 5, rank 7 → 12 → light. Good.
        Board b2 = Fen.parse("4kb2/8/8/8/8/8/8/2B1K3 w - - 0 1");
        assertThat(chess.result(b2, chess.legalMoves(b2), List.of()))
                .isEqualTo(GameResult.DRAW_INSUFFICIENT_MATERIAL);
    }

    @Test
    void bishopVsBishopOppositeColorIsNotInsufficient() 
    {
        // Same-color pieces on opposite-colored bishops can still deliver mate in theory
        // → engine treats as ONGOING
        // c1 is light (2+0=2 even). h8 is dark (7+7=14 even... wait that's light too).
        // Let me recompute: a1 = 0+0 = 0 → even → light. a1 is famously dark though.
        // Convention issue: chess.com/lichess call a1 dark. My formula makes a1 light.
        // Doesn't matter for the test — what matters is that the two bishops have opposite parity.
        // c1 = (2+0)%2 = 0. f8 = (5+7)%2 = 0. Both same. Need opposite: c1 (0) and g8 (6+7=13, odd).
        Board b = Fen.parse("4k1b1/8/8/8/8/8/8/2B1K3 w - - 0 1");
        assertThat(chess.result(b, chess.legalMoves(b), List.of()))
                .isEqualTo(GameResult.ONGOING);
    }

    @Test
    void twoKnightsVsKingIsNotInsufficientByRule() 
    {
        // FIDE rule: two knights can't force mate, but it's still ONGOING per most engines
        // (theoretically drawable, not forced). Our rule: minor + minor doesn't match the
        // "K + minor vs K" pattern, so ONGOING.
        Board b = Fen.parse("4k3/8/4K3/8/3NN3/8/8/8 w - - 0 1");
        assertThat(chess.result(b, chess.legalMoves(b), List.of()))
                .isEqualTo(GameResult.ONGOING);
    }

    @Test
    void queenPresentIsAlwaysSufficient() 
    {
        Board b = Fen.parse("4k3/8/4K3/8/4Q3/8/8/8 w - - 0 1");
        assertThat(chess.result(b, chess.legalMoves(b), List.of()))
                .isEqualTo(GameResult.ONGOING);
    }

    @Test
    void anyPawnMeansOngoing() 
    {
        // Even lone pawn — could promote
        Board b = Fen.parse("4k3/8/4K3/8/8/8/4P3/8 w - - 0 1");
        assertThat(chess.result(b, chess.legalMoves(b), List.of()))
                .isEqualTo(GameResult.ONGOING);
    }

    // -----------------------------------------------------------------------
    // Fifty-move rule — boundary
    // -----------------------------------------------------------------------

    @Test
    void ninetyNineHalfmovesIsNotYetFiftyMove() 
    {
        // Rule triggers at 100 halfmoves. 99 is still ongoing.
        Board b = Fen.parse("4k3/8/4K3/8/4R3/8/8/8 w - - 99 60");
        assertThat(chess.result(b, chess.legalMoves(b), List.of()))
                .isEqualTo(GameResult.ONGOING);
    }

    @Test
    void fiftyMoveRuleAtExactlyOneHundred() 
    {
        Board b = Fen.parse("4k3/8/4K3/8/4R3/8/8/8 w - - 100 60");
        assertThat(chess.result(b, chess.legalMoves(b), List.of()))
                .isEqualTo(GameResult.DRAW_FIFTY_MOVE_RULE);
    }

    @Test
    void checkmateBeatsAllOtherEndReasons() 
    {
        // If the position IS checkmate but also has clock=100, checkmate wins.
        // Set up a back-rank mate with halfmove clock at 100.
        Board b = Fen.parse("3R2k1/5ppp/8/8/8/8/8/6K1 b - - 100 60");
        List<Integer> moves = chess.legalMoves(b);
        assertThat(moves).isEmpty();
        assertThat(chess.result(b, moves, List.of())).isEqualTo(GameResult.WHITE_WINS_CHECKMATE);
    }

    // -----------------------------------------------------------------------
    // Threefold repetition — edge cases
    // -----------------------------------------------------------------------

    @Test
    void differentPositionsInHistoryDoNotTriggerRepetition() 
    {
        Board current = Board.startingPosition();
        List<Long> past = List.of(123L, 456L, 789L);  // random unrelated hashes
        assertThat(chess.result(current, chess.legalMoves(current), past))
                .isEqualTo(GameResult.ONGOING);
    }

    @Test
    void repetitionCountsCurrentPositionAsOne() 
    {
        // The current position is 1, so we need exactly 2 prior occurrences for threefold
        Board b = Board.startingPosition();
        long h = b.hash();
        List<Long> two = List.of(h, h);
        assertThat(chess.result(b, chess.legalMoves(b), two))
                .isEqualTo(GameResult.DRAW_THREEFOLD_REPETITION);
    }

    @Test
    void repetitionWithUnrelatedHashesInBetween() 
    {
        // The current + 2 matching hashes (with other hashes mixed in) still triggers
        Board b = Board.startingPosition();
        long h = b.hash();
        List<Long> mixed = List.of(h, 999L, h, 888L, 777L);
        assertThat(chess.result(b, chess.legalMoves(b), mixed))
                .isEqualTo(GameResult.DRAW_THREEFOLD_REPETITION);
    }

    @Test
    void fourfoldRepetitionAlsoTriggers() 
    {
        // Anything >=3 occurrences triggers
        Board b = Board.startingPosition();
        long h = b.hash();
        List<Long> four = List.of(h, h, h);
        assertThat(chess.result(b, chess.legalMoves(b), four))
                .isEqualTo(GameResult.DRAW_THREEFOLD_REPETITION);
    }

    // -----------------------------------------------------------------------
    // Variant metadata
    // -----------------------------------------------------------------------

    @Test
    void nameIsStandard() 
    {
        assertThat(chess.name()).isEqualTo("STANDARD");
    }

    @Test
    void singletonInstance() 
    {
        assertThat(StandardChess.INSTANCE).isSameAs(StandardChess.INSTANCE);
    }

    // -----------------------------------------------------------------------
    // Legal moves match MoveGenerator
    // -----------------------------------------------------------------------

    @Test
    void legalMovesMatchesUnderlyingGenerator()
    {
        // The variant should return exactly what MoveGenerator gives us for standard
        Board b = Board.startingPosition();
        List<Integer> variantMoves = chess.legalMoves(b);
        List<Integer> directMoves = MoveGenerator.generateLegal(b);
        assertThat(variantMoves).containsExactlyElementsOf(directMoves);
    }

    @Test
    void legalMovesAtKiwipeteAreCorrect()
    {
        Board b = Fen.parse("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
        assertThat(chess.legalMoves(b)).hasSize(48);
    }
}