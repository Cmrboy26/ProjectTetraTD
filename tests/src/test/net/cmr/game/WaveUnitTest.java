package net.cmr.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.HashMap;

import org.json.simple.JSONObject;

import net.cmr.rtd.game.world.EnemyFactory.EnemyType;
import net.cmr.rtd.waves.Wave;
import net.cmr.rtd.waves.WaveUnit;
import net.cmr.rtd.waves.WavesData;
import net.cmr.rtd.waves.WavesData.DifficultyRating;

public class WaveUnitTest {
    
    public void testWaveDataSerializationAndDeserialization() {
        WavesData data = new WavesData();
        data.name = "Quest One";
        data.difficulty = DifficultyRating.EASY;
        data.endlessMode = false;
        data.waves = new HashMap<Integer, Wave>();

        // Wave One
        Wave wave = new Wave(30);
        wave.addWaveUnit(new WaveUnit(0, 30, EnemyType.BASIC_ONE, 5));
        wave.addWaveUnit(new WaveUnit(5, 6.235235734573f, EnemyType.BASIC_TWO, 30));
        wave.addWaveUnit(new WaveUnit(10, 30, EnemyType.BASIC_ONE, 5));
        wave.addWaveUnit(new WaveUnit(15, EnemyType.BASIC_TWO));
        wave.addWaveUnit(new WaveUnit(0, EnemyType.BASIC_ONE, 1000));
        wave.addWaveUnit(new WaveUnit(wave, EnemyType.BASIC_ONE, 5));
        data.waves.put(1, wave);

        // Wave Two
        wave = new Wave(173);
        wave.addWaveUnit(new WaveUnit(0, 30, EnemyType.BASIC_ONE, 5));
        wave.addWaveUnit(new WaveUnit(55, 6.235235734573f, EnemyType.BASIC_TWO, 30));
        wave.addWaveUnit(new WaveUnit(10, 30, EnemyType.BASIC_ONE, 5));
        wave.addWaveUnit(new WaveUnit(15, EnemyType.BASIC_TWO));
        wave.addWaveUnit(new WaveUnit(0, EnemyType.BASIC_ONE, 1000));
        wave.addWaveUnit(new WaveUnit(wave, EnemyType.BASIC_ONE, 38));
        data.waves.put(2, wave);

        String serializedString = null;
        JSONObject main = new JSONObject();
        try {
            data.serialize(main);
            serializedString = main.toJSONString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String deserializedString = null;
        try {
            WavesData newData = WavesData.deserialize(main);
            JSONObject newMain = new JSONObject();
            newData.serialize(newMain);
            deserializedString = newMain.toJSONString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(serializedString, deserializedString);
    }

}
