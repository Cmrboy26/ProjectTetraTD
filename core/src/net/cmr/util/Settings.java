package net.cmr.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

@SuppressWarnings("unused")
public class Settings {
    
    public static final String PREFERENCES_LOCATION = "RTD-Settings.xml";
    public static final String MUSIC_VOLUME = "musicVolume";
    public static final String SFX_VOLUME = "sfxVolume";
    public static final String USERNAME = "username";
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
        
        defaultFloat(MUSIC_VOLUME, 0.5f, force, preferences);
        defaultFloat(SFX_VOLUME, 0.5f, force, preferences);
        defaultString(USERNAME, System.getProperty("user.name"), force, preferences);

        preferences.flush();
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

    public static void applySettings() {
        // Set the default values if they don't exist
        setDefaults(false);
        Preferences preferences = getPreferences();
        // Apply the settings to the game
        Audio.getInstance().setMusicVolume(preferences.getFloat(MUSIC_VOLUME));
        Audio.getInstance().setSFXVolume(preferences.getFloat(SFX_VOLUME));

        Log.info("Applied settings.");
    }

    public static Preferences getPreferences() {
        if (preferences != null) return preferences;
        return Gdx.app.getPreferences(PREFERENCES_LOCATION);
    }

}
