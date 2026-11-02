package arena;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArenaTest {
    @Test void detectsVerticalWin() { assertTrue(Board.of(0, 1, 0, 1, 0, 1).wins(0)); }
    @Test void detectsHorizontalWin() { assertTrue(Board.of(0, 0, 1, 1, 2, 2).wins(3)); }
    @Test void rejectsAFullColumn() { assertThrows(IllegalArgumentException.class, () -> Board.of(0, 0, 0, 0, 0, 0, 0)); }

    @Test void botsTakeAnImmediateWin() {
        Board b = Board.of(0, 1, 0, 1, 0, 1);
        assertEquals(0, new Bots.HeuristicBot().chooseMove(b));
        assertEquals(0, new Bots.MinimaxBot(3).chooseMove(b));
    }

    @Test void botsBlockAnImmediateLoss() {
        Board b = Board.of(0, 1, 0, 1, 0);
        assertEquals(0, new Bots.HeuristicBot().chooseMove(b));
        assertEquals(0, new Bots.MinimaxBot(3).chooseMove(b));
    }

    @Test void minimaxBeatsRandom() {
        int wins = 0;
        for (int g = 0; g < 20; g++) {
            boolean minimaxRed = g % 2 == 0;
            int result = minimaxRed ? Tournament.play(new Bots.MinimaxBot(3), new Bots.RandomBot(), 2)
                                    : Tournament.play(new Bots.RandomBot(), new Bots.MinimaxBot(3), 2);
            if (result == (minimaxRed ? 1 : 2)) wins++;
        }
        assertTrue(wins >= 16, "Minimax won only " + wins + " of 20");
    }

    @Test void tournamentAccountsForEveryGame() {
        for (Tournament.Standing s : Tournament.run(List.of("Random", "Heuristic"), 6))
            assertEquals(6, s.wins() + s.draws() + s.losses());
    }
}
