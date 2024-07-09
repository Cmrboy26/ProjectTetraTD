package net.cmr.rtd.game.files;

import java.io.IOException;
import java.util.HashSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.ProjectTetraTD;
import net.cmr.rtd.ProjectTetraTD.LevelValueKey;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.waves.WavesData;
import net.cmr.rtd.waves.WavesData.DifficultyRating;
import net.cmr.util.Log;

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
    final FileHandle waveFile;

    private String displayName;
    private QuestTask[] tasks;
    private int difficulty = -1;

    public QuestFile(LevelFolder level, String questFileName) {
        this.level = level;
        this.waveFile = level.getFolder().child("waves").child(questFileName);
    }

    /**
     * Constructor for a quest file for a game that has already been started (will be typically located in retrotowerdefense/saves/[worldname]/...)
     */
    public QuestFile(LevelFolder untamperedLevel, FileHandle waveFile) {
        this.level = untamperedLevel;
        this.waveFile = waveFile;
    }
    
    public QuestFile(LevelFolder untamperedLevel, QuestFile referenceFile) {
        FileHandle saveFile = referenceFile.getSaveFolder();
        FileHandle waveQuestFile = saveFile.child("wave.json");
        this.level = untamperedLevel;
        this.waveFile = waveQuestFile;
    }

    public LevelFolder getLevel() {
        return level;
    }

    public FileHandle getFile() {
        return waveFile;
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
            } else {
                tasks = new QuestTask[0];
            }

            displayName = (String) object.get("name");
            if (displayName == null) {
                displayName = getFile().nameWithoutExtension();
            }

            Long difficulty = (Long) object.get("difficulty");
            if (difficulty != null) {
                this.difficulty = difficulty.intValue();
            } else {
                this.difficulty = 0;
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

    public DifficultyRating getDifficulty() {
        if (difficulty == -1) {
            read();
        }
        return DifficultyRating.deserialize(difficulty);
    }

    public FileHandle getSaveFolder() {
        FileHandle savesFolder = Gdx.files.external("retrotowerdefense/saves/");
        FileHandle worldSaveFolder = savesFolder.child(level.getWorld().getFolder().name());
        return worldSaveFolder.child(getSaveFolderName());
    }

    public void deleteSave() {
        FileHandle saveFolder = getSaveFolder();
        saveFolder.deleteDirectory();
    }

    /**
     * Creates a save folder in the saves directory for the selected quest file.
     * Will overwrite any existing files in that directory.
     */
    public void createSave() {
        // Create a folder for the new save
        FileHandle levelDataFolder = level.getFolder();

        FileHandle newSaveFolder = getSaveFolder();
        //newSaveFolder.deleteDirectory();
        newSaveFolder.mkdirs();

        // Copy "world.dat" and the quest file (renamed to "wave.json") from the levelDataFolder to the newSaveFolder
        FileHandle worldFile = levelDataFolder.child("world.dat");
        FileHandle waveFile = getFile();

        FileHandle newWorldFile = newSaveFolder.child("world.dat");
        FileHandle newWaveFile = newSaveFolder.child("wave.json");

        worldFile.copyTo(newWorldFile);
        if (waveFile.file().getAbsolutePath().equals(newWaveFile.file().getAbsolutePath())) {
            return;
        }
        waveFile.copyTo(newWaveFile);

        FileHandle newPlayerData = newSaveFolder.child("players.dat");
        if (newPlayerData.exists()) {
            newPlayerData.delete();
        }
    }

    public void saveGame(World world) throws IOException {
        FileHandle saveFolder = getSaveFolder();

        FileHandle waveFile = saveFolder.child("wave.json");
        if (!waveFile.exists()) {
            waveFile.parent().mkdirs();
            waveFile.file().createNewFile();
            JSONObject waveData = new JSONObject();
            world.getWavesData().serialize(waveData);
            waveFile.writeString(waveData.toJSONString(), false);
        }

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

    /**
     * @return a hashset of tasks that have been completed with the provided game state.
     * The new list can be compared with a previous list to determine which tasks have been completed since the last list update using {@link #getJustCompletedTasks(HashSet, HashSet)}
     */
    public HashSet<Long> calculateCompletedTasks(int team, UpdateData updateData) {
        QuestTask[] tasks = getTasks();
        HashSet<Long> completedTasks = new HashSet<>();
        for (int i = 0; i < tasks.length; i++) {
            QuestTask task = tasks[i];
            boolean isComplete = task.isTaskComplete(updateData, team);
            if (!isComplete) {
                continue;
            }
            completedTasks.add(task.id);
        }
        return completedTasks;
    }

    /**
     * @return a hashset of tasks that are different between the previous and current task lists
     * To be used in conjunction with {@link #calculateCompletedTasks(int, UpdateData)} in, for example, 
     * one time UI notifications and saving completed tasks to file
     */
    public HashSet<Long> getJustCompletedTasks(HashSet<Long> previousTasks, HashSet<Long> currentTasks) {
        HashSet<Long> justCompletedTasks = new HashSet<>();
        for (Long task : currentTasks) {
            if (!previousTasks.contains(task)) {
                justCompletedTasks.add(task);
            }
        }
        return justCompletedTasks;
    }

    public boolean hasNoTasks() {
        return getTasks() == null || getTasks().length == 0;
    }

    public boolean areAllTasksCompleted() {
        if (hasNoTasks()) {
            return true;
        }
        Long[] completedTasks = ProjectTetraTD.getStoredLevelValue(this, LevelValueKey.COMPLETED_TASKS, Long[].class);
        if (completedTasks == null) {
            return false;
        }
        HashSet<Long> completedTasksSet = new HashSet<>();
        for (Long task : completedTasks) {
            completedTasksSet.add(task);
        }
        HashSet<Long> allTasks = new HashSet<>();
        for (QuestTask task : getTasks()) {
            allTasks.add((long) task.id);
        }
        if (!completedTasksSet.containsAll(allTasks)) {
            return false;
        }
        return true;
    }

    /**
     * This method should be used to serialize SAVED files, not the original quest files. It is meant to be used when resuming the last played game.
     */
    public String[] serialize() {
        WorldFolder world = level.getWorld();
        String worldName = world.getFolder().name();
        String levelName = level.getFolder().name();

        String[] serialized = new String[3];
        serialized[0] = worldName;
        serialized[1] = levelName;
        String savedWaveFileName = getSaveFolder().child("wave.json").file().getAbsolutePath();
        serialized[2] = savedWaveFileName;

        return serialized;
    }

    /**
     * This method should be used to deserialize SAVED files, not the original quest files. It is meant to be used when resuming the last played game.
     * @param serialized the serialized data from {@link #serialize()}
     */
    public static QuestFile deserialize(String[] serialized) {
        WorldFolder world = new WorldFolder(serialized[0]);
        LevelFolder level = new LevelFolder(world, serialized[1]);
        FileHandle absoluteWaveFile = Gdx.files.absolute(serialized[2]);
        QuestFile quest = new QuestFile(level, absoluteWaveFile);
        return quest;
    }


    @Override
    public String toString() {
        return getDisplayName();
    }

    
}
