package com.duckchess.duck_chess.chess.movegen;

/**
 * State snapshot taken before a move is applied.
 *
 * We save the parts of the board that make/unmake needs to restore:
 *   - the captured piece (byte code)
 *   - prior castling rights
 *   - prior en-passant square
 *   - prior halfmove clock
 *   - prior duck square (unchanged for standard chess, matters for duck chess)
 *
 * fullmoveNumber and sideToMove are derived deterministically from the move
 * and don't need to be stored.
 */
public final class UndoInfo {
    public byte capturedPiece;
    public int  priorCastlingRights;
    public int  priorEnPassantSquare;
    public int  priorHalfmoveClock;
    public int  priorDuckSquare;
}