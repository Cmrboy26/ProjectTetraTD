package net.cmr.rtd.game;

import net.cmr.rtd.game.stream.GameStream;

/**
 * Represents a player in a game.
 * This class is used to store {@link GameStream} and other player specific information.
 */
public class GamePlayer {
    
    public static final String GAME_FULL = "The game is full.";
    public static final String USERNAME_TOO_LONG = "The username is too long.";
    public static final String USERNAME_TAKEN = "The username is already taken.";

    public static final int USERNAME_LENGTH = 16;

    private final GameStream stream;
    private final String username;

    public GamePlayer(GameStream stream, String username) {
        this.stream = stream;
        this.username = username;
    }

    public GameStream getStream() { return stream; }
    public String getUsername() { return username; }

}
