package net.cmr.rtd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3NativesLoader;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.GameManager.GameManagerDetails;
import net.cmr.rtd.game.GameSave;
import net.cmr.util.Log;

public class ServerLauncher {
    
    public static void main(String[] args) {
        Log.initializeLog();
        GameManagerDetails details = new GameManagerDetails();
        details.actAsServer(true);
        details.setMaxPlayers(4);

        Lwjgl3NativesLoader.load();
        Gdx.files = new Lwjgl3Files();

        GameManager manager = new GameManager(details);
        manager.initialize(new GameSave("host"));
        manager.start();

        while(manager.isRunning()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
