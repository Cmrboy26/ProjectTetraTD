package net.cmr.rtd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URL;
import java.util.function.Consumer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.kryonet.Client;

import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import net.cmr.rtd.game.EasterEgg;
import net.cmr.rtd.game.Feedback;
import net.cmr.rtd.game.Hotkeys;
import net.cmr.rtd.game.Feedback.FeedbackForm;
import net.cmr.rtd.game.achievements.Achievement;
import net.cmr.rtd.game.achievements.AchievementManager;
import net.cmr.rtd.game.achievements.custom.TutorialCompleteAchievement;
import net.cmr.rtd.game.files.QuestFile;
import net.cmr.rtd.game.packets.ConnectPacket;
import net.cmr.rtd.game.packets.GameInfoPacket;
import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.packets.PacketEncryption;
import net.cmr.rtd.game.stream.GameStream.PacketListener;
import net.cmr.rtd.game.stream.OnlineGameStream;
import net.cmr.rtd.screen.FirstTimePlayingScreen;
import net.cmr.rtd.screen.GameScreen;
import net.cmr.rtd.screen.KeybindScreen;
import net.cmr.rtd.screen.MainMenuScreen;
import net.cmr.rtd.shader.ShaderManager;
import net.cmr.rtd.shader.ShaderManager.CustomShader;
import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Audio;
import net.cmr.util.Audio.GameMusic;
import net.cmr.util.Audio.GameSFX;
import net.cmr.util.CMRGame;
import net.cmr.util.IntroScreen;
import net.cmr.util.Log;
import net.cmr.util.Settings;
import net.cmr.util.Sprites;

public class ProjectTetraTD extends CMRGame {

	public static final String GAME_NAME = "Project Tetra TD";
	public static final String EXTERNAL_FILE_NAME = "pttd/";
	
	public static final int MAJORVERSION = 1;
	public static final int MINORVERSION = 1;
	public static final int PATCHVERSION = 0;
	public static final boolean TEST_VERSION = true;

	public ProjectTetraTD(NativeFileChooser fileChooser) {
		super(fileChooser);
	}

	Viewport screenViewport;
	Viewport viewport;
	Stage achievementNotificationStage;
	public ShaderManager shaderManager;
	Table actorTable;

	@Override
	public void create () {
		super.create();

		screenViewport = new ScreenViewport();
		viewport = new ExtendViewport(640, 360);
		achievementNotificationStage = new Stage(viewport);
		actorTable = new Table();
		actorTable.align(Align.top);
		actorTable.setFillParent(true);
		achievementNotificationStage.addActor(actorTable);

		Settings.applySettings();

		Hotkeys.load();
		Hotkeys.save();
		
		AbstractScreenEX nextScreen = null;
		if (getUsername().equals("null")) {
			nextScreen = new FirstTimePlayingScreen();
		} else {
			nextScreen = new MainMenuScreen();
		}
		showIntroScreen(nextScreen);

		if (isMobile()) {
			// TODO: Find out why tooltips dont appear on mobile
			TooltipManager.getInstance().instant();
		}

		AchievementManager.getInstance();
		AchievementManager.save();

		shaderManager = new ShaderManager();

		FileHandle gameDataFolder = Gdx.files.external(ProjectTetraTD.EXTERNAL_FILE_NAME);
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

	Object screenLock = new Object();
	Screen newScreen;

	public synchronized void setScreenAsynchronous(Screen screen) {
		synchronized (screenLock) {
			newScreen = screen;
		}
	}

	@Override
	public void render () {
		if (Gdx.input.isKeyJustPressed(Input.Keys.F12)) {
			CMRGame.setDebug(!CMRGame.isDebug());
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
			try {
				System.out.println("Retrieving feedback form... REMOVE THIS CODE BEFORE RELEASE!!!");
				FeedbackForm form = Feedback.retrieveFeedbackForm();
				form.submit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.Q) && EasterEgg.isFelipe()) {
			onAchievementComplete(Achievement.createAchievementInstance(TutorialCompleteAchievement.class));
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.F8)) {
			shaderManager.reloadShaders();
			Log.info("Reloaded shaders.");
		}

		synchronized (screenLock) {
			if (newScreen != null) {
				setScreen(newScreen);
				newScreen = null;
			}
		}

		ScreenUtils.clear(0, 0, 0, 1);
		shaderManager.update();
		super.render();

		viewport.apply(true);
		batch().setProjectionMatrix(viewport.getCamera().combined);
		achievementNotificationStage.act();
		batch().begin();
		batch().setColor(Color.WHITE);
		achievementNotificationStage.draw();
		batch().end();

		if (Settings.getPreferences().getBoolean(Settings.SHOW_FPS)) {
			screenViewport.apply(true);
			batch().setProjectionMatrix(screenViewport.getCamera().combined);
			batch().begin();
			batch().setColor(Color.WHITE);
			BitmapFont font = Sprites.skin().getFont("small-font");
			font.setColor(Color.WHITE);
			font.draw(batch(), "FPS: " + Gdx.graphics.getFramesPerSecond(), 5, Gdx.graphics.getHeight() - 5);
			batch().end();
		}
	}

	public final int achievementHeight = 100;

	public void onAchievementComplete(Achievement<?> achievement) {
		Audio.getInstance().playSFX(GameSFX.ACHIEVEMENT_GET, 1f, 1f);

		Table achievementTable = new Table();
		achievementTable.setWidth(200);
		achievementTable.setHeight(achievementHeight);
		actorTable.clear();
		actorTable.add(achievementTable).row();
		
		Image achievementIcon = new Image(Sprites.sprite(achievement.getDisplayIcon()));
		achievementTable.add(achievementIcon).size(50, 50).expand().center().fill();

		VerticalGroup textGroup = new VerticalGroup();
		achievementTable.add(textGroup).expand().center().fill().pad(10);

		Label title = new Label("Achievement Complete!", Sprites.skin(), "small");
		title.setColor(Color.WHITE);
		title.setAlignment(Align.center);
		textGroup.addActor(title);

		Label subtitle = new Label("- " + achievement.getReadableName() + " -", Sprites.skin(), "small");
		subtitle.setColor(Color.WHITE);
		subtitle.setAlignment(Align.center);
		subtitle.setFontScale(.45f);
		textGroup.addActor(subtitle);
		
		Action actions = Actions.sequence(
			Actions.moveBy(0, achievementHeight, 0),
			Actions.moveBy(0, -achievementHeight, 0.25f, Interpolation.sineOut),
			Actions.delay(5),
			Actions.moveBy(0, achievementHeight, 0.5f, Interpolation.sineIn),
			Actions.removeActor()
		);
		achievementTable.addAction(actions);
	}

	@Override
	public void resize(int width, int height) {
		screenViewport.update(width, height, true);
		viewport.update(width, height, true);
		shaderManager.resize(width, height);
		super.resize(width, height);
	}
	
	@Override
	public void pause() {
		super.pause();
		AchievementManager.save();
	}

	@Override
	public void resume() {
		super.resume();
		if (isMobile()) {
			IntroScreen introScreen = new IntroScreen(new MainMenuScreen());
			setScreen(introScreen);
		}
	}

	@Override
	public void dispose () {
		super.dispose();
		shaderManager.dispose();
	}

	public void enableShader(Batch batch, CustomShader shader, float... inputs) {
		shaderManager.enableShader(batch, shader, inputs);
	}
	public void disableShader(Batch batch) {
		shaderManager.disableShader(batch);
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

	/**
	 * Retrieves the external IP address of the client
	 * @return
	 */
	public static String getExternalIP() {
		try {
			URL whatismyip = URI.create("http://checkip.amazonaws.com").toURL();
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
		FileHandle dataFile = Gdx.files.external(ProjectTetraTD.EXTERNAL_FILE_NAME+"userdata.json");
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
		FileHandle dataFile = Gdx.files.external(ProjectTetraTD.EXTERNAL_FILE_NAME+"userdata.json");
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
		writeUserData("lastPlayed", serializedQuest);
	}

	public String[] getLastPlayedQuest() {
		return readUserData("lastPlayed", String[].class);
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
		writeUserData("lastPlayed", null);
	}

	public int getLastPlayedTeam() {
		return readUserData("lastPlayedTeam", Long.class, -1L).intValue();
	}

	public void setLastPlayedTeam(int team) {
		writeUserData("lastPlayedTeam", team);
	}

	public static @Null String getLastPlayedWorld() {
		return readUserData("lastPlayedWorld", String.class);
	}

	public static void setLastPlayedWorld(String world) {
		writeUserData("lastPlayedWorld", world);
	}

	public static @Null Long getLastFormID() {
		return readUserData("lastFormID", Long.class, -1L);
	}

	public static void setLastFormID(Long formID) {
		writeUserData("lastFormID", formID);
	}

	public static void writeUserData(String key, Object value) {
		Object writeValue = value;
		if (value != null && value.getClass().isArray()) {
			JSONArray array = new JSONArray();
			for (int i = 0; i < Array.getLength(value); i++) {
				array.add(Array.get(value, i));
			}
			writeValue = array;
		}
		FileHandle dataFile = Gdx.files.external(ProjectTetraTD.EXTERNAL_FILE_NAME+"userdata.json");
		if (!dataFile.exists()) {
			dataFile.writeString("{}", false);
		}
		JSONParser parser = new JSONParser();
		try {
			JSONObject root = (JSONObject) parser.parse(dataFile.readString());
			if (writeValue == null) {
				root.remove(key);
				dataFile.writeString(root.toJSONString(), false);
				return;
			}
			root.put(key, writeValue);
			dataFile.writeString(root.toJSONString(), false);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public static <T> T readUserData(String key, Class<T> clazz, T defaultValue) {
		FileHandle dataFile = Gdx.files.external(ProjectTetraTD.EXTERNAL_FILE_NAME+"userdata.json");
		if (!dataFile.exists()) {
			dataFile.writeString("{}", false);
		}
		String data = dataFile.readString();
		JSONParser parser = new JSONParser();
		try {
			JSONObject root = (JSONObject) parser.parse(data);
			Object value = root.get(key);
			if (value == null) {
				return null;
			}
			if (value instanceof JSONArray) {
				JSONArray array = (JSONArray) value;
				// If T is an array, convert the JSONArray to T
				if (clazz.isArray()) {
					Object arrayValue = Array.newInstance(clazz.getComponentType(), array.size());
					for (int i = 0; i < array.size(); i++) {
						Array.set(arrayValue, i, array.get(i));
					}
					return clazz.cast(arrayValue);
				}
			}
			return clazz.cast(value);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Stored value is not of type " + clazz.getSimpleName());
		}
		return defaultValue;
	}

	public static <T> T readUserData(String key, Class<T> clazz) {
		return readUserData(key, clazz, null);
	}

	@Override
	public void setScreen(Screen screen) {
		Screen previousScreen = getScreen();
		super.setScreen(screen);
		GameMusic previousMusic = null;
		GameMusic newMusic = null;
		if (previousScreen instanceof AbstractScreenEX) {
			previousMusic = ((AbstractScreenEX) previousScreen).getScreenMusic();
		}
		if (screen instanceof AbstractScreenEX) {
			newMusic = ((AbstractScreenEX) screen).getScreenMusic();
		}

		if (newMusic == null) {
			Audio.getInstance().stopMusic();
		} else if (previousMusic != newMusic) {
			Audio.getInstance().playMusic(newMusic);
		}
	}


}