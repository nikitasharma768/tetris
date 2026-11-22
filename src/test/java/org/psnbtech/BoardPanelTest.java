package org.psnbtech;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the board's game logic without opening a window.
 * The board is 10 columns wide and 22 rows tall (20 visible + 2 hidden).
 */
class BoardPanelTest {

    private BoardPanel board;

    @BeforeEach
    void setUp() {
        board = new BoardPanel(null);   // the game window is not needed for logic tests
        board.clear();
    }

    @Test
    void everyPieceFitsAtItsSpawnPointOnEmptyBoard() {
        for (TileType type : TileType.values()) {
            assertTrue(
                board.isValidAndEmpty(type, type.getSpawnColumn(), type.getSpawnRow(), 0),
                type + " should fit at its spawn point");
        }
    }

    @Test
    void pieceCannotGoPastLeftWall() {
        assertFalse(board.isValidAndEmpty(TileType.TypeO, -10, 5, 0));
    }

    @Test
    void pieceCannotGoPastRightWall() {
        assertFalse(board.isValidAndEmpty(TileType.TypeO, 20, 5, 0));
    }

    @Test
    void pieceCannotGoBelowFloor() {
        assertFalse(board.isValidAndEmpty(TileType.TypeO, 4, 30, 0));
    }

    @Test
    void pieceCannotOverlapPlacedPiece() {
        board.addPiece(TileType.TypeO, 4, 10, 0);
        assertFalse(board.isValidAndEmpty(TileType.TypeO, 4, 10, 0));
    }

    @Test
    void clearEmptiesTheBoard() {
        board.addPiece(TileType.TypeO, 4, 10, 0);
        board.clear();
        assertTrue(board.isValidAndEmpty(TileType.TypeO, 4, 10, 0));
    }

    @Test
    void emptyBoardHasNoLinesToClear() {
        assertEquals(0, board.checkLines());
    }

    @Test
    void twoFullBottomRowsAreCleared() {
        // Five 2x2 O-pieces side by side fill the bottom two rows completely
        for (int x = 0; x < 10; x += 2) {
            board.addPiece(TileType.TypeO, x, 20, 0);
        }
        assertEquals(2, board.checkLines());
        // After clearing, the bottom is empty again
        assertTrue(board.isValidAndEmpty(TileType.TypeO, 0, 20, 0));
    }

    @Test
    void oPieceLooksTheSameInEveryRotation() {
        for (int r = 1; r < 4; r++) {
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 2; y++) {
                    assertEquals(
                        TileType.TypeO.isTile(x, y, 0),
                        TileType.TypeO.isTile(x, y, r));
                }
            }
        }
    }
}