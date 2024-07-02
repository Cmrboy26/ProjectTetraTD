package net.cmr.rtd.waves;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.badlogic.gdx.files.FileHandle;

import net.cmr.rtd.game.endless.EndlessUtils;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.EnemyFactory.EnemyType;
import net.cmr.rtd.game.world.entities.TowerEntity;

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
    public float preparationTime = -1;
    public int startingMoney;
    public int startingHealth;
    public HashMap<Integer, Wave> waves;
    public int wavesPerComponentTarget;
    public EndlessUtils endlessUtils;

    public static int DEFAULT_STARTING_MONEY = 100;
    public static int DEFAULT_STARTING_HEALTH = 50;
    public static int DEFAULT_WAVES_PER_COMPONENT_TARGET = 3;

    public int sinusoidalGameDifficultyPeriod = 7; // 7 waves to go from peak to peak of the sinusoidal function
    public float sinusoidalGameDifficultyAmplitude = 1/21f; // (1/21) * 100 = + or - 9% difficulty 
    public float requiredDPSIncreasePercentageForStagnancy = 0.10f; // 10% increase in DPS required to not be considered stagnant
    public float dpsThresholdScale = 1/4f; // 25% of the enemy's health must be dealt in DPS in order to have that enemy appear in the wave
    public float sinusoidalCenterValue = 1.03f; // The "center" percentage of the sinusoidal function
    public int stagnationThreshold = 2; // The number of waves that the player can be stagnant before penalties will be applied
    public float stagnationDifficultyIncrease = 0.2f; // The penalty to the player's health and money for being stagnant

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

        if (data.endlessMode) {
            if (main.containsKey("recommendedEndlessSettingsDifficulty")) {
                data.setRecommendedValuesForEndless(DifficultyRating.deserialize(((Number) main.get("recommendedEndlessSettingsDifficulty")).intValue()));
            }

            data.requiredDPSIncreasePercentageForStagnancy = main.containsKey("requiredDPSIncreasePercentageForStagnancy") ? ((Number) main.get("requiredDPSIncreasePercentageForStagnancy")).floatValue() : data.requiredDPSIncreasePercentageForStagnancy;
            data.dpsThresholdScale = main.containsKey("dpsThresholdScale") ? ((Number) main.get("dpsThresholdScale")).floatValue() : data.dpsThresholdScale;
            data.sinusoidalCenterValue = main.containsKey("sinusoidalCenterValue") ? ((Number) main.get("sinusoidalCenterValue")).floatValue() : data.sinusoidalCenterValue;
            data.sinusoidalGameDifficultyPeriod = main.containsKey("sinusoidalGameDifficultyPeriod") ? ((Number) main.get("sinusoidalGameDifficultyPeriod")).intValue() : data.sinusoidalGameDifficultyPeriod;
            data.sinusoidalGameDifficultyAmplitude = main.containsKey("sinusoidalGameDifficultyAmplitude") ? ((Number) main.get("sinusoidalGameDifficultyAmplitude")).floatValue() : data.sinusoidalGameDifficultyAmplitude;
            data.stagnationThreshold = main.containsKey("stagnationThreshold") ? ((Number) main.get("stagnationThreshold")).intValue() : data.stagnationThreshold;
            data.stagnationDifficultyIncrease = main.containsKey("stagnationDifficultyIncrease") ? ((Number) main.get("stagnationDifficultyIncrease")).floatValue() : data.stagnationDifficultyIncrease;
        }
        
        if (main.containsKey("preparationTime")) {
            data.preparationTime = ((Number) main.get("preparationTime")).floatValue();
        }
        if (data.preparationTime == -1) {
            throw new IOException("Preparation time has not been set for the waves data.");
        }

        if (main.containsKey("wavesPerComponentTarget")) {
            data.wavesPerComponentTarget = ((Number) main.get("wavesPerComponentTarget")).intValue();
        } else {
            data.wavesPerComponentTarget = DEFAULT_WAVES_PER_COMPONENT_TARGET;
        }

        if (main.containsKey("startingMoney")) {
            data.startingMoney = ((Number) main.get("startingMoney")).intValue();
        } else {
            data.startingMoney = DEFAULT_STARTING_MONEY;
        }
        if (main.containsKey("startingHealth")) {
            data.startingHealth = ((Number) main.get("startingHealth")).intValue();
        } else {
            data.startingHealth = DEFAULT_STARTING_HEALTH;
        }
        
        JSONArray waves = (JSONArray) main.get("waves");
        for (Object wave : waves) {
            JSONObject waveObject = (JSONObject) wave;
            Wave newWave = new Wave(((Number) waveObject.get("waveTime")).floatValue());
            if (waveObject.containsKey("warnPlayer")) {
                boolean warnPlayer = (Boolean) waveObject.get("warnPlayer");
                newWave.setWarnPlayer(warnPlayer);
            }
            if (waveObject.containsKey("additionalPrep")) {
                int additionalPreparationTime = ((Number) waveObject.get("additionalPrep")).intValue();
                newWave.setAdditionalPrepTime(additionalPreparationTime);
            }
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
        main.put("startingMoney", startingMoney);
        main.put("startingHealth", startingHealth);
        main.put("wavesPerComponentTarget", wavesPerComponentTarget);

        if (endlessMode) {
            main.put("requiredDPSIncreasePercentageForStagnancy", requiredDPSIncreasePercentageForStagnancy);
            main.put("dpsThresholdScale", dpsThresholdScale);
            main.put("sinusoidalCenterValue", sinusoidalCenterValue);
            main.put("sinusoidalGameDifficultyPeriod", sinusoidalGameDifficultyPeriod);
            main.put("sinusoidalGameDifficultyAmplitude", sinusoidalGameDifficultyAmplitude);
            main.put("stagnationThreshold", stagnationThreshold);
        }

        main.put("preparationTime", preparationTime);
        
        JSONArray waves = new JSONArray();
        for (int waveNumber : this.waves.keySet()) {
            JSONObject wave = new JSONObject();

            Wave waveObject = this.waves.get(waveNumber);
            wave.put("waveTime", waveObject.getWaveTime());
            if (waveObject.shouldWarnPlayer()) {
                wave.put("warnPlayer", true);
            }
            if (waveObject.getAdditionalPrepTime() != 0) {
                wave.put("additionalPrep", waveObject.getAdditionalPrepTime());
            }
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
    public EnemyType[] getEntities(UpdateData data, float elapsedTime, float delta, int waveNumber) {
        Wave wave = waves.get(waveNumber);
        if (wave == null) {
            return null;
        }

        ArrayList<EnemyType> entities = new ArrayList<EnemyType>();
        if (endlessMode && wave.waveUnits.size() == 0 && elapsedTime >= -.5f) {
            endlessUtils.generateWaveUnits(wave, data.getManager());
        }
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

    public Wave getNextWave(int waveNumber, UpdateData updateData) {
        if (updateData.isClient()) throw new UnsupportedOperationException("Cannot get next wave on client.");

        Wave at = getWave(waveNumber);

        if (endlessMode && endlessUtils == null) {
            endlessUtils = new EndlessUtils(this);
        }
        if (endlessMode && at == null) {
            Wave next = endlessUtils.generateDynamicWave(updateData.getManager());
            waves.put(waveNumber, next);
            return next;
        }

        return at;
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

        public final int id;
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

    public void setRecommendedValuesForEndless(DifficultyRating difficulty) {
        int[] period = { 7, 7, 7, 7, 6 };
        float[] amplitude = { 1/21f, 1/21f, 1/21f, 1/18f, 1/14f };
        float[] requiredDPSIncrease = { 0.7f, 0.8f, 0.10f, 0.12f, 0.14f };
        float[] dpsThreshold = { 1/4f, 1/4f, 1/4f, 1/4f, 1/4f };
        float[] centerValue = { 1.01f, 1.02f, 1.03f, 1.04f, 1.06f };
        float[] preparationTime = { 11, 10, 9, 8, 7 };
        int[] stagnationThreshold = { 3, 2, 2, 2, 1 };
        float[] stagnationDifficultyIncrease = { 0.1f, 0.15f, 0.2f, 0.25f, 0.3f };

        sinusoidalGameDifficultyPeriod = period[difficulty.id - 1];
        sinusoidalGameDifficultyAmplitude = amplitude[difficulty.id - 1];
        requiredDPSIncreasePercentageForStagnancy = requiredDPSIncrease[difficulty.id - 1];
        dpsThresholdScale = dpsThreshold[difficulty.id - 1];
        sinusoidalCenterValue = centerValue[difficulty.id - 1];
        this.preparationTime = preparationTime[difficulty.id - 1];
        this.stagnationThreshold = stagnationThreshold[difficulty.id - 1];
        this.stagnationDifficultyIncrease = stagnationDifficultyIncrease[difficulty.id - 1];
    }

    public int size() {
        return waves.size();
    }
    public float getPreparationTime() {
        return preparationTime;
    }
}
