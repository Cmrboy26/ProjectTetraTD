package net.cmr.rtd.game;

import com.badlogic.gdx.Files.FileType;

import java.io.File;
import java.nio.channels.FileLock;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import net.cmr.rtd.RetroTowerDefense;
import net.cmr.rtd.game.GameManager.GameManagerDetails;

/**
 * A class to handle saving and loading game data
 */
public class GameSave {
    
    private static final String LOCATION = "saves/"; 
    private final String saveName;

    public GameSave(String saveName) {
        this.saveName = saveName;
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

    public GameManager saveGame(GameManager manager) {
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
                System.out.println(absoluteLocation + File.separator + LOCATION + saveName);
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

}
