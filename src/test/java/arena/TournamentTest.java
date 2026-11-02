package arena;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TournamentTest {
    private static final List<String> THREE = List.of("Random", "Heuristic", "Minimax-3");

    @Test void playReturnsAValidResult() {
        int result = Tournament.play(new Bots.RandomBot(), new Bots.RandomBot(), 2);
        assertTrue(result >= 0 && result <= 2);
    }

    @Test void pointsAreThreeForAWinAndOneForADraw() {
        assertEquals(9, new Tournament.Standing("x", 2, 3, 1).points());
    }

    @Test void everyBotPlaysTheExpectedNumberOfGames() {
        for (Tournament.Standing s : Tournament.run(THREE, 4))
            assertEquals(8, s.wins() + s.draws() + s.losses(), s.name());   // 2 opponents x 4 games
    }

    @Test void totalWinsEqualTotalLosses() {
        var standings = Tournament.run(THREE, 4);
        assertEquals(standings.stream().mapToInt(Tournament.Standing::wins).sum(),
                     standings.stream().mapToInt(Tournament.Standing::losses).sum());
    }

    @Test void standingsAreSortedByPoints() {
        var s = Tournament.run(THREE, 4);
        for (int i = 1; i < s.size(); i++) assertTrue(s.get(i - 1).points() >= s.get(i).points());
    }

    @Test void strongerBotRanksFirst() {
        assertEquals("Minimax-3", Tournament.run(List.of("Random", "Minimax-3"), 10).get(0).name());
    }

    @Test void zeroGamesGivesEmptyRecords() {
        for (Tournament.Standing s : Tournament.run(List.of("Random", "Heuristic"), 0)) assertEquals(0, s.points());
    }
}
