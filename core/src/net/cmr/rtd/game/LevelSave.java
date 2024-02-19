package net.cmr.rtd.game;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class LevelSave {
    
    private static final String LOCATION = "retrotowerdefense/levels/"; 
    private final String levelName;

    public LevelSave(String levelName) {
        this.levelName = levelName;
    }

    /**
     * Returns the FileHandle for the level folder
     * @param type The type of file to return (Can only be External or Absolute)
     * @return The FileHandle for the level folder
     * @throws IllegalArgumentException If the file type is not External or Absolute
     */
    public FileHandle getLevelFolder(FileType type) {
        switch (type) {
            case External:
                return Gdx.files.external(LOCATION + levelName);
            case Local:
                throw new IllegalArgumentException("Local file type not supported for level folder");
            default:
                throw new IllegalArgumentException("Unsupported file type");
        }
    }
    
    /**
     * Returns the FileHandle for the world file
     * @param type The type of file to return (Can only be External or Absolute)
     * @return The FileHandle for the world file
     * @throws IllegalArgumentException If the file type is not External or Absolute
     */
    public FileHandle getWorldFile(FileType type) {
        return getLevelFolder(type).child("world.dat");
    }
    
    /**
     * Returns the FileHandle for the waves folder
     * @param type The type of file to return (Can only be External or Absolute)
     * @return The FileHandle for the waves folder
     * @throws IllegalArgumentException If the file type is not External or Absolute
     */
    public FileHandle getWavesFolder(FileType type) {
        return getLevelFolder(type).child("waves/");
    }

    public FileHandle[] getWavesFiles(FileType type) {
        return getWavesFolder(type).list();
    } 

    /**
     * Creates a new save folder for the level.
     * OVERWRITES any existing save folder with the same name.
     * @param folderName The name of the folder to create
     * @param waveName The name of the wave to use for the level
     * @return The new save folder
     */
    public GameSave createSave(String folderName, String waveName) {

        if (folderName == null || folderName.isEmpty()) {
            throw new IllegalArgumentException("Folder name cannot be null or empty");
        }
        if (waveName == null || waveName.isEmpty()) {
            throw new IllegalArgumentException("Wave name cannot be null or empty");
        }
        for (char c : folderName.toCharArray()) {
            if (c == '/' || c == '\\' || c == ':' || c == '*' || c == '?' || c == '"' || c == '<' || c == '>' || c == '|') {
                throw new IllegalArgumentException("Folder name cannot contain any of the following characters: / \\ : * ? \" < > |");
            }
        }
        for (char c : waveName.toCharArray()) {
            if (c == '/' || c == '\\' || c == ':' || c == '*' || c == '?' || c == '"' || c == '<' || c == '>' || c == '|') {
                throw new IllegalArgumentException("Wave name cannot contain any of the following characters: / \\ : * ? \" < > |");
            }
        }

        FileHandle desiredWaveFile = getWavesFolder(FileType.External).child(waveName+".json");
        if (!desiredWaveFile.exists()) {
            throw new IllegalArgumentException("Wave file does not exist");
        }

        GameSave save = new GameSave(folderName);
        FileHandle folder = save.getSaveFolder();
        if (folder.exists()) {
            folder.deleteDirectory();
        }
        folder.mkdirs();

        // Copy the world file to the new save folder
        FileHandle levelWorldFile = getWorldFile(FileType.External);
        FileHandle saveWorldFile = save.getWorldFile(FileType.External);
        levelWorldFile.copyTo(saveWorldFile);

        // Copy the selected wave file to the new save folder
        FileHandle levelWave = getWavesFolder(FileType.External).child(waveName+".json");
        FileHandle saveWave = save.getWaveFile(FileType.External);
        levelWave.copyTo(saveWave);

        return save;
    }

}
