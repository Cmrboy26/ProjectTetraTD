package net.cmr.rtd.game.world;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.screen.GameScreen;

public class UpdateData {

    final GameManager manager;
    final GameScreen screen;
    final boolean isServer;

    public UpdateData(GameManager manager) {
        this.manager = manager;
        this.screen = null;
        this.isServer = true;
    }

    // Note: technically the constructor variables shouldn't be null, but for debugging
    // on the clientside without actually creating a GameScreen, a null value can be passed
    public UpdateData(GameScreen screen) {
        this.screen = screen;
        this.manager = null;
        this.isServer = false;
    }

    public boolean isServer() {
        return isServer;
    }

    public boolean isClient() {
        return !isServer;
    }
    
}
