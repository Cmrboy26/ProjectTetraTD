package net.cmr.rtd.screen;

import com.esotericsoftware.kryo.util.Null;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.stream.GameStream;
import net.cmr.util.AbstractScreenEX;

public class GameScreen extends AbstractScreenEX {
    
    GameStream stream;

    public GameScreen(GameStream stream, @Null GameManager gameManager) {
        super(INITIALIZE_ALL);
        this.stream = stream;
    }

}
