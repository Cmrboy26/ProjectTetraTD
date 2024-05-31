package net.cmr.rtd.game.endless;

import java.util.ArrayList;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.world.EnemyFactory.EnemyType;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.entities.towers.FireTower;
import net.cmr.rtd.waves.Wave;
import net.cmr.rtd.waves.WaveUnit;
import net.cmr.util.Log;

/**
 * Utility class that dynamically determines the difficulty of the next wave for endless mode.
 */
public class EndlessUtils {

    int stagnation = 0;
    float maximumDPS = 0;
    float dpsThresholdScale = 1/4f;

    // Determines how much DPS is required to defeat the generated wave at the maximum of the sinusoidal function 
    float maximumScale = 1.01f;
    // Determines the period of the sinusoidal function (how many waves to go from maximum to maximum)
    float sinusoidalPeriod = 7;
    // The distance from maximumScale to the peaks/trophs of the sinusoidal function
    float sinusoidalFunctionAmplitude = 1/21f;

    final int STAGNATION_THRESHOLD = 2;

    public EndlessUtils() {
        
    }    

    public Wave generateDynamicWave(GameManager manager) {
        // Create an empty wave
        int currentWave = manager.getWorld().getWave();
        Wave wave = new Wave(getWaveDuration(currentWave));
        return wave;
    }

    transient float previousRoundDensity = 1; // enemies / tile. used for calculating approximateDPS with fire towers

    public void generateWaveUnits(Wave wave, GameManager gameManager) {
        int teams = gameManager.getTeams().size();
        int currentWave = gameManager.getWorld().getWave();
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

        boolean isStagnant = targetDPS <= maximumDPS && currentWave >= 2; // If DPS hasn't increased, increase the stagnant counter
        if (isStagnant) {
            Log.info("Player was STAGNANT... current stagnant progress: "+stagnation);
            stagnation++;
        } else {
            stagnation--;
            if (stagnation < 0) {
                stagnation = 0;
            }
        }

        targetDPS = Math.max(maximumDPS, targetDPS);
        maximumDPS = Math.max(maximumDPS, targetDPS);

        float max = maximumScale;
        double sinusoidalFunctionOutput = -Math.cos(2*currentWave*Math.PI/sinusoidalPeriod);
        double scale = (sinusoidalFunctionOutput + (max / sinusoidalFunctionAmplitude) - 1d)*sinusoidalFunctionAmplitude;
        if (stagnation >= STAGNATION_THRESHOLD) { // Punish players for not upgrading their towers
            Log.info("Player was STAGNANT for "+stagnation+" waves");
            scale += 0.2 * (stagnation - STAGNATION_THRESHOLD + 1);
        }
        // Don't fluctuate the difficulty during the beginning few waves
        if (currentWave <= 3) {
            scale = 1;
        }

        targetDPS *= scale;

        targetDPS = Math.max(2, targetDPS); // Minimum DPS (to prevent waves with no enemies)

        // TODO: FIX RESUMING ENDLESS MODE.
        // TODO: Add another wave type that have half of one enemy and half of another enemy
        int waveType = currentWave % 4; // 0 = default, 1 = small type enemy wave, 2 = large type enemy wave, 3 = default
        if (currentWave <= 3) {
            waveType = 0;
        }

        float targetHealthPoints = targetDPS*wave.getWaveTime();
        Log.info("Difficulty Scale: "+((scale-1)*100f)+"%, Target DPS: "+targetDPS+", Target HP: "+targetHealthPoints+", Wave Type: "+waveType);

        wave.addWaveUnit(new WaveUnit(0, EnemyType.BASIC_ONE, 0)); // To prevent a bug where this method is called repeatedly when no enemies are added

        if (waveType == 0 || waveType == 1) {
            defaultWave(targetDPS, targetHealthPoints, wave);
        }
        else if (waveType == 2) {
            smallWave(targetDPS, targetHealthPoints, wave);
        }
        else if (waveType == 3) {
            largeWave(targetDPS, targetHealthPoints, wave);
        }
        calculateEnemyDensity(wave);
    }

    /**
     * Calculates the enemy density of the wave to be used for the next wave.
     * @param wave the wave to calculate the enemy density of
     */
    public void calculateEnemyDensity(Wave wave) {
        float density = 0;
        for (WaveUnit unit : wave.getWaveUnits()) {
            density += unit.getQuantity();
        }
        density /= wave.getWaveTime();
        Log.info("Enemy Density: "+density+" enemies/tile");
        this.previousRoundDensity = density;
    }

    public float getWaveDuration(int waveNumber) {
        float period = 6;

        double sinusoidalFunctionOutput = -Math.cos(2*waveNumber*Math.PI/period);
        float length = 30;
        boolean isMinimum = sinusoidalFunctionOutput == -1;
        boolean isMaximum = sinusoidalFunctionOutput == 1;

        if (isMaximum) {
            length = 20;
        }
        if (isMinimum) {
            length = 45;
        }
        return length;
    }

    private void smallWave(float targetDPS, float targetHealthPoints, Wave wave) {
        int maxSmallEnemies = (int) Math.max(25, Math.round(5f*Math.sqrt(targetDPS)));
        for (int i = 0; i < EnemyType.values().length; i++) {
            EnemyType type = EnemyType.values()[i];
            if (targetDPS >= type.getHealth()*dpsThresholdScale) {targetHealthPoints = fillWaveUnitMaximum(type, targetHealthPoints, wave, (int) (maxSmallEnemies/(1 + i*.5f)));}
        }
    }

    private void defaultWave(float targetDPS, float targetHealthPoints, Wave wave) {
        smallWave(targetDPS, targetHealthPoints, wave);
    } 

    private void largeWave(float targetDPS, float targetHealthPoints, Wave wave) {
        for (int i = EnemyType.values().length-1; i >= 0; i--) {
            EnemyType type = EnemyType.values()[i];
            if (targetDPS >= type.getHealth()*dpsThresholdScale) {targetHealthPoints = fillWaveUnit(type, targetHealthPoints, wave);}
        }
    }

    public float fillWaveUnit(EnemyType enemy, float targetHP, Wave wave) {
        int totalEnemies = (int) (targetHP / enemy.getHealth());
        WaveUnit unit = new WaveUnit(wave, enemy, totalEnemies);
        wave.addWaveUnit(unit);
        targetHP -= totalEnemies * enemy.getHealth();
        Log.info("- Added " + totalEnemies + " " + enemy + " enemies");
        return targetHP;
    }

    public float fillWaveUnitMaximum(EnemyType enemy, float targetHP, Wave wave, int maxEnemies) {
        int totalEnemies = Math.min((int) (targetHP / enemy.getHealth()), maxEnemies);
        WaveUnit unit = new WaveUnit(wave, enemy, totalEnemies);
        wave.addWaveUnit(unit);
        targetHP -= totalEnemies * enemy.getHealth();
        Log.info("- Added " + totalEnemies + " " + enemy + " enemies");
        return targetHP;
    }

    public float calculateApproximateDPS(ArrayList<TowerEntity> towers) {
        float totalDPS = 0;
        for (TowerEntity tower : towers) {
            if (tower instanceof FireTower) {
                totalDPS += tower.getDisplayDamage();
                totalDPS += ((FireTower)tower).calculateApproximateFireballDPS(Math.min(10, previousRoundDensity));
                continue;
            }
            totalDPS += tower.getDisplayDamage() / tower.getAttackSpeed();
        }
        return totalDPS;
    }

}
