package net.cmr.rtd.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

import com.esotericsoftware.kryonet.Client;

import net.cmr.rtd.RetroTowerDefense;
import net.cmr.rtd.game.GameManager.GameManagerDetails;
import net.cmr.rtd.game.files.QuestFile;
import net.cmr.rtd.game.packets.ConnectPacket;
import net.cmr.rtd.game.packets.GameInfoPacket;
import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.packets.PacketEncryption;
import net.cmr.rtd.game.stream.GameStream;
import net.cmr.rtd.game.stream.GameStream.PacketListener;
import net.cmr.rtd.game.stream.LocalGameStream;
import net.cmr.rtd.game.stream.OnlineGameStream;
import net.cmr.rtd.game.world.TeamData;
import net.cmr.rtd.game.world.TeamData.NullTeamException;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.screen.GameScreen;
import net.cmr.rtd.screen.MainMenuScreen;
import net.cmr.rtd.screen.TeamSelectionScreen;
import net.cmr.util.Log;
import net.cmr.util.Settings;

/**
 * Holds static helper methods to join singleplayer/multiplayer games and to host multiplayer games.
 */
public class GameConnector {
    
    public static void startSingleplayerGame(QuestFile quest) {
		GameManagerDetails details = new GameManagerDetails();
		details.setMaxPlayers(1);
		details.setHostedOnline(false);
		Function<Integer, Void> joinGameWithTeam = new Function<Integer, Void>() {
			@Override
			public Void apply(Integer team) {
		
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
				return null;
			}
		};
		Consumer<GameInfoPacket> callback = new Consumer<GameInfoPacket>() {
			@Override
			public void accept(GameInfoPacket t) {
				RetroTowerDefense.getInstance().setScreen(new TeamSelectionScreen(joinGameWithTeam, t.teams));
			}
		};
		getGameInfo(quest, details.getMaxPlayers(), callback);
    }

    public static void joinMultiplayerGame(QuestFile quest, String ip, int port) {
		
		Function<Integer, Void> joinGameWithTeam = new Function<Integer, Void>() {
			@Override
			public Void apply(Integer team) {
				try {
					RetroTowerDefense game = RetroTowerDefense.getInstance(RetroTowerDefense.class);
					Client client = new Client(30000, 30000);
					OnlineGameStream.registerPackets(client.getKryo());
					OnlineGameStream stream = new OnlineGameStream(new PacketEncryption(), client);
			
					Log.info("Connecting to " + ip + ":" + port + "...");
					client.start();
					try {
						client.connect(5000, ip, port);
					} catch (Exception e) {
						Log.error("Failed to connect to server.", e);
						throw e;
					}
					stream.addListener(new PacketListener() {
						@Override
						public void packetReceived(Packet packet) {
							Log.debug("Client received packet: " + packet);
						}
					});
					Log.info("Connected.");
					GameScreen screen = new GameScreen(stream, null, null, null, team);
					game.setScreen(screen);
			
					stream.sendPacket(new ConnectPacket(Settings.getPreferences().getString(Settings.USERNAME), team));
				} catch (Exception e) {
					e.printStackTrace();
					RetroTowerDefense.getInstance().setScreen(new MainMenuScreen());
				}
				return null;
			}
		};
		Consumer<GameInfoPacket> callback = new Consumer<GameInfoPacket>() {
			@Override
			public void accept(GameInfoPacket t) {
				RetroTowerDefense.getInstance().setScreen(new TeamSelectionScreen(joinGameWithTeam, t.teams));
			}
		};

		getGameInfo("localhost", 11265, callback);
    }

    public static void hostMultiplayerGame(QuestFile quest, int port) {
		// TODO: Implement this method
		//HostScreen screen = new HostScreen(details, save, lsave, teams);
		//setScreen(screen);
    }

	private static void getGameInfo(QuestFile file, int maxPlayers, Consumer<GameInfoPacket> callback) {
		RetroTowerDefense game = RetroTowerDefense.getInstance(RetroTowerDefense.class);
		World world = file.loadWorld();
		
		ArrayList<Integer> availableTeams = new ArrayList<Integer>();
        int teamCounter = 0;
        for (int i = 0; i < GameManager.MAX_TEAMS; i++) {
            try {
                TeamData team = new TeamData(world, i);
                // If this succeeded, then this team exists in the world.
                teamCounter++;
				availableTeams.add(i);
            } catch (NullTeamException e) {
                // This team does not exist in the world.
            }
        }
		int[] teams = new int[availableTeams.size()];
		for (int i = 0; i < availableTeams.size(); i++) {
			teams[i] = availableTeams.get(i);
		}
		GameInfoPacket packet = new GameInfoPacket(teams, 0, maxPlayers);
		callback.accept(packet);
	}

	private static void getGameInfo(String ip, int port, Consumer<GameInfoPacket> callback) {
		RetroTowerDefense game = RetroTowerDefense.getInstance(RetroTowerDefense.class);
		try {
			Client client = new Client();
			OnlineGameStream.registerPackets(client.getKryo());
			OnlineGameStream stream = new OnlineGameStream(new PacketEncryption(), client);

			Log.info("Connecting to " + ip + ":" + port + "...");
			client.start();
			try {
				client.connect(5000, ip, port);
			} catch (Exception e) {
				Log.error("Failed to connect to server.", e);
				throw e;
			}
			Log.info("Connected.");
			stream.sendPacket(new GameInfoPacket());
			stream.addListener(new PacketListener() {
				@Override
				public void packetReceived(Packet packet) {
					if (packet instanceof GameInfoPacket) {
						GameInfoPacket info = (GameInfoPacket) packet;
						Log.info("Received game info: " + info.teams + " teams, " + info.players + " players, " + info.maxPlayers + " max players.");
						callback.accept(info);
						stream.onClose();
					}
				}
			});
			while (stream.isOpen()) {
				stream.update();
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			game.setScreen(new MainMenuScreen());
		}
	}

}
