package net.cmr.rtd.game.endless;

import java.util.ArrayList;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.world.EnemyFactory.EnemyType;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.waves.Wave;
import net.cmr.rtd.waves.WaveUnit;

/**
 * Utility class that dynamically determines the difficulty of the next wave for endless mode.
 */
public class EndlessUtils {
    
    public static Wave generateDynamicWave(GameManager gameManager) {
        int teams = gameManager.getTeams().size();
        ArrayList<TowerEntity>[] teamTowers = new ArrayList[teams];
        for (int i = 0; i < teams; i++) {
            teamTowers[i] = gameManager.getTowers(i);
        }
        
        float[] dps = new float[teams];
        for (int i = 0; i < teams; i++) {
            dps[i] = calculateApproximateDPS(teamTowers[i]);
        }
        
        // Find the average DPS of all teams
        float targetDPS = 0;
        for (int i = 0; i < teams; i++) {
            targetDPS += dps[i];
        }
        targetDPS /= teams;

        Wave wave = new Wave(30);
        wave.setAdditionalPrepTime(1);

        // TODO: Better balance out the enemy distributions
        // ALSO, make waves have certain quirks, like a lot of small enemies, increased health?, giant clusters of enemies, etc.
        // FIX RESUMING ENDLESS MODE.

        float targetHealthPoints = targetDPS*wave.getWaveTime();
        System.out.println("Target health points: " + targetHealthPoints);
        if (targetHealthPoints > 125 && targetDPS > 33) {
            int totalEnemies = (int) (targetHealthPoints / 125);
            WaveUnit unit = new WaveUnit(wave, EnemyType.BASIC_FOUR, totalEnemies);
            wave.addWaveUnit(unit);
            targetHealthPoints -= totalEnemies * 125;
            System.out.println("Added " + totalEnemies + " BASIC_FOUR enemies");
        }
        if (targetHealthPoints > 25 && targetDPS > 10) {
            int totalEnemies = (int) (targetHealthPoints / 25);
            WaveUnit unit = new WaveUnit(wave, EnemyType.BASIC_THREE, totalEnemies);
            wave.addWaveUnit(unit);
            targetHealthPoints -= totalEnemies * 25;
            System.out.println("Added " + totalEnemies + " BASIC_THREE enemies");
        }
        if (targetHealthPoints > 5 && targetDPS > 2.25f) {
            int totalEnemies = (int) (targetHealthPoints / 10);
            WaveUnit unit = new WaveUnit(wave, EnemyType.BASIC_TWO, totalEnemies);
            wave.addWaveUnit(unit);
            targetHealthPoints -= totalEnemies * 5;
            System.out.println("Added " + totalEnemies + " BASIC_TWO enemies");
        }
        if (targetHealthPoints > 1) {
            int totalEnemies = (int) (targetHealthPoints / 4);
            WaveUnit unit = new WaveUnit(wave, EnemyType.BASIC_ONE, totalEnemies);
            wave.addWaveUnit(unit);
            targetHealthPoints -= totalEnemies;
            System.out.println("Added " + totalEnemies + " BASIC_ONE enemies");
        }

        return wave;
    }

    public static float calculateApproximateDPS(ArrayList<TowerEntity> towers) {
        float totalDPS = 0;
        for (TowerEntity tower : towers) {
            totalDPS += tower.getDisplayDamage() / tower.getAttackSpeed();
        }
        return totalDPS;
    }

}
