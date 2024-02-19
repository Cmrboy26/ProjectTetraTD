package net.cmr.rtd;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.esotericsoftware.kryonet.Client;

import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.GameManager.GameManagerDetails;
import net.cmr.rtd.game.GameSave;
import net.cmr.rtd.game.LevelSave;
import net.cmr.rtd.game.packets.ConnectPacket;
import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.packets.PacketEncryption;
import net.cmr.rtd.game.stream.GameStream;
import net.cmr.rtd.game.stream.GameStream.PacketListener;
import net.cmr.rtd.game.stream.LocalGameStream;
import net.cmr.rtd.game.stream.OnlineGameStream;
import net.cmr.rtd.screen.GameScreen;
import net.cmr.rtd.screen.MainMenuScreen;
import net.cmr.util.CMRGame;
import net.cmr.util.Log;
import net.cmr.util.Settings;

public class RetroTowerDefense extends CMRGame {
	
	public RetroTowerDefense(NativeFileChooser fileChooser) {
		super(fileChooser);
	}

	@Override
	public void create () {
		super.create();
		Settings.applySettings();
		showIntroScreen(new MainMenuScreen());

		FileHandle gameDataFolder = Gdx.files.external("retrotowerdefense/");
		gameDataFolder.mkdirs();

		FileHandle savesFolder = gameDataFolder.child("saves/");
		savesFolder.mkdirs();

		FileHandle levelsFolder = gameDataFolder.child("levels/");
		levelsFolder.mkdirs();

		FileHandle editorFolder = gameDataFolder.child("editor/");
		editorFolder.mkdirs();
	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);
		super.render();
	}
	
	@Override
	public void dispose () {
		super.dispose();
	}

	public String getUsername() {
		return Settings.getPreferences().getString(Settings.USERNAME);
	}

	/**
	 * Joins an online game with the given IP and port
	 */
	public void joinOnlineGame(String ip, int port) {
		Client client = new Client();
		OnlineGameStream.registerPackets(client.getKryo());
		OnlineGameStream stream = new OnlineGameStream(new PacketEncryption(), client);

		Log.info("Connecting to " + ip + ":" + port + "...");
		client.start();
		try {
			client.connect(5000, ip, port);
		} catch (Exception e) {
			Log.error("Failed to connect to server.", e);
			return;
		}
		stream.addListener(new PacketListener() {
			@Override
			public void packetReceived(Packet packet) {
				Log.debug("Client received packet: " + packet);
			}
		});
		Log.info("Connected.");
		GameScreen screen = new GameScreen(stream, null, null);
		setScreen(screen);

		stream.sendPacket(new ConnectPacket(Settings.getPreferences().getString(Settings.USERNAME), 0));
	}

	/**
	 * Starts and joins a local/singleplayer game on the client's machine
	 * Creates a new save folder for the selected level and wave (difficulty).
	 * This method should be called when creating a new game (NOT loading).
	 * @param details The details of the game manager
	 * @param levelSave The save to use for the game
	 * @param saveName The name of the save folder
	 * @param waveName The name of the wave to use for the level
	 */
	public void joinSingleplayerGame(GameManagerDetails details, LevelSave levelSave, String saveName, String waveName) {
		joinSingleplayerGame(details, levelSave.createSave(saveName, waveName, false));
	}

	/**
	 * Starts and joins a local/singleplayer game on the client's machine
	 * Should be called when LOADING a game.
	 */
	public void joinSingleplayerGame(GameManagerDetails details, GameSave save) {
		GameManager manager = save.loadGame(details);
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

		GameScreen screen = new GameScreen(clientsideStream, manager, null);
		manager.initialize(new GameSave("default")); 
		setScreen(screen);
		manager.start();
		manager.onNewConnection(serversideStream);
		
		clientsideStream.sendPacket(new ConnectPacket(Settings.getPreferences().getString(Settings.USERNAME), 0));
	}

	/**
	 * Retrieves the external IP address of the client
	 * @return
	 */
	public static String getExternalIP() {
		try {
			URL whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			String ip = in.readLine();
			in.close();
			return ip;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Unknown";
	}
}
