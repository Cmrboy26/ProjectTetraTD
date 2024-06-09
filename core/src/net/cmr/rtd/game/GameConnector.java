package net.cmr.rtd.game;

import net.cmr.rtd.RetroTowerDefense;
import net.cmr.rtd.game.GameManager.GameManagerDetails;
import net.cmr.rtd.game.files.QuestFile;
import net.cmr.rtd.game.packets.ConnectPacket;
import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.stream.GameStream;
import net.cmr.rtd.game.stream.GameStream.PacketListener;
import net.cmr.rtd.game.stream.LocalGameStream;
import net.cmr.rtd.screen.GameScreen;
import net.cmr.util.Log;
import net.cmr.util.Settings;

/**
 * Holds static helper methods to join singleplayer/multiplayer games and to host multiplayer games.
 */
public class GameConnector {
    
    public static void startSingleplayerGame(QuestFile quest, int team) {
        GameManagerDetails details = new GameManagerDetails();
        details.setMaxPlayers(1);
        details.setHostedOnline(false);

        GameManager manager = new GameManager(details);
		LocalGameStream[] streams = LocalGameStream.createStreamPair();
		GameStream clientsideStream = streams[0];
		GameStream serversideStream = streams[1];

        serversideStream.addListener(new PacketListener() {
			@Override
			public void packetReceived(Packet packet) {
				Log.debug("Server received packet: " + packet);
			}
		});
		clientsideStream.addListener(new PacketListener() {
			@Override
			public void packetReceived(Packet packet) {
				Log.debug("Client received packet: " + packet);
			}
		});

        GameScreen screen = new GameScreen(clientsideStream, manager, null, null, team);

        manager.initialize(quest);
		RetroTowerDefense.getInstance().setScreen(screen);
		manager.start();
		manager.onNewConnection(serversideStream);
		
		clientsideStream.sendPacket(new ConnectPacket(Settings.getPreferences().getString(Settings.USERNAME), team));
    }

    public static void joinMultiplayerGame(QuestFile quest, String ip, int port) {

    }

    public static void hostMultiplayerGame(QuestFile quest, int port) {

    }

}
