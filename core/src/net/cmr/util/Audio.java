package net.cmr.util;

import java.util.HashMap;
import java.util.Objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Disposable;

import net.cmr.rtd.game.world.entities.Player;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.screen.GameScreen;

/**
 * The Audio class handles the management and playback of sound effects and music in the game.
 */
public class Audio implements Disposable {

    private static final String SFX_PATH = "audio/sfx/";
    private static final String MUSIC_PATH = "audio/music/";

    public static final float SOUND_DROPOFF_DISTANCE = 9; // tiles

    /**
     * Represents the sound effects used in the game.
     */
    public enum GameSFX {
        BUTTON_CLICK("button_click.wav"),
        BUTTON_HOVER("button_hover.wav"),
        BUTTON_PRESS("button_press.wav"),
        BUTTON_RELEASE("button_release.wav"),
        BUTTON_UNHOVER("button_unhover.wav"),
        HIT1("hit1.wav"),
        HIT2("hit2.wav"),
        HIT3("hit3.wav"),
        FIREBALL_LAUNCH("gunshot.wav"),
        FIREBALL_HIT("fireball_hit.wav"),
        SHOOT("shoot.wav"),
        FIRE_DAMAGE("fire_damage.wav"),

        PLACE1("place1.wav"),
        PLACE2("place2.wav"),
        CLICK("select.wav"),
        SELECT("trueSelect.wav"),
        DESELECT("falseSelect.wav"),
        WARNING("warning.wav"),
        SCARY_WARNING("scary_warning.wav"),
        UPGRADE_COMPLETE("upgrade_complete.wav"),
        ACHIEVEMENT_GET("achievement_get.wav"),
        ;

        private final String fileName;

        private GameSFX(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Returns the unique ID of the sound effect.
         *
         * @return The ID of the sound effect.
         */
        public int getID() {
            return name().hashCode();
        }

        /**
         * Returns the file name of the sound effect.
         *
         * @return The file name of the sound effect.
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * Returns the file handle of the sound effect.
         *
         * @return The file handle of the sound effect.
         */
        public FileHandle getFileHandle() {
            return Gdx.files.internal(SFX_PATH + getFileName());
        }
        public static GameSFX random(GameSFX... sfx) {
            return sfx[(int) (Math.random() * sfx.length)];
        }
    }

    /**
     * Represents the music tracks used in the game.
     */
    public enum GameMusic {
        GAME_1("game1.mp3"),
        GAME_2("game2.mp3"),
        GAME_3("game3.mp3")
        ;

        private final String fileName;

        private GameMusic(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Returns the unique ID of the music track.
         *
         * @return The ID of the music track.
         */
        public int getID() {
            return name().hashCode();
        }

        /**
         * Returns the file name of the music track.
         *
         * @return The file name of the music track.
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * Returns the file handle of the music track.
         *
         * @return The file handle of the music track.
         */
        public FileHandle getFileHandle() {
            return Gdx.files.internal(MUSIC_PATH + getFileName());
        }

        public static GameMusic random(GameMusic... music) {
            return music[(int) (Math.random() * music.length)];
        }

        public static GameMusic menuMusic() {
            return null;
        }
    }

    private HashMap<Integer, Sound> sfxMap;
    private HashMap<Integer, Music> musicMap;

    private Music currentMusic = null;
    private float musicVolume = 1f;
    private float sfxVolume = 1f;

    private Audio() {
        sfxMap = new HashMap<>();
        musicMap = new HashMap<>();

        if(Gdx.files == null) {
            return;
        }

        for (GameSFX sfx : GameSFX.values()) {
            initializeSFX(sfx.getID(), sfx.getFileHandle());
        }
        for (GameMusic music : GameMusic.values()) {
            initializeMusic(music.getID(), music.getFileHandle());
        }

        Log.info("Audio initialized");
    }

    /**
     * Initializes a sound effect with the given ID and file handle.
     *
     * @param id     The ID of the sound effect.
     * @param handle The file handle of the sound effect.
     */
    public void initializeSFX(int id, FileHandle handle) {
        if(!handle.exists()) {
            return;
        }
        Log.info("Initializing SFX: " + handle.name());
        sfxMap.put(id, Gdx.audio.newSound(handle));
    }

    /**
     * Initializes a music track with the given ID and file handle.
     *
     * @param id     The ID of the music track.
     * @param handle The file handle of the music track.
     */
    public void initializeMusic(int id, FileHandle handle) {
        if(!handle.exists()) {
            return;
        }
        Log.info("Initializing music: " + handle.name());
        musicMap.put(id, Gdx.audio.newMusic(handle));
    }

    /**
     * Plays a sound effect with the given volume.
     *
     * @param sfx    The sound effect to play.
     * @param volume The volume of the sound effect.
     * @return The ID of the sound effect instance.
     */
    public long playSFX(GameSFX sfx, float volume) {
        return playSFX(sfx.getID(), volume, 1, 0);
    }

    /**
     * Plays a sound effect with the given volume and pitch.
     *
     * @param sfx    The sound effect to play.
     * @param volume The volume of the sound effect.
     * @param pitch  The pitch of the sound effect.
     * @return The ID of the sound effect instance.
     */
    public long playSFX(GameSFX sfx, float volume, float pitch) {
        return playSFX(sfx.getID(), volume, pitch, 0);
    }

    /**
     * Plays a sound effect with the given volume, pitch, and pan.
     *
     * @param sfx    The sound effect to play.
     * @param volume The volume of the sound effect.
     * @param pitch  The pitch of the sound effect.
     * @param pan    The pan of the sound effect.
     * @return The ID of the sound effect instance.
     */
    public long playSFX(GameSFX sfx, float volume, float pitch, float pan) {
        return playSFX(sfx.getID(), volume, pitch, pan);
    }

    /**
     * Plays a sound effect with the given ID, volume, pitch, and pan.
     *
     * @param id     The ID of the sound effect.
     * @param volume The volume of the sound effect.
     * @param pitch  The pitch of the sound effect.
     * @param pan    The pan of the sound effect.
     * @return The ID of the sound effect instance.
     */
    public long playSFX(int id, float volume, float pitch, float pan) {
        Sound sound = sfxMap.get(id);
        if(Objects.isNull(sound)) {
            return -1;
        }
        long output = sound.play(volume * sfxVolume, pitch, pan);
        return output;
    }

    /**
     * Plays a sound effect in the world with the given volume and pitch.
     * @param position The position of the sound effect.
     * @see #playSFX(GameSFX, float, float)
     */
    public void worldSFX(GameSFX sfx, float volume, float pitch, Vector2 position, GameScreen screen) {
        if (screen == null) {
            return;
        }
        float newVolume = volume;
        Player player = screen.getLocalPlayer();
        if (player != null) {
            Vector2 playerPos = player.getPosition().cpy();
            Vector2 soundPos = position.cpy();
            float distance = playerPos.dst(soundPos) / Tile.SIZE; // distance in tiles
            float dropoffDistance = SOUND_DROPOFF_DISTANCE;
            if (distance > dropoffDistance) {
                return;
            }
            newVolume *= Math.sqrt(dropoffDistance - distance) / Math.sqrt(dropoffDistance);
        }
        playSFX(sfx, newVolume, pitch);
    }

    /**
     * Plays the specified music track.
     *
     * @param music The music track to play.
     */
    public void playMusic(GameMusic music) {
        playMusic(music.getID());
    }

    /**
     * Plays the music track with the given ID.
     *
     * @param id The ID of the music track.
     */
    public void playMusic(int id) {
        if (musicMap.get(id) == null) {
            return;
        }
        if (currentMusic != null) {
            currentMusic.stop();
        }

        currentMusic = musicMap.get(id);
        currentMusic.setVolume(musicVolume);
        currentMusic.setLooping(true);
        currentMusic.setOnCompletionListener((Music music) -> {
            currentMusic = null;
        });
        currentMusic.play();
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
    }

    /**
     * Sets the volume of the music.
     *
     * @param volume The volume of the music.
     */
    public void setMusicVolume(float volume) {
        musicVolume = volume;
        if (currentMusic != null) {
            currentMusic.setVolume(volume);
        }
    }

    /**
     * Sets the volume of the sound effects.
     *
     * @param volume The volume of the sound effects.
     */
    public void setSFXVolume(float volume) {
        sfxVolume = volume;
    }

    @Override
    public void dispose() {
        for (Sound sfx : sfxMap.values()) {
            sfx.dispose();
        }
        for (Music music : musicMap.values()) {
            music.dispose();
        }
    }

    // Singleton

    private static Audio instance;

    /**
     * Returns the singleton instance of the Audio class.
     *
     * @return The Audio instance.
     */
    public static Audio getInstance() {
        if (instance == null) {
            instance = new Audio();
        }
        return instance;
    }

    public static Button addClickSFX(Button button) {
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Audio.getInstance().playSFX(GameSFX.CLICK, 1f);
            }
        });
        return button;
    }

    @SuppressWarnings("rawtypes") 
    public static SelectBox addClickSFX(SelectBox selectBox) {
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Audio.getInstance().playSFX(GameSFX.SELECT, 1f);
            }
        });
        selectBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Audio.getInstance().playSFX(GameSFX.CLICK, 1f);
            }
        });
        return selectBox;
    }

    /**
     * Initializes the Audio system.
     */
    public static void initializeAudio() {
        getInstance();
    }

    /**
     * Disposes the Audio system.
     */
    public static void disposeAudio() {
        getInstance().dispose();
    }
}
