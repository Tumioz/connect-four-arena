package arena;

/** A player. Implementations may drop and undo discs while thinking, but must leave the board as they found it. */
public interface Bot {
    int chooseMove(Board board);
}
