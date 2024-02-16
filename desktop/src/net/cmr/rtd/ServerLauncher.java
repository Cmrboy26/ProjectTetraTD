package net.cmr.rtd;

import java.util.Scanner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3NativesLoader;

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

        Scanner scanner = new Scanner(System.in);
        while(manager.isRunning()) {
            String input = scanner.nextLine();
            if (input.equals("exit")) {
                Log.info("Recieved command: exit. Closing server...");
                manager.stop();
                break;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        scanner.close();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(1);
    }

    public static void main(String[] args) {
        Log.initializeLog();

        Lwjgl3NativesLoader.load();
        Gdx.files = new Lwjgl3Files();

        new ServerLauncher();
    }

}
