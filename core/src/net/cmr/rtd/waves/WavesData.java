package net.cmr.rtd.waves;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.badlogic.gdx.files.FileHandle;

import net.cmr.rtd.game.world.EnemyFactory.EnemyType;

/**
 * Stores the data for each of the waves in the game.
 * Defines how long waves will be, what enemies will spawn, how many will spawn, etc.
 * 
 * Wave1
 *  - Time Start, Time End, Type, Quantity 0:00 - 0:30: 5 basic enemies
 *  - 
 * 
 */
public class WavesData {

    public String name;
    public DifficultyRating difficulty;
    public boolean endlessMode;
    public HashMap<Integer, Wave> waves;

    public WavesData() {
        this.waves = new HashMap<Integer, Wave>();
    }

    /**
     * Loads the waves data from a file
     * @param save The save file to load the waves data from
     * @return The waves data
     */
    public static WavesData load(FileHandle wavesData) {
        if (!wavesData.exists()) {
            throw new NullPointerException("Waves data file does not exist");
        }
        try {
            JSONParser parser = new JSONParser();
            JSONObject main = (JSONObject) parser.parse(wavesData.reader());
            return deserialize(main);
        } catch (Exception e) {
            throw new RuntimeException("Error reading waves data", e);
        }
    }

    /**
     * Saves the waves data to a file
     * @param wavesData The file to save the waves data to
     */
    public void save(FileHandle wavesData) {
        try {
            JSONObject main = new JSONObject();
            serialize(main);
            main.writeJSONString(wavesData.writer(false));
        } catch (IOException e) {
            throw new RuntimeException("Error writing waves data", e);
        }
    }

    public static WavesData deserialize(JSONObject main) throws IOException {
        WavesData data = new WavesData();

        data.name = (String) main.get("name");
        data.difficulty = DifficultyRating.deserialize(((Number) main.get("difficulty")).intValue());
        data.endlessMode = (boolean) main.get("endlessMode");
        
        JSONArray waves = (JSONArray) main.get("waves");
        for (Object wave : waves) {
            JSONObject waveObject = (JSONObject) wave;
            Wave newWave = new Wave(((Number) waveObject.get("waveTime")).floatValue());
            JSONArray waveUnits = (JSONArray) waveObject.get("waveUnits");
            for (Object waveUnit : waveUnits) {
                JSONObject waveUnitObject = (JSONObject) waveUnit;

                boolean distributedSpawn = waveUnitObject.containsKey("distributedSpawn");
                if (distributedSpawn) {
                    JSONArray miniArray = (JSONArray) waveUnitObject.get("distributedSpawn");
                    EnemyType type = EnemyType.valueOf((String) miniArray.get(0));
                    int quantity = ((Number) miniArray.get(1)).intValue();
                    float startTime = 0, endTime = newWave.getWaveTime();
                    newWave.addWaveUnit(new WaveUnit(startTime, endTime, type, quantity));
                } else {
                    newWave.addWaveUnit(new WaveUnit(
                            ((Number) waveUnitObject.get("startTime")).floatValue(),
                            ((Number) waveUnitObject.get("endTime")).floatValue(),
                            EnemyType.valueOf((String) waveUnitObject.get("type")),
                            ((Number) waveUnitObject.get("quantity")).intValue()
                    ));
                }
            }
            data.waves.put(data.waves.size() + 1, newWave);
        }

        return data;
    }

    @SuppressWarnings("unchecked")
    public void serialize(JSONObject main) throws IOException {
        main.put("name", name);
        main.put("difficulty", DifficultyRating.serialize(difficulty));
        main.put("endlessMode", endlessMode);
        
        JSONArray waves = new JSONArray();
        for (int waveNumber : this.waves.keySet()) {
            JSONObject wave = new JSONObject();

            Wave waveObject = this.waves.get(waveNumber);
            wave.put("waveTime", waveObject.getWaveTime());
            JSONArray waveUnitArray = new JSONArray();
            for (WaveUnit unit : waveObject.getWaveUnits()) {
                JSONObject waveUnit = new JSONObject();

                if (unit.getTimeStart() == 0 && unit.getTimeEnd() == waveObject.getWaveTime()) {
                    JSONArray miniArray = new JSONArray();
                    miniArray.add(unit.getType().toString());
                    miniArray.add(unit.getQuantity());
                    waveUnit.put("distributedSpawn", miniArray);
                    waveUnitArray.add(waveUnit);
                    continue;
                }

                waveUnit.put("startTime", unit.getTimeStart());
                waveUnit.put("endTime", unit.getTimeEnd());
                waveUnit.put("type", unit.getType().toString());
                waveUnit.put("quantity", unit.getQuantity());
                waveUnitArray.add(waveUnit);
            }
            wave.put("waveUnits", waveUnitArray);
            waves.add(wave);
        }
        main.put("waves", waves);
    }

    public String getName() { return name; }
    public DifficultyRating getDifficulty() { return difficulty; }

    /**
     * Returns the entities that will spawn within the time frame given by the elapsed time and delta time.
     * @param elapsedTime The time that has elapsed since the start of the wave
     * @param delta The time that has elapsed since the last frame
     * @param waveNumber The number of the wave
     * @return The entities that will spawn within the time frame given by the elapsed time and delta time
     */
    public EnemyType[] getEntities(float elapsedTime, float delta, int waveNumber) {
        Wave wave = waves.get(waveNumber);
        if (wave == null) {
            return null;
        }

        ArrayList<EnemyType> entities = new ArrayList<EnemyType>();
        for (WaveUnit unit : wave.getWaveUnits()) {
            EnemyType[] types = unit.getEnemiesToSpawn(elapsedTime, delta);
            if (types != null) {
                for (EnemyType type : types) {
                    entities.add(type);
                }
            }
        }
        return entities.toArray(new EnemyType[entities.size()]);
    }

    public Wave getWave(int waveNumber) {
        return waves.get(waveNumber);
    }
    
    public enum DifficultyRating {
        UNDEFINED(0),
        EASY(1),
        MEDIUM(2),
        HARD(3),
        INSANE(4),
        IMPOSSIBLE(5)
        ;

        int id;
        DifficultyRating(int id) {
            this.id = id;
        }

        public static int serialize(DifficultyRating rating) {
            if (rating == null) {
                return UNDEFINED.id;
            }
            return rating.id;
        }
        public static DifficultyRating deserialize(int id) {
            for (DifficultyRating rating : DifficultyRating.values()) {
                if (rating.id == id) {
                    return rating;
                }
            }
            return DifficultyRating.UNDEFINED;
        }
    }

    public int size() {
        return waves.size();
    }
}
