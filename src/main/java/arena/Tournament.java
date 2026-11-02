package arena;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public final class Tournament {
    public record Standing(String name, int wins, int draws, int losses) {
        public int points() { return 3 * wins + draws; }
    }

    private Tournament() {}

    /** Plays one game and returns 0 for a draw, otherwise the winner (1 red, 2 yellow). The first plies are random so repeated games differ. */
    public static int play(Bot red, Bot yellow, int randomPlies) {
        Board b = new Board();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int ply = 0; !b.isFull(); ply++) {
            int c;
            if (ply < randomPlies) { List<Integer> m = b.legal(); c = m.get(rnd.nextInt(m.size())); }
            else c = (b.turn() == 1 ? red : yellow).chooseMove(b);
            if (b.wins(c)) return b.turn();
            b.drop(c);
        }
        return 0;
    }

    /** Round robin. Every pair plays gamesPerPair games and swaps colours each game. Pairs run in parallel. */
    public static List<Standing> run(List<String> names, int gamesPerPair) {
        int n = names.size();
        int[] w = new int[n], d = new int[n], l = new int[n];
        List<int[]> pairs = new ArrayList<>();
        for (int i = 0; i < n; i++) for (int j = i + 1; j < n; j++) pairs.add(new int[]{i, j});
        pairs.parallelStream().forEach(p -> {
            for (int g = 0; g < gamesPerPair; g++) {
                boolean firstIsRed = g % 2 == 0;
                Bot first = Bots.create(names.get(p[0])), second = Bots.create(names.get(p[1]));
                int result = firstIsRed ? play(first, second, 2) : play(second, first, 2);
                int winner = result == 0 ? -1 : ((result == 1) == firstIsRed ? p[0] : p[1]);
                synchronized (w) {
                    if (winner < 0) { d[p[0]]++; d[p[1]]++; }
                    else { w[winner]++; l[winner == p[0] ? p[1] : p[0]]++; }
                }
            }
        });
        return IntStream.range(0, n).mapToObj(i -> new Standing(names.get(i), w[i], d[i], l[i]))
                .sorted(Comparator.comparingInt(Standing::points).reversed()).toList();
    }
}
