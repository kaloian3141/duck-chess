package com.duckchess.duck_chess.chess.variant;

import com.duckchess.duck_chess.chess.Board;

import java.util.List;

/**
 * A chess variant. Provides variant-specific rules: how to generate legal moves,
 * and how to detect game-end.
 *
 * The core engine (movegen, board, hashing) is variant-agnostic.
 * This interface is the plug point where rules diverge.
 */
public interface Variant {

    String name();

    List<Integer> legalMoves(Board board);
    GameResult result(Board board, List<Integer> legalMoves, List<Long> pastHashes);
}