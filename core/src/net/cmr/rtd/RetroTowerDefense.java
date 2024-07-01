package net.cmr.rtd;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.function.Consumer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.esotericsoftware.kryonet.Client;

import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.GameManager.GameManagerDetails;
import net.cmr.rtd.game.GameSave;
import net.cmr.rtd.game.LevelSave;
import net.cmr.rtd.game.files.QuestFile;
import net.cmr.rtd.game.packets.ConnectPacket;
import net.cmr.rtd.game.packets.GameInfoPacket;
import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.packets.PacketEncryption;
import net.cmr.rtd.game.stream.GameStream;
import net.cmr.rtd.game.stream.GameStream.PacketListener;
import net.cmr.rtd.game.stream.LocalGameStream;
import net.cmr.rtd.game.stream.OnlineGameStream;
import net.cmr.rtd.screen.GameScreen;
import net.cmr.rtd.screen.HostScreen;
import net.cmr.rtd.screen.MainMenuScreen;
import net.cmr.util.CMRGame;
import net.cmr.util.Log;
import net.cmr.util.Settings;
import net.cmr.util.Sprites;

public class RetroTowerDefense extends CMRGame {
	
	public static final int MAJORVERSION = 1;
	public static final int MINORVERSION = 0;
	public static final int PATCHVERSION = 4;

	public RetroTowerDefense(NativeFileChooser fileChooser) {
		super(fileChooser);
	}

	ScreenViewport viewport = new ScreenViewport();

	@Override
	public void create () {
		super.create();
		Settings.applySettings();
		showIntroScreen(new MainMenuScreen());

		if (isMobile()) {
			// TODO: Find out why tooltips dont appear on mobile
			TooltipManager.getInstance().instant();
		}

		FileHandle gameDataFolder = Gdx.files.external("retrotowerdefense/");
		gameDataFolder.mkdirs();

		FileHandle savesFolder = gameDataFolder.child("saves/");
		savesFolder.mkdirs();

		FileHandle worldsFolder = gameDataFolder.child("worlds/");
		worldsFolder.mkdirs();

		Log.info("Copying story levels to external folder...");
		// Create story level folders from the assets
		FileHandle defaultWorldsHandle = Gdx.files.internal("defaultWorlds/");
		Log.info("Story levels handle: " + defaultWorldsHandle.file().getAbsolutePath());
		boolean defaultWorldsHandleExists = defaultWorldsHandle.isDirectory();
		Log.info("Story levels handle exists: " + defaultWorldsHandleExists);
		if (!defaultWorldsHandleExists) {
			Log.info("Handle not found, attempting to copy assets from \""+defaultWorldsHandle.path()+"\" to \""+worldsFolder.path()+"\"...");
			defaultWorldsHandle = Gdx.files.internal("assets/defaultWorlds/");
		}

		Log.info("Story level files: " + defaultWorldsHandle.list().length);
		for (FileHandle level : defaultWorldsHandle.list()) {
			// level is the folder containing the level data (waves folder, world.dat) in the assets folder
			FileHandle internal = defaultWorldsHandle.child(level.name());
			FileHandle external = worldsFolder;
			FileHandle externalLevelFolder = external.child(level.name());
			Log.info("Copying level \""+level.name()+"\" to external folder...");
			if (externalLevelFolder.isDirectory() && externalLevelFolder.exists()) {
				// Delete the directory so outdated story mode levels and waves are removed
				Log.info("Deleting outdated directory \""+externalLevelFolder.path()+"\"...");
				externalLevelFolder.deleteDirectory();
			}
			internal.copyTo(external);
			Log.info("Copied level \""+level.name()+"\" to external folder.");
		}
		FileHandle editorFolder = gameDataFolder.child("editor/");
		editorFolder.mkdirs();
	}

	@Override
	public void render () {
		if (Gdx.input.isKeyJustPressed(Input.Keys.F12) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT_BRACKET)) {
			CMRGame.setDebug(!CMRGame.isDebug());
		}

		ScreenUtils.clear(0, 0, 0, 1);
		super.render();

		if (Settings.getPreferences().getBoolean(Settings.SHOW_FPS)) {
			viewport.apply(true);
			batch().setProjectionMatrix(viewport.getCamera().combined);
			batch().begin();
			batch().setColor(Color.WHITE);
			Sprites.skin().getFont("small-font").draw(batch(), "FPS: " + Gdx.graphics.getFramesPerSecond(), 5, Gdx.graphics.getHeight()-5);
			batch().end();
		}
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
		super.resize(width, height);
	}
	
	@Override
	public void dispose () {
		super.dispose();
	}

	public String getUsername() {
		return Settings.getPreferences().getString(Settings.USERNAME);
	}

    public static boolean isMobile() {
        return Gdx.app.getType() == ApplicationType.Android || CMRGame.SIMULATE_MOBILE;
    }

	/**
	 * Joins an online game with the given IP and port
	 */
	public void joinOnlineGame(String ip, int port, int team) throws Exception {
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
		GameScreen screen = new GameScreen(stream, null, null, team);
		setScreen(screen);

		stream.sendPacket(new ConnectPacket(Settings.getPreferences().getString(Settings.USERNAME), team));
	}

	public void getOnlineGameData(String ip, int port, Consumer<GameInfoPacket> callback) throws Exception {
		Client client = new Client();
		OnlineGameStream.registerPackets(client.getKryo());
		OnlineGameStream stream = new OnlineGameStream(new PacketEncryption(), client);

		Log.info("Connecting to w" + ip + ":" + port + "...");
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
	}

	@Deprecated
	public void hostOnlineGame(GameManagerDetails details, LevelSave levelSave, String saveName, String waveName, boolean override, int teams) {
		hostOnlineGame(details, levelSave.createSave(saveName, waveName, override), levelSave, teams);
	}

	@Deprecated
	public void hostOnlineGame(GameManagerDetails details, GameSave save, LevelSave lsave, int teams) {
		HostScreen screen = new HostScreen(details, save, lsave, teams);
		setScreen(screen);
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
	public void joinSingleplayerGame(GameManagerDetails details, LevelSave levelSave, String saveName, String waveName, int team) {
		GameSave save = levelSave.createSave(saveName, waveName, false);
		joinSingleplayerGame(details, save, levelSave, team);
	}

	/**
	 * @see #joinSingleplayerGame(GameManagerDetails, LevelSave, String, String)
	 * @param override Whether to override an existing save folder with the same name
	 */
	public void joinSingleplayerGame(GameManagerDetails details, LevelSave levelSave, String saveName, String waveName, boolean override, int team) {
		GameSave save = levelSave.createSave(saveName, waveName, override);
		joinSingleplayerGame(details, save, levelSave, team);
	}

	/**
	 * Starts and joins a local/singleplayer game on the client's machine
	 * Should be called when LOADING a game.
	 */
	@Deprecated
	public void joinSingleplayerGame(GameManagerDetails details, GameSave save, LevelSave lsave, int team) {
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

		GameScreen screen = new GameScreen(clientsideStream, manager, null, team);

		//manager.initialize(save); // TODO: FIX
		setScreen(screen);
		manager.start();
		manager.onNewConnection(serversideStream);
		
		clientsideStream.sendPacket(new ConnectPacket(Settings.getPreferences().getString(Settings.USERNAME), team));
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

	public enum LevelValueKey {
		HIGHSCORE, // Stored as a long
		FARTHEST_WAVE, // Stored as a long
		COMPLETED_TASKS, // stored as a long[], ID of each task
		;

		public String getKey() {
			return name().toLowerCase();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getStoredLevelValue(QuestFile quest, LevelValueKey key, Class<T> type) {
		FileHandle dataFile = Gdx.files.external("retrotowerdefense/userdata.json");
		if (!dataFile.exists()) {
			dataFile.writeString("{}", false);
			return null;
		}
		String data = dataFile.readString();
		JSONParser parser = new JSONParser();
		try {
			JSONObject root = (JSONObject) parser.parse(data);
			JSONObject levelData = (JSONObject) root.get(quest.getSaveFolderName());
			if (levelData == null) {
				return null;
			}
			Object value = levelData.get(key.getKey());
			if (value == null) {
				return null;
			}
			if (value instanceof JSONArray) {
				JSONArray array = (JSONArray) value;
				// If T is an array, convert the JSONArray to T
				if (type.isArray()) {
					Object arrayValue = Array.newInstance(type.getComponentType(), array.size());
					for (int i = 0; i < array.size(); i++) {
						Array.set(arrayValue, i, array.get(i));
					}
					return (T) arrayValue;
				}
			}
			return type.cast(value);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Stored value is not of type " + type.getSimpleName());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static void setStoredLevelValue(QuestFile quest, LevelValueKey key, Object value) {
		FileHandle dataFile = Gdx.files.external("retrotowerdefense/userdata.json");
		if (!dataFile.exists()) {
			dataFile.writeString("{}", false);
		}
		String data = dataFile.readString();
		JSONParser parser = new JSONParser();
		try {
			JSONObject root = (JSONObject) parser.parse(data);
			JSONObject levelData = (JSONObject) root.get(quest.getSaveFolderName());
			if (levelData == null) {
				levelData = new JSONObject();
				root.put(quest.getSaveFolderName(), levelData);
			}
			if (value != null && value.getClass().isArray()) {
				JSONArray array = new JSONArray();
				for (int i = 0; i < Array.getLength(value); i++) {
					array.add(Array.get(value, i));
				}
				levelData.put(key.getKey(), array);
			} else {
				levelData.put(key.getKey(), value);
			}
			dataFile.writeString(root.toJSONString(), false);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public void setLastPlayedQuest(String[] serializedQuest) {
		FileHandle dataFile = Gdx.files.external("retrotowerdefense/userdata.json");
		if (!dataFile.exists()) {
			dataFile.writeString("{}", false);
		}
		String data = dataFile.readString();
		JSONParser parser = new JSONParser();
		try {
			JSONObject root = (JSONObject) parser.parse(data);
			JSONArray lastPlayed = new JSONArray();
			for (String s : serializedQuest) {
				lastPlayed.add(s);
			}
			root.put("lastPlayed", lastPlayed);
			dataFile.writeString(root.toJSONString(), false);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public String[] getLastPlayedQuest() {
		FileHandle dataFile = Gdx.files.external("retrotowerdefense/userdata.json");
		if (!dataFile.exists()) {
			dataFile.writeString("{}", false);
		}
		String data = dataFile.readString();
		JSONParser parser = new JSONParser();
		try {
			JSONObject root = (JSONObject) parser.parse(data);
			JSONArray lastPlayed = (JSONArray) root.get("lastPlayed");
			if (lastPlayed == null) {
				return null;
			}
			String[] quest = new String[lastPlayed.size()];
			for (int i = 0; i < lastPlayed.size(); i++) {
				quest[i] = (String) lastPlayed.get(i);
			}
			return quest;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean hasLastPlayedQuest() {
		try {
			String[] lastPlayedQuest = getLastPlayedQuest();
			if (lastPlayedQuest == null) {
				return false;
			}
			QuestFile quest = QuestFile.deserialize(lastPlayedQuest);
			return quest != null && quest.getSaveFolder().child("wave.json").exists();
		} catch (Exception e) {
			return false;
		}
	}

	public void clearLastPlayedQuest() {
		FileHandle dataFile = Gdx.files.external("retrotowerdefense/userdata.json");
		if (!dataFile.exists()) {
			dataFile.writeString("{}", false);
		}
		String data = dataFile.readString();
		JSONParser parser = new JSONParser();
		try {
			JSONObject root = (JSONObject) parser.parse(data);
			root.remove("lastPlayed");
			dataFile.writeString(root.toJSONString(), false);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public int getLastPlayedTeam() {
		FileHandle dataFile = Gdx.files.external("retrotowerdefense/userdata.json");
		if (!dataFile.exists()) {
			dataFile.writeString("{}", false);
		}
		String data = dataFile.readString();
		JSONParser parser = new JSONParser();
		try {
			JSONObject root = (JSONObject) parser.parse(data);
			Long team = (Long) root.get("lastPlayedTeam");
			if (team == null) {
				return -1;
			}
			return team.intValue();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void setLastPlayedTeam(int team) {
		FileHandle dataFile = Gdx.files.external("retrotowerdefense/userdata.json");
		if (!dataFile.exists()) {
			dataFile.writeString("{}", false);
		}
		String data = dataFile.readString();
		JSONParser parser = new JSONParser();
		try {
			JSONObject root = (JSONObject) parser.parse(data);
			root.put("lastPlayedTeam", team);
			dataFile.writeString(root.toJSONString(), false);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public static @Null String getLastPlayedWorld() {
		FileHandle dataFile = Gdx.files.external("retrotowerdefense/userdata.json");
		if (!dataFile.exists()) {
			dataFile.writeString("{}", false);
		}
		String data = dataFile.readString();
		JSONParser parser = new JSONParser();
		try {
			JSONObject root = (JSONObject) parser.parse(data);
			String world = (String) root.get("lastPlayedWorld");
			if (world == null) {
				return null;
			}
			return world;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void setLastPlayedWorld(String world) {
		FileHandle dataFile = Gdx.files.external("retrotowerdefense/userdata.json");
		if (!dataFile.exists()) {
			dataFile.writeString("{}", false);
		}
		String data = dataFile.readString();
		JSONParser parser = new JSONParser();
		try {
			JSONObject root = (JSONObject) parser.parse(data);
			root.put("lastPlayedWorld", world);
			dataFile.writeString(root.toJSONString(), false);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}


}