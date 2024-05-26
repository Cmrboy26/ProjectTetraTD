package net.cmr.rtd.waves;

import java.util.ArrayList;

import com.badlogic.gdx.utils.Null;

import net.cmr.rtd.game.world.EnemyFactory.EnemyType;

public class WaveUnit {
    
    private final float timeStart;
    private final float timeEnd;
    private final EnemyType type;
    private final int quantity;

    /**
     * Spawns a number of enemies of the given type between the given times during the wave
     * @param timeStart The time the enemies will start spawning
     * @param timeEnd The time the enemies will stop spawning
     * @param type The type of enemy to spawn
     * @param quantity The number of enemies to spawn (evenly distributed between timeStart and timeEnd)
     */
    public WaveUnit(float timeStart, float timeEnd, EnemyType type, int quantity) {
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.type = type;
        this.quantity = quantity;

        timeStart = Math.max(0, timeStart);
        timeEnd = Math.max(0, timeEnd);
        quantity = Math.max(0, quantity);
    }

    /**
     * Spawns a number of enemies of the given type at a specific time during the wave
     */
    public WaveUnit(float time, EnemyType type, int quantity) {
        this(time, time, type, quantity);
    }

    /**
     * Spawns a single enemy of the given type at a specific time during the wave
     */
    public WaveUnit(float time, EnemyType type) {
        this(time, time, type, 1);
    }
    
    /**
     * Spawns a number of enemies evenly throughout the duration of the wave
     */
    public WaveUnit(Wave wave, EnemyType type, int quantity) {
        this(0, wave.getWaveTime(), type, quantity);
    }

    public float getTimeStart() { return timeStart; }
    public float getTimeEnd() { return timeEnd; }
    public EnemyType getType() { return type; }
    public int getQuantity() { return quantity; }

        //                     elapsed time                 delta
        // | --------------------------------------------- | --- |
    public @Null EnemyType[] getEnemiesToSpawn(float elapsedTime, float delta) {
        ArrayList<EnemyType> enemies = new ArrayList<EnemyType>();

        // If the time doesn't apply to this wave unit, return null
        if (!(isBetween(elapsedTime, timeStart, timeEnd) || isBetween(elapsedTime + delta, timeStart, timeEnd) || isBetween(timeStart, elapsedTime, elapsedTime + delta) || isBetween(timeEnd, elapsedTime, elapsedTime + delta))) {
            return null;
        }

        // Single enemy test
        if (timeStart == timeEnd && isBetween(timeStart, elapsedTime, elapsedTime + delta)) {
            for (int i = 0; i < quantity; i++) {
                enemies.add(type);
            }
            return enemies.toArray(new EnemyType[enemies.size()]);
        }

        // Multiple enemies test
        float timePerEnemy = (timeEnd - timeStart) / quantity;
        for (int i = 0; i < quantity; i++) {
            float time = timeStart + (timePerEnemy * i);
            if (isBetween(time, elapsedTime, elapsedTime + delta)) {
                enemies.add(type);
            }
        }
        return enemies.toArray(new EnemyType[enemies.size()]);
    }

    private boolean isBetween(float value, float min, float max) {
        return value >= min && value < max;
    }

    @Override
    public String toString() {
        return "WaveUnit [timeStart=" + timeStart + ", timeEnd=" + timeEnd + ", type=" + type + ", quantity=" + quantity + "]";
    }

}
