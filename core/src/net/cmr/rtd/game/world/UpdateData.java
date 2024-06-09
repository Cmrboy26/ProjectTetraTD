package net.cmr.rtd.game.world;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.storage.TeamInventory;
import net.cmr.rtd.screen.GameScreen;

public class UpdateData {

    private final GameManager manager;
    private final GameScreen screen;
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

    public World getWorld() {
        if (isServer) {
            return manager.getWorld();
        } else {
            return screen.getWorld();
        }
    }

    public GameManager getManager() {
        return manager;
    }
    public GameScreen getScreen() {
        return screen;
    }

    public TeamInventory getInventory(int team) {
        if (isServer) {
            return manager.getTeam(team).getInventory();
        } else {
            return screen.getTeamInventory();
        }
    }
    
}
