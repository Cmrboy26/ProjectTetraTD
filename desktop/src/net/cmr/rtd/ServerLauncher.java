package net.cmr.rtd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3NativesLoader;
import com.esotericsoftware.kryonet.Server;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.GameManager.GameManagerDetails;
import net.cmr.rtd.game.GameSave;
import net.cmr.util.Log;

public class ServerLauncher {
    
    public ServerLauncher() {
        GameManagerDetails details = new GameManagerDetails();
        details.actAsServer(true);
        details.setMaxPlayers(4);

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

    public static void main(String[] args) {
        Log.initializeLog();

        Lwjgl3NativesLoader.load();
        Gdx.files = new Lwjgl3Files();

        new ServerLauncher();
    }

}
