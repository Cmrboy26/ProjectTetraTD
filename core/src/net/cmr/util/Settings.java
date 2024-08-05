package net.cmr.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Graphics.DisplayMode;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.stream.OnlineGameStream;

@SuppressWarnings("unused")
public class Settings {
    
    public static final String PREFERENCES_LOCATION = "RTD-Settings.xml";
    public static final String MASTER_VOLUME = "masterVolume";
    public static final String MUSIC_VOLUME = "musicVolume";
    public static final String SFX_VOLUME = "sfxVolume";
    public static final String USERNAME = "username";
    public static final String SHOW_FPS = "showFPS";
    public static final String FPS = "fps";
    public static final String SHOW_PLACEMENT_GRID = "showPlacementGrid";
    public static final String FULLSCREEN = "fullscreen";
    public static final String GAMMA = "gamma";
    // Host settings
    public static final String USE_UPNP = "useUPnP";
    public static final String PORT = "port";
    public static final String MAX_PLAYERS = "maxPlayers";
    // Join settings
    public static final String JOIN_IP = "joinIP";
    public static final String JOIN_PORT = "joinPort";
    private static Preferences preferences;

    public static void initializeSettings() {
        // Read settings from preferences
        preferences = getPreferences();
    }

    public static void resetSettings() {
        setDefaults(true);
    }

    public static void setDefaults(boolean force) {
        Preferences preferences = getPreferences();
        
        defaultFloat(MASTER_VOLUME, 0.5f, force, preferences);
        defaultFloat(MUSIC_VOLUME, 0.5f, force, preferences);
        defaultFloat(SFX_VOLUME, 0.5f, force, preferences);
        defaultString(USERNAME, "guest", force, preferences);
        defaultBoolean(SHOW_FPS, false, force, preferences);
        defaultBoolean(SHOW_PLACEMENT_GRID, false, force, preferences);
        defaultInt(FPS, -1, force, preferences);
        defaultBoolean(FULLSCREEN, false, force, preferences);
        defaultFloat(GAMMA, 1.0f, force, preferences);

        defaultBoolean(USE_UPNP, true, force, preferences);
        defaultInt(PORT, 11265, force, preferences);
        defaultInt(MAX_PLAYERS, 4, force, preferences);

        defaultString(JOIN_IP, "", force, preferences);
        defaultInt(JOIN_PORT, 11265, force, preferences);

        preferences.flush();
    }

    private static void defaultBoolean(String key, boolean value, boolean force, Preferences preferences) {
        if(force || !preferences.contains(key)) {
            preferences.putBoolean(key, value);
        }
    }
    private static void defaultFloat(String key, float value, boolean force, Preferences preferences) {
        if(force || !preferences.contains(key)) {
            preferences.putFloat(key, value);
        }
    }
    private static void defaultString(String key, String value, boolean force, Preferences preferences) {
        if(force || !preferences.contains(key)) {
            preferences.putString(key, value);
        }
    }
    private static void defaultDouble(String key, double value, boolean force, Preferences preferences) {
        if(force || !preferences.contains(key)) {
            preferences.putFloat(key, (float)value);
        }
    }
    private static void defaultInt(String key, int value, boolean force, Preferences preferences) {
        if(force || !preferences.contains(key)) {
            preferences.putInteger(key, value);
        }
    }
    private static void defaultLong(String key, long value, boolean force, Preferences preferences) {
        if(force || !preferences.contains(key)) {
            preferences.putLong(key, value);
        }
    }

    static int lastSettings = -1;

    public static void applySettings() {
        // Set the default values if they don't exist
        setDefaults(false);
        Preferences preferences = getPreferences();
        // Apply the settings to the game
        Audio.getInstance().setMusicVolume(preferences.getFloat(MUSIC_VOLUME) * preferences.getFloat(MASTER_VOLUME));
        Audio.getInstance().setSFXVolume(preferences.getFloat(SFX_VOLUME) * preferences.getFloat(MASTER_VOLUME));

        int fps = preferences.getInteger(FPS);
        if (fps <= -1) {
            Gdx.graphics.setVSync(true);
        } else {
            Gdx.graphics.setVSync(false);
        }
        Gdx.graphics.setForegroundFPS(fps);

        int newSettings = preferences.getBoolean(FULLSCREEN) ? 1 : 0;
        boolean changeScreen = lastSettings != newSettings;
        lastSettings = newSettings;

        if (lastSettings == 1 && changeScreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else if (lastSettings == 0 && changeScreen) {
            Gdx.graphics.setWindowedMode(640, 480);
        }

        Log.info("Applied settings.");
    }

    public static Preferences getPreferences() {
        if (preferences != null) return preferences;
        return Gdx.app.getPreferences(PREFERENCES_LOCATION);
    }

}
