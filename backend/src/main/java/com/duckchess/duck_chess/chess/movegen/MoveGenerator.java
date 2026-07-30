package com.duckchess.duck_chess.chess.movegen;

import com.duckchess.duck_chess.chess.Board;
import com.duckchess.duck_chess.chess.Color;
import com.duckchess.duck_chess.chess.Move;
import com.duckchess.duck_chess.chess.Piece;
import com.duckchess.duck_chess.chess.PieceType;
import com.duckchess.duck_chess.chess.Square;
import com.duckchess.duck_chess.chess.Zobrist;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates pseudo-legal moves for the side to move.
 *
 * Pseudo-legal means "the piece could move like this on paper" — includes
 * moves that leave your own king in check. Legality filtering is done by
 * the naive approach: make the move, check whether your king is attacked,
 * unmake. See {@link #generateLegal(Board)}.
 */
public final class MoveGenerator 
{

    private MoveGenerator() {}

    public static List<Integer> generatePseudoLegal(Board board) 
    {
        List<Integer> moves = new ArrayList<>(64);
        Color side = board.sideToMove();

        for(int from = 0; from < 64; from++) 
        {
            byte piece = board.get(from);
            if(piece == Piece.EMPTY || piece == Piece.DUCK) continue;
            if(!Piece.isColor(piece, side)) continue;

            PieceType type = Piece.typeOf(piece);
            switch(type) 
            {
                case PAWN   -> genPawnMoves(board, from, side, moves);
                case KNIGHT -> genKnightMoves(board, from, side, moves);
                case BISHOP -> genSliderMoves(board, from, side, AttackTables.BISHOP_DIRECTIONS, moves);
                case ROOK   -> genSliderMoves(board, from, side, AttackTables.ROOK_DIRECTIONS, moves);
                case QUEEN  -> genSliderMoves(board, from, side, AttackTables.QUEEN_DIRECTIONS, moves);
                case KING   -> genKingMoves(board, from, side, moves);
            }
        }
        return moves;
    }

    public static List<Integer> generateLegal(Board board) 
    {
        List<Integer> pseudo = generatePseudoLegal(board);
        List<Integer> legal = new ArrayList<>(pseudo.size());
        Color us = board.sideToMove();
        UndoInfo undo = new UndoInfo();

        for(int move : pseudo) 
        {
            make(board, move, undo);
            if(!AttackDetector.isInCheck(board, us)) 
                {
                legal.add(move);
            }
            unmake(board, move, undo);
        }
        return legal;
    }


    private static void genPawnMoves(Board board, int from, Color side, List<Integer> out) 
    {
        int direction   = (side == Color.WHITE) ?  1 : -1;
        int startRank   = (side == Color.WHITE) ?  1 : 6;
        int promoRank   = (side == Color.WHITE) ?  7 : 0;

        int fromFile = Square.file(from);
        int fromRank = Square.rank(from);


        int oneStepRank = fromRank + direction;
        if(oneStepRank >= 0 && oneStepRank < 8) 
        {
            int oneStep = Square.of(fromFile, oneStepRank);
            if(board.get(oneStep) == Piece.EMPTY) 
            {
                if(oneStepRank == promoRank) 
                {
                    addPromotions(out, from, oneStep, false);
                } 
                else
                {
                    out.add(Move.quiet(from, oneStep));
                    if(fromRank == startRank) 
                    {
                        int twoStep = Square.of(fromFile, fromRank + 2 * direction);
                        if(board.get(twoStep) == Piece.EMPTY) 
                        {
                            out.add(Move.doublePush(from, twoStep));
                        }
                    }
                }
            }
        }

        int[] captureTargets = (side == Color.WHITE)
                ? AttackTables.WHITE_PAWN_ATTACKS[from]
                : AttackTables.BLACK_PAWN_ATTACKS[from];

        for(int target : captureTargets)
        {
            byte piece = board.get(target);
            if(piece != Piece.EMPTY && piece != Piece.DUCK && !Piece.isColor(piece, side))
            {
                if(Square.rank(target) == promoRank) 
                {
                    addPromotions(out, from, target, true);
                } 
                else 
                {
                    out.add(Move.capture(from, target));
                }
            } 
            else if(target == board.enPassantSquare()) 
            {
                out.add(Move.enPassant(from, target));
            }
        }
    }

    private static void addPromotions(List<Integer> out, int from, int to, boolean isCapture) 
    {
        out.add(Move.promotion(from, to, Move.PROMO_QUEEN,  isCapture));
        out.add(Move.promotion(from, to, Move.PROMO_ROOK,   isCapture));
        out.add(Move.promotion(from, to, Move.PROMO_BISHOP, isCapture));
        out.add(Move.promotion(from, to, Move.PROMO_KNIGHT, isCapture));
    }

    private static void genKnightMoves(Board board, int from, Color side, List<Integer> out) 
    {
        for(int target : AttackTables.KNIGHT_ATTACKS[from])
        {
            addNonSliderTarget(board, from, target, side, out);
        }
    }

    private static void genKingMoves(Board board, int from, Color side, List<Integer> out) 
    {
        for(int target : AttackTables.KING_ATTACKS[from]) 
        {
            addNonSliderTarget(board, from, target, side, out);
        }
        addCastles(board, from, side, out);
    }

    private static void addNonSliderTarget(Board board, int from, int target, Color side, List<Integer> out) 
    {
        byte piece = board.get(target);
        if(piece == Piece.EMPTY) 
        {
            out.add(Move.quiet(from, target));
        } 
        else if(piece == Piece.DUCK) 
        {
            // Duck blocks knight/king landing (knights that jump are per duck-chess rule;
            // handled by variant layer, not here — here the duck simply blocks).
            // For standard chess this branch never fires because there's no duck.
        } 
        else if(!Piece.isColor(piece, side)) 
        {
            out.add(Move.capture(from, target));
        }

    }

    private static void addCastles(Board board, int from, Color side, List<Integer> out) 
    {
        int kingRight  = (side == Color.WHITE) ? Board.CASTLE_WHITE_KING  : Board.CASTLE_BLACK_KING;
        int queenRight = (side == Color.WHITE) ? Board.CASTLE_WHITE_QUEEN : Board.CASTLE_BLACK_QUEEN;
        int kingE = (side == Color.WHITE) ? Square.E1 : Square.E8;
        int rookH = (side == Color.WHITE) ? Square.H1 : Square.H8;
        int rookA = (side == Color.WHITE) ? Square.A1 : Square.A8;
        int kingG = (side == Color.WHITE) ? Square.G1 : Square.G8;
        int kingC = (side == Color.WHITE) ? Square.C1 : Square.C8;
        int kingF = (side == Color.WHITE) ? Square.F1 : Square.F8;
        int kingD = (side == Color.WHITE) ? Square.D1 : Square.D8;
        int rookB = (side == Color.WHITE) ? Square.B1 : Square.B8;
        Color enemy = side.opponent();


        if(from != kingE) return;


        if(board.canCastle(kingRight)
                && board.get(rookH) == Piece.encode(side, PieceType.ROOK)
                && board.get(kingF) == Piece.EMPTY
                && board.get(kingG) == Piece.EMPTY
                && !AttackDetector.isAttacked(board, kingE, enemy)
                && !AttackDetector.isAttacked(board, kingF, enemy)
                && !AttackDetector.isAttacked(board, kingG, enemy)) 
        {
            out.add(Move.castle(kingE, kingG));
        }

        if(board.canCastle(queenRight)
                && board.get(rookA) == Piece.encode(side, PieceType.ROOK)
                && board.get(kingD) == Piece.EMPTY
                && board.get(kingC) == Piece.EMPTY
                && board.get(rookB) == Piece.EMPTY
                && !AttackDetector.isAttacked(board, kingE, enemy)
                && !AttackDetector.isAttacked(board, kingD, enemy)
                && !AttackDetector.isAttacked(board, kingC, enemy)) 
        {
            out.add(Move.castle(kingE, kingC));
        }
    }

 
    private static void genSliderMoves(Board board, int from, Color side, int[][] directions, List<Integer> out) {
        int fromFile = Square.file(from);
        int fromRank = Square.rank(from);

        for(int[] dir : directions)
        {
            int file = fromFile + dir[0];
            int rank = fromRank + dir[1];
            while(file >= 0 && file < 8 && rank >= 0 && rank < 8) 
            {
                int target = Square.of(file, rank);
                byte piece = board.get(target);
                if(piece == Piece.EMPTY)
                {
                    out.add(Move.quiet(from, target));
                } 
                else if(piece == Piece.DUCK) 
                {
                    break;
                }
                else if(!Piece.isColor(piece, side)) 
                {
                    out.add(Move.capture(from, target));
                    break;
                }
                else 
                {
                    break;
                }
                file += dir[0];
                rank += dir[1];
            }
        }
    }

    public static void make(Board board, int move, UndoInfo undo) 
    {
        int from = Move.from(move);
        int to   = Move.to(move);
        byte movingPiece = board.get(from);
        Color side       = board.sideToMove();

        undo.priorCastlingRights   = board.castlingRights();
        undo.priorEnPassantSquare  = board.enPassantSquare();
        undo.priorHalfmoveClock    = board.halfmoveClock();
        undo.priorDuckSquare       = board.duckSquare();
        undo.capturedPiece         = Move.isEnPassant(move)
                ? Piece.encode(side.opponent(), PieceType.PAWN)
                : board.get(to);

        boolean isPawnMove = Piece.typeOf(movingPiece) == PieceType.PAWN;
        if(Move.isCapture(move) || isPawnMove) 
            {
            board.setHalfmoveClock(0);
        }
        else 
        {
            board.setHalfmoveClock(board.halfmoveClock() + 1);
        }

        board.set(from, Piece.EMPTY);
        board.set(to, movingPiece);

        if(Move.isEnPassant(move))
        {
            int capturedPawnSquare = Square.of(Square.file(to), Square.rank(from));
            board.set(capturedPawnSquare, Piece.EMPTY);
        }

        if(Move.isPromotion(move))
        {
            PieceType promoType = switch (Move.promotion(move)) 
            {
                case Move.PROMO_KNIGHT -> PieceType.KNIGHT;
                case Move.PROMO_BISHOP -> PieceType.BISHOP;
                case Move.PROMO_ROOK   -> PieceType.ROOK;
                case Move.PROMO_QUEEN  -> PieceType.QUEEN;
                default -> throw new IllegalStateException("bad promo");
            };
            board.set(to, Piece.encode(side, promoType));
        }

        if(Move.isCastle(move)) 
        {
            switch(to) 
            {
                case Square.G1 -> { board.set(Square.H1, Piece.EMPTY); board.set(Square.F1, Piece.WHITE_ROOK); }
                case Square.C1 -> { board.set(Square.A1, Piece.EMPTY); board.set(Square.D1, Piece.WHITE_ROOK); }
                case Square.G8 -> { board.set(Square.H8, Piece.EMPTY); board.set(Square.F8, Piece.BLACK_ROOK); }
                case Square.C8 -> { board.set(Square.A8, Piece.EMPTY); board.set(Square.D8, Piece.BLACK_ROOK); }
            }
        }

        int newRights = board.castlingRights();
        if(movingPiece == Piece.WHITE_KING) newRights &= ~(Board.CASTLE_WHITE_KING | Board.CASTLE_WHITE_QUEEN);
        if(movingPiece == Piece.BLACK_KING) newRights &= ~(Board.CASTLE_BLACK_KING | Board.CASTLE_BLACK_QUEEN);
        if(from == Square.H1 || to == Square.H1) newRights &= ~Board.CASTLE_WHITE_KING;
        if(from == Square.A1 || to == Square.A1) newRights &= ~Board.CASTLE_WHITE_QUEEN;
        if(from == Square.H8 || to == Square.H8) newRights &= ~Board.CASTLE_BLACK_KING;
        if(from == Square.A8 || to == Square.A8) newRights &= ~Board.CASTLE_BLACK_QUEEN;
        board.setCastlingRights(newRights);

        if(Move.isDoublePush(move)) 
        {
            int epRank = (side == Color.WHITE) ? 2 : 5;
            board.setEnPassantSquare(Square.of(Square.file(from), epRank));
        }
        else 
        {
            board.setEnPassantSquare(Square.NONE);
        }

        if(side == Color.BLACK) 
        {
            board.setFullmoveNumber(board.fullmoveNumber() + 1);
        }
        board.setSideToMove(side.opponent());
        board.setHash(Zobrist.computeHash(board));
    }

    public static void unmake(Board board, int move, UndoInfo undo) 
    {
        int from = Move.from(move);
        int to   = Move.to(move);

        Color sideThatMoved = board.sideToMove().opponent();
        board.setSideToMove(sideThatMoved);
        if(sideThatMoved == Color.BLACK) 
        {
            board.setFullmoveNumber(board.fullmoveNumber() - 1);
        }

        byte pieceOnTo = board.get(to);
        byte movingPiece = Move.isPromotion(move)
                ? Piece.encode(sideThatMoved, PieceType.PAWN)
                : pieceOnTo;

        board.set(from, movingPiece);
        board.set(to, Piece.EMPTY);

        if(Move.isEnPassant(move))
        {
            int capturedPawnSquare = Square.of(Square.file(to), Square.rank(from));
            board.set(capturedPawnSquare, undo.capturedPiece);
        } 
        else if(Move.isCapture(move))
        {
            board.set(to, undo.capturedPiece);
        }

        if(Move.isCastle(move)) 
        {
            switch(to) 
            {
                case Square.G1 -> { board.set(Square.H1, Piece.WHITE_ROOK); board.set(Square.F1, Piece.EMPTY); }
                case Square.C1 -> { board.set(Square.A1, Piece.WHITE_ROOK); board.set(Square.D1, Piece.EMPTY); }
                case Square.G8 -> { board.set(Square.H8, Piece.BLACK_ROOK); board.set(Square.F8, Piece.EMPTY); }
                case Square.C8 -> { board.set(Square.A8, Piece.BLACK_ROOK); board.set(Square.D8, Piece.EMPTY); }
            }
        }

        board.setCastlingRights(undo.priorCastlingRights);
        board.setEnPassantSquare(undo.priorEnPassantSquare);
        board.setHalfmoveClock(undo.priorHalfmoveClock);
        board.setDuckSquare(undo.priorDuckSquare);
        board.setHash(Zobrist.computeHash(board));
    }
}