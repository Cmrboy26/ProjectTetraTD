package net.cmr.rtd.game;

import java.io.File;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import net.cmr.rtd.RetroTowerDefense;
import net.cmr.rtd.game.GameManager.GameManagerDetails;
import net.cmr.rtd.waves.WavesData;

/**
 * A class to handle saving and loading game data
 * 
 * Saved Game Structure:
 * game-name/
 *    world.dat - stores map, enemy, team, and tower data
 *    player.dat - stores player data
 *    wave.json - stores wave data for the game
 * 
 * Playable Level Structure:
 * level-name/
 *    world.dat - stores map and team data (no enemy or tower data included)
 *    waves/
 *       easyWave.json - stores wave data for the easy difficulty (specified in {@link WavesData})
 *       mediumWave.json - stores wave data for the medium difficulty
 *       ... - stores wave data for the other difficulties
 **/
@Deprecated
public class GameSave {
    
    private static final String LOCATION = "retrotowerdefense/saves/"; 
    private final String saveName;

    @Deprecated
    public GameSave(String saveName) {
        this.saveName = saveName;
    }

    public GameSave(String worldName, String levelName) {
        this.saveName = worldName + "/" + levelName;
    }

    /**
     * Reads details about the saved game from the save file
     * @param details
     * @return
     */
    public GameManager loadGame(GameManagerDetails details) {
        GameManager manager = new GameManager(details);
        return manager;
    }

    /**
     * Returns the FileHandle for the save folder
     * @param type The type of file to return (Can only be External or Absolute)
     * @return The FileHandle for the save folder
     * @throws IllegalArgumentException If the file type is not External or Absolute
     */
    public FileHandle getSaveFolder(FileType type) {
        switch (type) {
            case External:
                return Gdx.files.external(LOCATION + saveName);
            case Local:
                throw new IllegalArgumentException("Local file type not supported for save folder");
            case Classpath:
                throw new IllegalArgumentException("Classpath file type not supported for save folder");
            case Internal:
                throw new IllegalArgumentException("Internal file type not supported for save folder");
            case Absolute:
                String absoluteLocation = System.getProperty("user.dir");
                return Gdx.files.absolute(absoluteLocation + File.separator + LOCATION + saveName);
            default:
                throw new IllegalArgumentException("Invalid file type");
        }
    }

    /**
     * Returns the FileHandle for the save folder
     * This is dependant on whether the game 
     *      * @return
     */
    public FileHandle getSaveFolder() {
        if (RetroTowerDefense.instanceExists()) {
            return getSaveFolder(FileType.External);
        } else {
            return getSaveFolder(FileType.Absolute);
        }
    }

    public FileHandle getWorldFile(FileType type) {
        return getSaveFolder(type).child("world.dat");
    }

    public FileHandle getWaveFile(FileType type) {
        return getSaveFolder(type).child("wave.json");
    }

    public static GameSave[] getSaveList() {
        FileHandle[] files = Gdx.files.external(LOCATION).list();
        GameSave[] saves = new GameSave[files.length];
        for(int i = 0; i < files.length; i++) {
            saves[i] = new GameSave(files[i].name());
        }
        return saves;
    }

    public String getName() {
        return saveName;
    }

    public GameSave copySave(LevelSave levelSave) {
        byte[] wavedata = getWaveFile(FileType.External).readBytes();
        GameSave save = levelSave.createSave(saveName, null, true, wavedata);
        save.getWaveFile(FileType.External).writeBytes(wavedata, false);
        return save;
    }

    public void delete() {
        getSaveFolder().deleteDirectory();
    }

}
