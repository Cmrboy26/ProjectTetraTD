package net.cmr.rtd.game;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.esotericsoftware.kryonet.Client;

import net.cmr.rtd.ProjectTetraTD;
import net.cmr.rtd.game.GameManager.GameManagerDetails;
import net.cmr.rtd.game.files.LevelFolder;
import net.cmr.rtd.game.files.QuestFile;
import net.cmr.rtd.game.files.WorldFolder;
import net.cmr.rtd.game.packets.ConnectPacket;
import net.cmr.rtd.game.packets.GameInfoPacket;
import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.packets.PacketEncryption;
import net.cmr.rtd.game.packets.PlayerPositionsPacket;
import net.cmr.rtd.game.stream.GameStream;
import net.cmr.rtd.game.stream.GameStream.PacketListener;
import net.cmr.rtd.game.stream.LocalGameStream;
import net.cmr.rtd.game.stream.OnlineGameStream;
import net.cmr.rtd.game.world.TeamData;
import net.cmr.rtd.game.world.TeamData.NullTeamException;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.screen.GameScreen;
import net.cmr.rtd.screen.LoadingScreen;
import net.cmr.rtd.screen.MainMenuScreen;
import net.cmr.rtd.screen.TeamSelectionScreen;
import net.cmr.rtd.screen.TeamSelectionScreen.ConnectionAttempt;
import net.cmr.rtd.screen.TutorialScreen;
import net.cmr.rtd.screen.LoadingScreen.LogList;
import net.cmr.util.Log;
import net.cmr.util.Settings;

/**
 * Holds static helper methods to join singleplayer/multiplayer games and to
 * host multiplayer games.
 */
public class GameConnector {

	public static void startSingleplayerGame(QuestFile quest) {
		startSingleplayerGame(quest, -1);
	}

	/**
	 * Starts a singleplayer game with the given quest and desired team.
	 * 
	 * @param quest       The quest to play.
	 * @param desiredTeam The team the player wants to join. If -1, the player will
	 *                    be prompted to choose a team.
	 */
	public static void startSingleplayerGame(QuestFile quest, final int desiredTeam) {
		final LogList loadingScreenSupplier = new LogList();
		LoadingScreen loadingScreen = new LoadingScreen(loadingScreenSupplier);

		GameManagerDetails details = new GameManagerDetails();
		details.setMaxPlayers(1);
		details.setHostedOnline(false);
		Consumer<ConnectionAttempt> joinGameWithTeam = new Consumer<ConnectionAttempt>() {
			@Override
			public void accept(ConnectionAttempt attempt) {
				int team = attempt.team;

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

				GameScreen screen = new GameScreen(clientsideStream, manager, details.getPassword(), team);

				manager.initialize(quest);
				ProjectTetraTD.getInstance(ProjectTetraTD.class).setScreenAsynchronous(screen);
				manager.start();
				manager.onNewConnection(serversideStream);

				if (desiredTeam != -1 && manager.areWavesPaused()) {
					manager.resumeWaves();
				}

				clientsideStream
						.sendPacket(new ConnectPacket(Settings.getPreferences().getString(Settings.USERNAME), team));
			}
		};
		Consumer<GameInfoPacket> callback = new Consumer<GameInfoPacket>() {
			@Override
			public void accept(GameInfoPacket t) {
				if (desiredTeam == -1) {
					TeamSelectionScreen teamSelectionScreen = new TeamSelectionScreen(loadingScreen, joinGameWithTeam, t.teams, t.playersOnTeam, t.hasPassword);
					int skipTeam = teamSelectionScreen.getSkipSelectionTeam();
					if (skipTeam != -1) {
						ProjectTetraTD.getInstance(ProjectTetraTD.class).setScreenAsynchronous(loadingScreen);
						joinGameWithTeam.accept(new ConnectionAttempt("", skipTeam));
					} else {
						ProjectTetraTD.getInstance(ProjectTetraTD.class).setScreenAsynchronous(teamSelectionScreen);
					}
				} else {
					ConnectionAttempt attempt = new ConnectionAttempt("", desiredTeam);
					joinGameWithTeam.accept(attempt);
				}
			}
		};
		
		Thread gameInfoThread = new Thread() {
			@Override
			public void run() {
				ProjectTetraTD.getInstance(ProjectTetraTD.class).setScreenAsynchronous(loadingScreen);
				getGameInfoFromFile(loadingScreen, loadingScreenSupplier, quest, details.getMaxPlayers(), callback);
			}
		};
		gameInfoThread.setDaemon(true);
		gameInfoThread.start();
	}

	public static void joinMultiplayerGame(String ip, int port) {
		final LogList loadingScreenSupplier = new LogList();
		LoadingScreen loadingScreen = new LoadingScreen(loadingScreenSupplier);
		Consumer<ConnectionAttempt> joinGameWithTeam = new Consumer<ConnectionAttempt>() {
			@Override
			public void accept(ConnectionAttempt attempt) {
				ProjectTetraTD game = ProjectTetraTD.getInstance(ProjectTetraTD.class);
				try {
					int team = attempt.team;

					Client client = new Client(30000, 30000);
					OnlineGameStream.registerPackets(client.getKryo());
					OnlineGameStream stream = new OnlineGameStream(new PacketEncryption(), client);

					Log.info("Joining " + ip + ":" + port + "...");
					loadingScreenSupplier.update("Joining "+ip+":"+port+"...");
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
					GameScreen screen = new GameScreen(stream, null, attempt.passwordAttempt, team);
					game.setScreenAsynchronous(screen);

					stream.sendPacket(new ConnectPacket(Settings.getPreferences().getString(Settings.USERNAME), team));
				} catch (Exception e) {
					e.printStackTrace();
					loadingScreenSupplier.update("An error occured while joining the game. "+e.getMessage());
					loadingScreen.showExitButton();
				}
			}
		};
		Consumer<GameInfoPacket> callback = new Consumer<GameInfoPacket>() {
			@Override
			public void accept(GameInfoPacket t) {
				TeamSelectionScreen teamSelectionScreen = new TeamSelectionScreen(loadingScreen, joinGameWithTeam, t.teams, t.playersOnTeam, t.hasPassword);
				int skipTeam = teamSelectionScreen.getSkipSelectionTeam();
				if (skipTeam != -1) {
                    ProjectTetraTD.getInstance(ProjectTetraTD.class).setScreenAsynchronous(loadingScreen);
					joinGameWithTeam.accept(new ConnectionAttempt("", skipTeam));
				} else {
					ProjectTetraTD.getInstance(ProjectTetraTD.class).setScreenAsynchronous(teamSelectionScreen);
				}
			}
		};

		Thread gameInfoThread = new Thread() {
			@Override
			public void run() {
				ProjectTetraTD.getInstance(ProjectTetraTD.class).setScreenAsynchronous(loadingScreen);
				getGameInfo(loadingScreen, loadingScreenSupplier, ip, port, callback);
			}
		};
		gameInfoThread.setDaemon(true);
		gameInfoThread.start();
	}

	public static void hostMultiplayerGame(QuestFile quest, GameManagerDetails details) {
		final LogList loadingScreenSupplier = new LogList();
		LoadingScreen loadingScreen = new LoadingScreen(loadingScreenSupplier);
		Consumer<ConnectionAttempt> joinGameWithTeam = new Consumer<ConnectionAttempt>() {
			@Override
			public void accept(ConnectionAttempt attempt) {
				int team = attempt.team;
				ProjectTetraTD rtd = ProjectTetraTD.getInstance(ProjectTetraTD.class);

				details.setHostedOnline(true);
				GameManager manager = new GameManager(details);
				LocalGameStream[] pair = LocalGameStream.createStreamPair();
				LocalGameStream clientsidestream = pair[0];

				loadingScreenSupplier.update("Starting server...");
				manager.initialize(quest);
				manager.start(loadingScreenSupplier);
				GameScreen screen = new GameScreen(clientsidestream, manager, details.getPassword(), team);
				rtd.setScreenAsynchronous(screen);

				clientsidestream.addListener(new PacketListener() {
					@Override
					public void packetReceived(Packet packet) {
						if (packet instanceof PlayerPositionsPacket) {
							return;
						}
						Log.debug("Client received packet: " + packet);
					}
				});
				pair[1].addListener(new PacketListener() {

					@Override
					public void packetReceived(Packet  packet) {
						Log.debug("Server received packet: " + packet);
					}
				});
				manager.onNewConnection(pair[1]);
				clientsidestream
						.sendPacket(new ConnectPacket(Settings.getPreferences().getString(Settings.USERNAME), team));
			}
		};

		Consumer<GameInfoPacket> callback = new Consumer<GameInfoPacket>() {
			@Override
			public void accept(GameInfoPacket t) {
				TeamSelectionScreen teamSelectionScreen = new TeamSelectionScreen(loadingScreen, joinGameWithTeam, t.teams, t.playersOnTeam, t.hasPassword);
				int skipTeam = teamSelectionScreen.getSkipSelectionTeam();
				if (skipTeam != -1) {
                    ProjectTetraTD.getInstance(ProjectTetraTD.class).setScreenAsynchronous(loadingScreen);
					joinGameWithTeam.accept(new ConnectionAttempt("", skipTeam));
				} else {
					ProjectTetraTD.getInstance(ProjectTetraTD.class).setScreenAsynchronous(teamSelectionScreen);
				}
			}
		};

		Thread gameInfoThread = new Thread() {
			@Override
			public void run() {
				ProjectTetraTD.getInstance(ProjectTetraTD.class).setScreenAsynchronous(loadingScreen);
				getGameInfoFromFile(loadingScreen, loadingScreenSupplier, quest, details.getMaxPlayers(), callback);
			}
		};
		gameInfoThread.setDaemon(true);
		gameInfoThread.start();
	}

	private static void getGameInfoFromFile(LoadingScreen loadingScreen, LogList loadingScreenSupplier, QuestFile file, int maxPlayers, Consumer<GameInfoPacket> callback) {
		ProjectTetraTD game = ProjectTetraTD.getInstance(ProjectTetraTD.class);
		try {
			loadingScreenSupplier.update("Loading world...");
			World world = file.loadWorld();
			if (world == null) {
				throw new NullPointerException("No world found at specified file: "+file.getFile().path());
			}

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
			int[] players = new int[availableTeams.size()];
			for (int i = 0; i < availableTeams.size(); i++) {
				teams[i] = availableTeams.get(i);
				players[i] = 0;
			}
			loadingScreenSupplier.update("World successfully loaded.");

			GameInfoPacket packet = new GameInfoPacket(teams, players, 0, maxPlayers, false);
			callback.accept(packet);
		} catch (Exception e) {
			e.printStackTrace();
			loadingScreenSupplier.update("An error occured while loading the world. "+e.getMessage());
			loadingScreen.showExitButton();
		}
	}

	private static void getGameInfo(LoadingScreen loadingScreen, LogList loadingScreenSupplier, String ip, int port, Consumer<GameInfoPacket> callback) {
		ProjectTetraTD game = ProjectTetraTD.getInstance(ProjectTetraTD.class);
		try {
			Client client = new Client();
			OnlineGameStream.registerPackets(client.getKryo());
			OnlineGameStream stream = new OnlineGameStream(new PacketEncryption(), client);

			loadingScreenSupplier.update("Fetching game data from "+ip+":"+port+"...");
			client.start();
			try {
				client.connect(5000, ip, port);
			} catch (Exception e) {
				Log.error("Failed to fetch game data from server.", e);
				throw e;
			}
			stream.sendPacket(new GameInfoPacket());
			stream.addListener(new PacketListener() {
				@Override
				public void packetReceived(Packet packet) {
					if (packet instanceof GameInfoPacket) {
						GameInfoPacket info = (GameInfoPacket) packet;
						Log.debug("Received game info: " + info.teams + " teams, " + info.players + " players, "
								+ info.maxPlayers + " max players.");
						loadingScreenSupplier.update("Successfully retrieved game info.");
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
			loadingScreenSupplier.update("An error occured while fetching game info. "+e.getMessage());
			loadingScreen.showExitButton();
		}
	}

	public static void startTutorial() {
		WorldFolder world = new WorldFolder("Tutorial");
		LevelFolder level = world.readLevels()[0];
		QuestFile quest = level.readQuests()[0];
		quest.createSave();
		int team = 0;

		GameManagerDetails details = new GameManagerDetails();
		details.setMaxPlayers(1);
		details.setHostedOnline(false);

		GameManager manager = new GameManager(details);
		LocalGameStream[] streams = LocalGameStream.createStreamPair();
		GameStream clientsideStream = streams[0];
		GameStream serversideStream = streams[1];

		GameScreen screen = new TutorialScreen(clientsideStream, manager);

		manager.initialize(quest);
		ProjectTetraTD.getInstance().setScreen(screen);
		manager.start();
		manager.onNewConnection(serversideStream);

		clientsideStream.sendPacket(new ConnectPacket(Settings.getPreferences().getString(Settings.USERNAME), team));
	}

}
