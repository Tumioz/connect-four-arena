package arena;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class BotsTest {
    /** How many of the games the subject wins when colours alternate. */
    private static int winsOf(Supplier<Bot> subject, Supplier<Bot> opponent, int games) {
        int wins = 0;
        for (int g = 0; g < games; g++) {
            boolean red = g % 2 == 0;
            int result = red ? Tournament.play(subject.get(), opponent.get(), 2)
                             : Tournament.play(opponent.get(), subject.get(), 2);
            if (result == (red ? 1 : 2)) wins++;
        }
        return wins;
    }

    @Test void registryListsEveryBot() {
        assertEquals(List.of("Random", "Heuristic", "Minimax-3", "Minimax-5"), Bots.names());
    }

    @Test void unknownBotIsRejected() {
        var e = assertThrows(IllegalArgumentException.class, () -> Bots.create("Skynet"));
        assertTrue(e.getMessage().contains("Skynet"));
    }

    @Test void everyBotPlaysALegalMove() {
        Board b = Board.of(0, 0, 0, 0, 0, 0);       // column 0 is full
        for (String name : Bots.names())
            for (int i = 0; i < 10; i++) {
                int move = Bots.create(name).chooseMove(b);
                assertTrue(b.canPlay(move), name + " chose column " + move);
            }
    }

    @Test void botsLeaveTheBoardUnchanged() {
        for (String name : Bots.names()) {
            Board b = Board.of(3, 3, 2);
            int score = b.score((byte) 2);
            List<Integer> legal = b.legal();
            byte turn = b.turn();
            Bots.create(name).chooseMove(b);
            assertEquals(legal, b.legal(), name);
            assertEquals(score, b.score((byte) 2), name);
            assertEquals(turn, b.turn(), name);
        }
    }

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

    @Test void minimaxFindsADoubleThreat() {
        // Red holds the bottom of columns 2 and 3. Playing column 1 or 4 makes an open three that cannot be blocked at both ends.
        int move = new Bots.MinimaxBot(3).chooseMove(Board.of(2, 2, 3, 3));
        assertTrue(move == 1 || move == 4, "chose column " + move);
    }

    @Test void heuristicBeatsRandom() {
        int wins = winsOf(Bots.HeuristicBot::new, Bots.RandomBot::new, 30);
        assertTrue(wins >= 18, "Heuristic won only " + wins + " of 30");
    }

    @Test void minimaxBeatsRandom() {
        int wins = winsOf(() -> new Bots.MinimaxBot(3), Bots.RandomBot::new, 20);
        assertTrue(wins >= 16, "Minimax-3 won only " + wins + " of 20");
    }

    @Test void deeperMinimaxBeatsHeuristic() {
        int wins = winsOf(() -> new Bots.MinimaxBot(5), Bots.HeuristicBot::new, 10);
        assertTrue(wins >= 7, "Minimax-5 won only " + wins + " of 10");
    }
}
