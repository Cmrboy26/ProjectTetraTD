package net.cmr.rtd.game.files;

import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import net.cmr.rtd.ProjectTetraTD;

/**
 * Represents the world directory for a set of levels.
 * The folder should contain a folder for each level along with a file for the world data.
 * 
 * World Data JSON Structure:
 * {
 *   "name": "worldName", // Should be a string representing the name of the world
 *   "levels": [ // Should be an array of strings representing the level names. The order of the levels should be the order they are played in.
 *      "level1", 
 *      "level2"
 *   ]
 * }
 * 
 */
public class WorldFolder {

    public static final String WORLD_FOLDER = ProjectTetraTD.EXTERNAL_FILE_NAME+"worlds/";
    public static final String WORLD_FILE = "world.json";

    private final String worldName;
    private String displayName;
    private LevelFolder[] levelsList;

    public WorldFolder(String worldName) {
        this.worldName = worldName;
    }

    public FileHandle getFolder() {
        return Gdx.files.external(WORLD_FOLDER + worldName);
    }

    public FileHandle getWorldData() {
        return getFolder().child(WORLD_FILE);
    }

    /**
     * @return the ordered list of levels in the world
     */
    public LevelFolder[] readLevels() {
        if (levelsList == null) {
            read();
        }
        return levelsList;
    }

    public String getDisplayName() {
        if (displayName == null) {
            read();
        }
        return displayName;
    }

    private void read() {
        FileHandle worldData = getWorldData();
        JSONParser parser = new JSONParser();
        try {
            JSONObject object = (JSONObject) parser.parse(worldData.reader());

            String name = (String) object.get("name");
            displayName = name;

            // Get the levels array
            JSONArray levels = (JSONArray) object.get("levels");
            LevelFolder[] levelFolders = new LevelFolder[levels.size()];
            for (int i = 0; i < levels.size(); i++) {
                String level = (String) levels.get(i);
                levelFolders[i] = new LevelFolder(this, level);
            }
            
            levelsList = levelFolders;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static WorldFolder[] listWorlds() {
        FileHandle[] worldFolders = Gdx.files.external(WORLD_FOLDER).list();
        return Arrays.stream(worldFolders).map(file -> new WorldFolder(file.name())).filter(worldfolder -> !worldfolder.getFolder().name().equals("Tutorial")).toArray(WorldFolder[]::new);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
    
}
