package net.cmr.rtd.game.files;

import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Null;

import net.cmr.rtd.RetroTowerDefense;
import net.cmr.rtd.RetroTowerDefense.LevelValueKey;

/** 
 * Level Data JSON Structure:
 * {
 *   "name": "levelName", // Should be a string representing the name of the level
 *   "levels": [ // Should be an ORDERED array of strings representing the quests
 *      "level1", 
 *      "level2"
 *   ]
 * }
 * 
 */
public class LevelFolder {
    
    public static final String LEVEL_FILE = "level.json";

    private final WorldFolder world;
    private final String levelName;
    private String displayName;
    private QuestFile[] questsList;
    private String[] unlockRequirements;

    public LevelFolder(WorldFolder world, String levelName) {
        this.world = world;
        this.levelName = levelName;
    }

    public FileHandle getFolder() {
        return world.getFolder().child(levelName);
    }

    public FileHandle getLevelData() {
        return getFolder().child(LEVEL_FILE);
    }

    /**
     * @return the ordered list of levels in the world
     */
    public QuestFile[] readQuests() {
        if (questsList == null) {
            read();
        }
        return questsList;
    }

    public String getDisplayName() {
        if (displayName == null) {
            read();
        }
        return displayName;
    }

    public String[] getUnlockRequirements() {
        if (unlockRequirements == null) {
            read();
        }
        return unlockRequirements;
    }

    private void read() {
        FileHandle levelData = getLevelData();
        JSONParser parser = new JSONParser();
        try {
            JSONObject object = (JSONObject) parser.parse(levelData.reader());

            // Get the display name
            displayName = (String) object.get("name");
            if (displayName == null) {
                displayName = levelName;
            }

            // Get the levels array
            JSONArray quests = (JSONArray) object.get("quests");
            QuestFile[] questFiles = Arrays.stream(quests.toArray()).map(levelName -> new QuestFile(this, (String) levelName)).toArray(QuestFile[]::new);
            questsList = questFiles;
            
            JSONArray requirements = (JSONArray) object.get("unlockRequirements");
            if (requirements == null) {
                unlockRequirements = new String[0];
            } else {
                unlockRequirements = Arrays.stream(requirements.toArray()).map(levelName -> (String) levelName).toArray(String[]::new);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isLevelLocked() {
        boolean isLocked = false;
        for (String requirement : getUnlockRequirements()) {
            // Format: "worldname/levelname/questfile/questtaskid"
            String[] parts = requirement.split("/");
            if (parts.length != 4) {
                continue;
            }
            WorldFolder world = new WorldFolder(parts[0]);
            LevelFolder level = new LevelFolder(world, parts[1]);
            QuestFile quest = new QuestFile(level, parts[2]);
            QuestTask task = QuestTask.getTask(quest, Long.parseLong(parts[3]));
            
            @Null Long[] completedTasks = RetroTowerDefense.getStoredLevelValue(quest, LevelValueKey.COMPLETED_TASKS, Long[].class);
            if (completedTasks == null) {
                // If no tasks are completed for the quest, then the target task could not have been completed, thus the level is locked
                isLocked = true;
                break;
            }

            boolean taskCompleted = Arrays.stream(completedTasks).anyMatch(id -> id == task.id);
            if (!taskCompleted) {
                // The task has not been completed, so the level is locked
                isLocked = true;
                break;
            }
            // The target task has been completed. Continue checking the other requirements
        }
        return isLocked;
    }

    public WorldFolder getWorld() {
        return world;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

}
