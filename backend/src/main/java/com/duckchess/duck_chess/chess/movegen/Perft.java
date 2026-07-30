package com.duckchess.duck_chess.chess.movegen;

import com.duckchess.duck_chess.chess.Board;
import com.duckchess.duck_chess.chess.Move;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
/**
 * Perft (performance test) — counts leaf nodes at depth N from a position.
 *
 * The count itself is meaningless, but the numbers are known-correct for many
 * standard test positions. Matching the reference counts is the strongest
 * confidence signal that move generation is correct: any bug (missing en
 * passant, wrong castling right update, dropped promotion) shows up as an
 * off-by-N discrepancy.
 *
 * This is a pure read of the engine — no side effects, no logging.
 */
public final class Perft 
{

    private Perft() {}

    /**
     * Count leaf nodes at exactly {@code depth} plies from the current board.
     * Depth 0 counts the position itself as 1 node.
     */
    public static long count(Board board, int depth) 
    {
        if(depth == 0)
            return 1L;

        List<Integer> moves = MoveGenerator.generateLegal(board);
        if(depth == 1)
            return moves.size();

        long nodes = 0L;
        UndoInfo undo = new UndoInfo();
        for(int move : moves)
        {
            MoveGenerator.make(board, move, undo);
            nodes += count(board, depth - 1);
            MoveGenerator.unmake(board, move, undo);
        }
        return nodes;
    }

    /**
     * Divide — for each root move, print/return the perft count of the resulting position.
     * Useful for narrowing down which move causes an incorrect subtree count.
     */
    public static Map<String, Long> divide(Board board, int depth)
    {
        Map<String, Long> result = new LinkedHashMap<>();
        List<Integer> moves = MoveGenerator.generateLegal(board);
        UndoInfo undo = new UndoInfo();
        for(int move : moves)
        {
            MoveGenerator.make(board, move, undo);
            long childCount = (depth <= 1) ? 1L : count(board, depth - 1);
            result.put(Move.toUci(move), childCount);
            MoveGenerator.unmake(board, move, undo);
        }
        return result;
    }
}