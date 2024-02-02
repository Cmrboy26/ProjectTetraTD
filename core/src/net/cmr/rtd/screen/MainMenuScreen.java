package net.cmr.rtd.screen;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.GameManager.GameManagerDetails;
import net.cmr.rtd.game.packets.ConnectPacket;
import net.cmr.rtd.game.stream.LocalGameStream;
import net.cmr.util.AbstractScreenEX;

public class MainMenuScreen extends AbstractScreenEX {
    
    LocalGameStream clientSender, serverSender;

    public MainMenuScreen() {
        super(INITIALIZE_ALL);

        GameManagerDetails details = new GameManagerDetails();
        details.actAsServer(false);
        GameManager gameManager = new GameManager(details);

        LocalGameStream[] streamPairs = LocalGameStream.createStreamPair();
        clientSender = streamPairs[0];
        serverSender = streamPairs[1];

        clientSender.addListener((packet, it) -> {
            System.out.println("Client received packet: " + packet);
        });
        serverSender.addListener((packet, it) -> {
            System.out.println("Server received packet: " + packet);
        });

        gameManager.onPlayerConnect(serverSender);
        clientSender.sendPacket(new ConnectPacket("Username"));
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        clientSender.update();
        serverSender.update();
    }

}
