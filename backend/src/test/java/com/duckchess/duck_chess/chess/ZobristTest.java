package com.duckchess.duck_chess.chess;

import com.duckchess.duck_chess.chess.movegen.MoveGenerator;
import com.duckchess.duck_chess.chess.movegen.UndoInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZobristTest {

    @Test
    void identicalPositionsHaveIdenticalHashes() 
    {
        Board a = Board.startingPosition();
        Board b = Board.startingPosition();
        assertThat(a.hash()).isEqualTo(b.hash());
    }

    @Test
    void differentPositionsHaveDifferentHashes() 
    {
        Board a = Board.startingPosition();
        Board b = Fen.parse("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
        assertThat(a.hash()).isNotEqualTo(b.hash());
    }

    @Test
    void sideToMoveAffectsHash() 
    {
        Board a = Fen.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        Board b = Fen.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1");
        assertThat(a.hash()).isNotEqualTo(b.hash());
    }

    @Test
    void castlingRightsAffectHash() 
    {
        Board full = Fen.parse("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");
        Board none = Fen.parse("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w - - 0 1");
        assertThat(full.hash()).isNotEqualTo(none.hash());
    }

    @Test
    void enPassantSquareAffectsHash() 
    {
        Board with    = Fen.parse("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
        Board without = Fen.parse("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1");
        assertThat(with.hash()).isNotEqualTo(without.hash());
    }

    @Test
    void makeUnmakeRestoresHash() 
    {
        Board b = Board.startingPosition();
        long originalHash = b.hash();

        UndoInfo undo = new UndoInfo();
        int move = Move.doublePush(Square.E2, Square.E4);
        MoveGenerator.make(b, move, undo);
        assertThat(b.hash()).isNotEqualTo(originalHash);

        MoveGenerator.unmake(b, move, undo);
        assertThat(b.hash()).isEqualTo(originalHash);
    }

    @Test
    void makeUnmakePreservesHashAcrossAllStartingMoves() 
    {
        Board b = Board.startingPosition();
        long originalHash = b.hash();

        List<Integer> moves = MoveGenerator.generateLegal(b);
        for(int m : moves) 
        {
            UndoInfo undo = new UndoInfo();
            MoveGenerator.make(b, m, undo);
            MoveGenerator.unmake(b, m, undo);
            assertThat(b.hash())
                    .as("hash not restored after make/unmake of %s", Move.toUci(m))
                    .isEqualTo(originalHash);
        }
    }

    @Test
    void samePositionReachedByDifferentPathsHasSameHash() 
    {
        // 1. Nf3 Nc6 2. Nc3 vs 1. Nc3 Nc6 2. Nf3 — same position, different move order
        Board a = Board.startingPosition();
        Board bOther = Board.startingPosition();

        UndoInfo undoA = new UndoInfo();
        UndoInfo undoB = new UndoInfo();

        // Path A: Nf3, Nc6, Nc3
        MoveGenerator.make(a, Move.quiet(Square.G1, Square.F3), undoA);
        MoveGenerator.make(a, Move.quiet(Square.B8, Square.C6), undoA);
        MoveGenerator.make(a, Move.quiet(Square.B1, Square.C3), undoA);

        // Path B: Nc3, Nc6, Nf3
        MoveGenerator.make(bOther, Move.quiet(Square.B1, Square.C3), undoB);
        MoveGenerator.make(bOther, Move.quiet(Square.B8, Square.C6), undoB);
        MoveGenerator.make(bOther, Move.quiet(Square.G1, Square.F3), undoB);

        assertThat(a.hash()).isEqualTo(bOther.hash());
        assertThat(a.toFen()).isEqualTo(bOther.toFen());
    }

    @Test
    void computeHashMatchesIncrementalHash() 
    {
        // After several moves, computeHash from scratch should equal
        // whatever the board's hash field says.
        Board b = Board.startingPosition();
        UndoInfo undo = new UndoInfo();

        MoveGenerator.make(b, Move.doublePush(Square.E2, Square.E4), undo);
        MoveGenerator.make(b, Move.doublePush(Square.E7, Square.E5), undo);
        MoveGenerator.make(b, Move.quiet(Square.G1, Square.F3), undo);
        MoveGenerator.make(b, Move.quiet(Square.B8, Square.C6), undo);

        assertThat(b.hash()).isEqualTo(Zobrist.computeHash(b));
    }


    @Test
    void enPassantAvailabilityAffectsHash() 
    {
        // Same piece placement, but one board has an ep target and the other doesn't.
        // This is the case that actually catches a real bug: if a pawn double-pushes
        // but the opponent doesn't capture, the ep target must be gone next turn.
        Board withEp    = Fen.parse("rnbqkbnr/pppp1ppp/8/8/3Pp3/8/PPP1PPPP/RNBQKBNR b KQkq d3 0 1");
        Board withoutEp = Fen.parse("rnbqkbnr/pppp1ppp/8/8/3Pp3/8/PPP1PPPP/RNBQKBNR b KQkq -  0 1");
        assertThat(withEp.hash()).isNotEqualTo(withoutEp.hash());
    }

    @Test
    void differentEnPassantFilesGiveDifferentHashes() 
    {
        // Two positions where the only difference is which file the ep target is on
        Board d3 = Fen.parse("rnbqkbnr/8/8/8/8/8/8/RNBQKBNR b KQkq d3 0 1");
        Board e3 = Fen.parse("rnbqkbnr/8/8/8/8/8/8/RNBQKBNR b KQkq e3 0 1");
        assertThat(d3.hash()).isNotEqualTo(e3.hash());
    }

    @Test
    void duckSquareAffectsHash() 
    {
        Board a = Board.startingPosition();
        Board b = Board.startingPosition();
        b.setDuckSquare(Square.D4);
        b.set(Square.D4, Piece.DUCK);
        b.setHash(Zobrist.computeHash(b));
        assertThat(a.hash()).isNotEqualTo(b.hash());
    }

    @Test
    void movingDuckToDifferentSquaresGivesDifferentHashes() 
    {
        Board d4 = Board.startingPosition();
        d4.setDuckSquare(Square.D4);
        d4.set(Square.D4, Piece.DUCK);
        d4.setHash(Zobrist.computeHash(d4));

        Board e5 = Board.startingPosition();
        e5.setDuckSquare(Square.E5);
        e5.set(Square.E5, Piece.DUCK);
        e5.setHash(Zobrist.computeHash(e5));

        assertThat(d4.hash()).isNotEqualTo(e5.hash());
    }

    @Test
    void hashChangesEveryPlyDuringPerftLike() 
    {
        // Confirm the hash actually changes on each move — a bug where make forgets
        // to recompute hash would surface here immediately
        Board b = Board.startingPosition();
        long h0 = b.hash();

        UndoInfo undo = new UndoInfo();
        MoveGenerator.make(b, Move.doublePush(Square.E2, Square.E4), undo);
        long h1 = b.hash();
        MoveGenerator.make(b, Move.doublePush(Square.E7, Square.E5), undo);
        long h2 = b.hash();
        MoveGenerator.make(b, Move.quiet(Square.G1, Square.F3), undo);
        long h3 = b.hash();
        MoveGenerator.make(b, Move.quiet(Square.B8, Square.C6), undo);
        long h4 = b.hash();

        assertThat(h0).isNotEqualTo(h1);
        assertThat(h1).isNotEqualTo(h2);
        assertThat(h2).isNotEqualTo(h3);
        assertThat(h3).isNotEqualTo(h4);
        // All four intermediate hashes are distinct too
        assertThat(java.util.Set.of(h0, h1, h2, h3, h4)).hasSize(5);
    }

    @Test
    void castlingRightLossChangesHash() 
    {
        // Same position, different castling rights should hash differently.
        // Also confirms make() updates the hash correctly when it revokes rights.
        Board b = Fen.parse("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");
        long before = b.hash();

        UndoInfo undo = new UndoInfo();
        MoveGenerator.make(b, Move.quiet(Square.E1, Square.E2), undo);   // king move revokes both white rights
        long after = b.hash();

        assertThat(before).isNotEqualTo(after);

        // Same king move made from a position that already has no white rights
        // should NOT trigger the castling delta — hashes should look different in
        // a predictable way (different piece-square contribution, no castle atom flip)
        Board noRights = Fen.parse("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w kq - 0 1");
        long noRightsBefore = noRights.hash();
        MoveGenerator.make(noRights, Move.quiet(Square.E1, Square.E2), undo);
        long noRightsAfter = noRights.hash();
        assertThat(noRightsBefore).isNotEqualTo(noRightsAfter);
    }

    @Test
    void promotionMakeUnmakeRoundTripsHash() 
    {
        // Promotion changes the piece on the board — most likely to break incremental
        // hash updates if we ever switch away from full recomputation
        Board b = Fen.parse("8/4P3/8/8/8/8/8/4K2k w - - 0 1");
        long before = b.hash();

        UndoInfo undo = new UndoInfo();
        int promo = Move.promotion(Square.E7, Square.E8, Move.PROMO_QUEEN, false);
        MoveGenerator.make(b, promo, undo);
        assertThat(b.hash()).isNotEqualTo(before);

        MoveGenerator.unmake(b, promo, undo);
        assertThat(b.hash()).isEqualTo(before);
    }

    @Test
    void enPassantCaptureMakeUnmakeRoundTripsHash() 
    {
        // The en-passant case has the trickiest state to preserve:
        // captured piece isn't on the target square, and ep availability changes
        Board b = Fen.parse("rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3");
        long before = b.hash();

        UndoInfo undo = new UndoInfo();
        int ep = Move.enPassant(Square.E5, Square.D6);
        MoveGenerator.make(b, ep, undo);
        assertThat(b.hash()).isNotEqualTo(before);

        MoveGenerator.unmake(b, ep, undo);
        assertThat(b.hash()).isEqualTo(before);
    }

    @Test
    void castlingMakeUnmakeRoundTripsHash() 
    {
        Board b = Fen.parse("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");
        long before = b.hash();

        UndoInfo undo = new UndoInfo();
        int castle = Move.castle(Square.E1, Square.G1);
        MoveGenerator.make(b, castle, undo);
        assertThat(b.hash()).isNotEqualTo(before);

        MoveGenerator.unmake(b, castle, undo);
        assertThat(b.hash()).isEqualTo(before);
    }

    @Test
    void makeUnmakeAllLegalMovesFromKiwipete() 
    {
        // Kiwipete is dense — captures, castling, promotions, en-passant all live here
        Board b = Fen.parse("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
        long before = b.hash();

        for(int m : MoveGenerator.generateLegal(b))
        {
            UndoInfo undo = new UndoInfo();
            MoveGenerator.make(b, m, undo);
            MoveGenerator.unmake(b, m, undo);
            assertThat(b.hash())
                    .as("hash not restored after %s", Move.toUci(m))
                    .isEqualTo(before);
        }
    }

    @Test
    void hashesAreWellDistributed()
    {
        // Very rough sanity check: 100 different positions should give 100 distinct hashes.
        // Not a rigorous statistical test — just catches "the hash is always 0" or
        // "the hash only depends on side to move" style bugs.
        java.util.Set<Long> hashes = new java.util.HashSet<>();
        Board b = Board.startingPosition();
        UndoInfo undo = new UndoInfo();

        hashes.add(b.hash());
        for(int i = 0; i < 100; i++)
        {
            var moves = MoveGenerator.generateLegal(b);
            if(moves.isEmpty()) 
                break;
            int m = moves.get(i % moves.size());
            MoveGenerator.make(b, m, undo);
            hashes.add(b.hash());
        }
        // At least 80 distinct (some will collide because we're deterministically
        // picking a move index, but should be nowhere near all duplicates)
        assertThat(hashes.size()).isGreaterThan(80);
    }
}