package net.cmr.rtd.game.files;

import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.waves.WavesData;

/**
 * Represents a wave file for a level inside of a level folder
 * 
 * Quest Data JSON Structure:
 * {
 *      // wave data information is also stored in this file
 *      "tasks": [
 * 
 *      ]
 * 
 * }
 * 
 */
public class QuestFile {

    final LevelFolder level;
    final FileHandle questFile;

    private String displayName;
    private QuestTask[] tasks;

    public QuestFile(LevelFolder level, String questFileName) {
        this.level = level;
        this.questFile = level.getFolder().child("waves").child(questFileName);
    }

    /**
     * Constructor for a quest file for a game that has already been started (will be typically located in retrotowerdefense/saves/[worldname]/...)
     */
    public QuestFile(LevelFolder untamperedLevel, FileHandle waveFile) {
        this.level = untamperedLevel;
        this.questFile = waveFile;
    }
    
    public QuestFile(LevelFolder untamperedLevel, QuestFile referenceFile) {
        FileHandle saveFile = referenceFile.getSaveFolder();
        FileHandle waveQuestFile = saveFile.child("wave.json");
        this.level = untamperedLevel;
        this.questFile = waveQuestFile;
    }

    public FileHandle getFile() {
        return questFile;
    }

    public void read() {
        FileHandle questData = getFile();
        JSONParser parser = new JSONParser();
        try {
            JSONObject object = (JSONObject) parser.parse(questData.reader());

            JSONArray taskList = (JSONArray) object.get("quests");
            if (taskList != null)  {
                tasks = new QuestTask[taskList.size()];
                for (int i = 0; i < taskList.size(); i++) {
                    JSONObject task = (JSONObject) taskList.get(i);
                    tasks[i] = QuestTask.readTask(task);
                }
            }

            displayName = (String) object.get("name");
            if (displayName == null) {
                displayName = getFile().nameWithoutExtension();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return true if there is an existing save file for this quest. Used in the resume game menu button
     */
    public boolean questFileExists() {
        FileHandle saveFolder = getSaveFolder();
        if (saveFolder.exists()) {
            FileHandle worldFile = saveFolder.child("world.dat");
            FileHandle waveFile = saveFolder.child("wave.json");
            return worldFile.exists() && waveFile.exists();
        }
        return false;
    }

    public String getSaveFolderName() {
        String levelName = level.getFolder().name();
        String questName = getDisplayName();

        // Create a minimized string for the save folder to have
        String minimizedString = levelName+"-"+questName;
        // remove all vowels from the string
        minimizedString = minimizedString.replaceAll("[ aeiou]", "");
        minimizedString = minimizedString.toLowerCase();
        return minimizedString;
    }

    public String getDisplayName() {
        if (displayName == null) {
            read();
        }
        return displayName;
    }

    public QuestTask[] getTasks() {
        if (tasks == null) {
            read();
        }
        return tasks;
    }

    public FileHandle getSaveFolder() {
        FileHandle savesFolder = Gdx.files.external("retrotowerdefense/saves/");
        FileHandle worldSaveFolder = savesFolder.child(level.getWorld().getFolder().name());
        return worldSaveFolder.child(getSaveFolderName());
    }

    /**
     * Creates a save folder in the saves directory for the selected quest file.
     * Will overwrite any existing files in that directory
     */
    public void createSave() {
        // Create a folder for the new save
        FileHandle levelDataFolder = level.getFolder();

        FileHandle newSaveFolder = getSaveFolder();
        newSaveFolder.deleteDirectory();
        newSaveFolder.mkdirs();

        // Copy "world.dat" and the quest file (renamed to "wave.json") from the levelDataFolder to the newSaveFolder
        FileHandle worldFile = levelDataFolder.child("world.dat");
        FileHandle waveFile = getFile();

        FileHandle newWorldFile = newSaveFolder.child("world.dat");
        FileHandle newWaveFile = newSaveFolder.child("wave.json");

        worldFile.copyTo(newWorldFile);
        waveFile.copyTo(newWaveFile);
    }

    public void saveGame(World world) throws IOException {
        FileHandle saveFolder = getSaveFolder();
        FileHandle worldFile = saveFolder.child("world.dat");
        byte[] worldData = GameObject.serializeGameObject(world);
        worldFile.writeBytes(worldData, false);

        FileHandle playersFile = saveFolder.child("players.dat");
        DataBuffer buffer = new DataBuffer();
        world.serializePlayerData(buffer);
        playersFile.writeBytes(buffer.toArray(), false);
    }

    /**
     * @return null if there is no world file, the world if there is a world file
     */
    public World loadWorld() {
        FileHandle saveFolder = getSaveFolder();
        FileHandle worldFile = saveFolder.child("world.dat");
        if (!worldFile.exists()) {
            return null;
        }
        byte[] data = worldFile.readBytes();
        World world = (World) GameObject.deserializeGameObject(data);
        return world;
    }

    /**
     * @return null if there is no wave file, the world if there is a wave file
     */
    public WavesData loadWavesData() {
        FileHandle saveFolder = getSaveFolder();
        FileHandle waveFile = saveFolder.child("wave.json");
        if (!waveFile.exists()) {
            return null;
        }
        WavesData wavesData = WavesData.load(waveFile);
        return wavesData;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
    
}
