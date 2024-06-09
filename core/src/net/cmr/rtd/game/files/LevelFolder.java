package net.cmr.rtd.game.files;

import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WorldFolder getWorld() {
        return world;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

}
