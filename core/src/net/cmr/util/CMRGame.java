package net.cmr.util;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import net.cmr.util.Log.LogLevel;

public abstract class CMRGame extends Game {

    private static CMRGame instance = null;
    private static Object instanceLock = new Object();
    private static boolean DEBUG = false;
    public static boolean SKIP_INTRO = true && DEBUG;
    private SpriteBatch batch;
    private NativeFileChooser fileChooser;

    public CMRGame(NativeFileChooser fileChooser) {
        super();
        this.fileChooser = fileChooser;
    }

    public void initialize() {
        setInstance(this);
        this.batch = new SpriteBatch();
        initializeSystems(fileChooser);
    }

    public static void initializeSystems(NativeFileChooser fileChooser) {
        Log.initializeLog();
        Log.setLogLevel(DEBUG ? LogLevel.DEBUG : LogLevel.INFO);
        Audio.initializeAudio();
        Sprites.initializeSpriteManager();
        Files.initializeFiles(fileChooser);
        Settings.initializeSettings();
    }

    @Override
    public void create() {
        initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        Audio.disposeAudio();
        Sprites.disposeManager();
    }

    public void showIntroScreen(AbstractScreenEX nextScreen) {
        setScreen(new IntroScreen(nextScreen));
    }

    public SpriteBatch batch() {
        return batch;
    };

    public static boolean isDebug() {
        return DEBUG;
    }
    
    public static boolean instanceExists() {
        return instance != null;
    }

    public static CMRGame getInstance() {
        synchronized (instanceLock) {
            if (instance == null) {
                throw new IllegalStateException("CMRGame instance has not been initialized.");
            }
            return instance;
        }
    }
    public static <T extends CMRGame> T getInstance(Class<T> type) {
        synchronized (instanceLock) {
            if (instance == null) {
                throw new IllegalStateException("Instance has not been initialized.");
            }
            return type.cast(instance);
        }
    }

    private static void setInstance(CMRGame instance) {
        synchronized (instanceLock) {
            if (CMRGame.instance != null) {
                throw new IllegalStateException("CMRGame instance has already been initialized.");
            }
            CMRGame.instance = instance;
        }
    }
    
}
