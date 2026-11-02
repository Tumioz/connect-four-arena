package arena;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {
    @Test void newBoardStartsEmptyWithRedToMove() {
        Board b = new Board();
        assertEquals(1, b.turn());
        assertEquals(7, b.legal().size());
        assertFalse(b.isFull());
    }

    @Test void turnsAlternate() {
        assertEquals(2, Board.of(3).turn());
        assertEquals(1, Board.of(3, 3).turn());
    }

    @Test void legalMovesAreCentreFirst() {
        assertEquals(List.of(3, 2, 4, 1, 5, 0, 6), new Board().legal());
    }

    @Test void fullColumnIsNotPlayable() {
        Board b = Board.of(0, 0, 0, 0, 0, 0);
        assertFalse(b.canPlay(0));
        assertFalse(b.canPlay(-1));
        assertFalse(b.canPlay(7));
        assertTrue(b.canPlay(1));
        assertEquals(List.of(3, 2, 4, 1, 5, 6), b.legal());
    }

    @Test void ofRejectsAnIllegalMove() {
        assertThrows(IllegalArgumentException.class, () -> Board.of(0, 0, 0, 0, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> Board.of(9));
    }

    @Test void undoRestoresThePosition() {
        Board b = Board.of(3, 4);
        int before = b.score((byte) 1);
        b.drop(2);
        b.undo(2);
        assertEquals(1, b.turn());
        assertEquals(before, b.score((byte) 1));
        assertTrue(b.canPlay(2));
    }

    @Test void detectsVerticalWin() {
        Board b = Board.of(0, 1, 0, 1, 0, 1);
        assertTrue(b.wins(0));
        assertFalse(b.wins(2));
    }

    @Test void detectsHorizontalWin() { assertTrue(Board.of(0, 0, 1, 1, 2, 2).wins(3)); }

    @Test void detectsRisingDiagonalWin() { assertTrue(Board.of(0, 1, 1, 2, 6, 2, 2, 3, 3, 3).wins(3)); }

    @Test void detectsFallingDiagonalWin() { assertTrue(Board.of(6, 5, 5, 4, 0, 4, 4, 3, 3, 3).wins(3)); }

    @Test void threeInARowIsNotAWin() { assertFalse(Board.of(0, 0, 1, 1).wins(2)); }

    @Test void canCheckAThreatForEitherPlayer() {
        Board b = Board.of(0, 1, 0, 1, 0);          // yellow to move, red threatens column 0
        assertTrue(b.wins(0, (byte) 1));
        assertFalse(b.wins(0, (byte) 2));
        assertFalse(b.wins(0));
    }

    @Test void scoreIsZeroOnAnEmptyBoard() { assertEquals(0, new Board().score((byte) 1)); }

    @Test void scoreFavoursTheCentre() {
        assertTrue(Board.of(3).score((byte) 1) > Board.of(0).score((byte) 1));
    }

    @Test void scoreIsSymmetricBetweenPlayers() {
        Board b = Board.of(3, 2, 3, 4, 2);
        assertEquals(b.score((byte) 1), -b.score((byte) 2));
    }
}
