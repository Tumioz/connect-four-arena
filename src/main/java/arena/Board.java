package arena;

import java.util.ArrayList;
import java.util.List;

/** Connect Four board. Row 0 is the bottom. Cells hold 0 (empty), 1 (red, moves first) or 2 (yellow). */
public final class Board {
    public static final int ROWS = 6, COLS = 7;
    private static final int[] ORDER = {3, 2, 4, 1, 5, 0, 6};  // centre first: better pruning and better play
    private static final int[][] DIRS = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
    private final byte[][] cell = new byte[ROWS][COLS];
    private final int[] height = new int[COLS];
    private int moves;

    public static Board of(int... cols) {
        Board b = new Board();
        for (int c : cols) {
            if (!b.canPlay(c)) throw new IllegalArgumentException("Illegal move in column " + c);
            b.drop(c);
        }
        return b;
    }

    public byte turn() { return (byte) (moves % 2 == 0 ? 1 : 2); }
    public boolean canPlay(int c) { return c >= 0 && c < COLS && height[c] < ROWS; }
    public boolean isFull() { return moves == ROWS * COLS; }
    public void drop(int c) { cell[height[c]][c] = turn(); height[c]++; moves++; }
    public void undo(int c) { height[c]--; cell[height[c]][c] = 0; moves--; }
    public boolean wins(int c) { return wins(c, turn()); }

    /** Would player p complete four in a row by dropping a disc in column c? Column c must not be full. */
    public boolean wins(int c, byte p) {
        int r = height[c];
        cell[r][c] = p;
        boolean win = false;
        for (int[] d : DIRS) {
            if (1 + run(r, c, d[0], d[1], p) + run(r, c, -d[0], -d[1], p) >= 4) { win = true; break; }
        }
        cell[r][c] = 0;
        return win;
    }

    private int run(int r, int c, int dr, int dc, byte p) {
        int n = 0;
        r += dr; c += dc;
        while (r >= 0 && r < ROWS && c >= 0 && c < COLS && cell[r][c] == p) { n++; r += dr; c += dc; }
        return n;
    }

    public List<Integer> legal() {
        List<Integer> out = new ArrayList<>(COLS);
        for (int c : ORDER) if (height[c] < ROWS) out.add(c);
        return out;
    }

    /** Position value for player p: open lines of four that p can still complete, minus the opponent's, plus a centre bonus. */
    public int score(byte p) {
        byte q = (byte) (3 - p);
        int s = 0;
        for (int r = 0; r < ROWS; r++) { if (cell[r][3] == p) s += 3; else if (cell[r][3] == q) s -= 3; }
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                for (int[] d : DIRS) {
                    int er = r + 3 * d[0], ec = c + 3 * d[1];
                    if (er < 0 || er >= ROWS || ec < 0 || ec >= COLS) continue;
                    int mine = 0, theirs = 0;
                    for (int k = 0; k < 4; k++) {
                        byte v = cell[r + k * d[0]][c + k * d[1]];
                        if (v == p) mine++; else if (v == q) theirs++;
                    }
                    if (theirs == 0) s += mine == 3 ? 5 : mine == 2 ? 2 : 0;
                    if (mine == 0) s -= theirs == 3 ? 5 : theirs == 2 ? 2 : 0;
                }
        return s;
    }
}
