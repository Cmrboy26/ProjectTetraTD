package net.cmr.rtd;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.GameManager.GameManagerDetails;
import net.cmr.rtd.game.GameSave;
import net.cmr.util.Log;

public class ServerLauncher extends ApplicationAdapter {
    
    GameManager manager;
    GameManagerDetails details;

    public ServerLauncher() {
        
    }

    @Override
    public void create() {
        details = new GameManagerDetails();
        details.actAsServer(true);
        details.setMaxPlayers(4);
        details.setUseConsole(true);

        manager = new GameManager(details);
        manager.initialize(new GameSave("host"));
        manager.start();

        //Scanner scanner = new Scanner(System.in);
        while(manager.isRunning()) {
            /*String input = scanner.nextLine();
            if (input.equals("exit")) {
                Log.info("Recieved command: exit. Closing server...");
                manager.stop();
                break;
            }*/
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //scanner.close();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(1);
    }

    @Override
    public void render() {

    }

    public static void main(String[] args) {
        Log.initializeLog();

        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new ServerLauncher(), config);
    }

}
