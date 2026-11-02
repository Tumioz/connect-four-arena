package arena;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/** The bot registry plus three players of increasing strength. Add a bot by registering it below. */
public final class Bots {
    private static final Map<String, Supplier<Bot>> REGISTRY = new LinkedHashMap<>();
    static {
        REGISTRY.put("Random", RandomBot::new);
        REGISTRY.put("Heuristic", HeuristicBot::new);
        REGISTRY.put("Minimax-3", () -> new MinimaxBot(3));
        REGISTRY.put("Minimax-5", () -> new MinimaxBot(5));
    }

    private Bots() {}
    public static List<String> names() { return List.copyOf(REGISTRY.keySet()); }

    public static Bot create(String name) {
        Supplier<Bot> s = REGISTRY.get(name);
        if (s == null) throw new IllegalArgumentException("Unknown bot: " + name);
        return s.get();
    }

    /** Plays any legal column. The baseline every other bot must beat. */
    public static final class RandomBot implements Bot {
        public int chooseMove(Board b) {
            List<Integer> m = b.legal();
            return m.get(ThreadLocalRandom.current().nextInt(m.size()));
        }
    }

    /** Rules of thumb: win if possible, block if necessary, otherwise lean towards the centre. */
    public static final class HeuristicBot implements Bot {
        public int chooseMove(Board b) {
            byte me = b.turn(), opp = (byte) (3 - me);
            List<Integer> legal = b.legal();
            for (int c : legal) if (b.wins(c, me)) return c;
            for (int c : legal) if (b.wins(c, opp)) return c;
            int i = (int) Math.min(legal.size() - 1, Math.abs(ThreadLocalRandom.current().nextGaussian()) * 1.5);
            return legal.get(i);
        }
    }

    /** Negamax search with alpha-beta pruning. Ties between equally good moves are broken at random. */
    public static final class MinimaxBot implements Bot {
        private static final int INF = 1_000_000, WIN = 100_000;
        private final int depth;

        public MinimaxBot(int depth) { this.depth = depth; }

        public int chooseMove(Board b) {
            int bestScore = -INF - 1;
            List<Integer> best = new ArrayList<>();
            for (int c : b.legal()) {
                int s;
                if (b.wins(c)) s = WIN + depth;
                else { b.drop(c); s = -negamax(b, depth - 1, -INF, INF); b.undo(c); }
                if (s > bestScore) { bestScore = s; best.clear(); }
                if (s == bestScore) best.add(c);
            }
            return best.get(ThreadLocalRandom.current().nextInt(best.size()));
        }

        /** Value of the position for the player to move. Faster wins score higher because d is larger when they happen sooner. */
        private int negamax(Board b, int d, int alpha, int beta) {
            if (b.isFull()) return 0;
            if (d == 0) return b.score(b.turn());
            for (int c : b.legal()) if (b.wins(c)) return WIN + d;
            int best = -INF;
            for (int c : b.legal()) {
                b.drop(c);
                int v = -negamax(b, d - 1, -beta, -alpha);
                b.undo(c);
                best = Math.max(best, v);
                alpha = Math.max(alpha, v);
                if (alpha >= beta) break;
            }
            return best;
        }
    }
}
