package net.cmr.rtd.game.endless;

import java.util.ArrayList;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.world.EnemyFactory.EnemyType;
import net.cmr.rtd.game.world.TeamData;
import net.cmr.rtd.game.world.entities.MiningTower;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.entities.towers.FireTower;
import net.cmr.rtd.waves.Wave;
import net.cmr.rtd.waves.WaveUnit;
import net.cmr.rtd.waves.WavesData;
import net.cmr.util.Log;

/**
 * Utility class that dynamically determines the difficulty of the next wave for endless mode.
 */
public class EndlessUtils {

    transient int stagnation = 0;
    transient float maximumDPS = 0;

    // The percentage of the enemy's health that the DPS must be to add the enemy to the wave
    /// Sinusoidal wave desmos graph: https://www.desmos.com/calculator/zov2sjdqvo
    float dpsThresholdScale = 1/4f; // The percentage of the enemy's health that the DPS must be to add the enemy to the wave
    // The "center" of the sinusoidal function
    float sinusoidalCenterValue = 1.03f;
    // Determines the period of the sinusoidal function (how many waves to go from maximum to maximum)
    float sinusoidalGameDifficultyPeriod = 7;
    // The distance from maximumScale to the peaks/trophs of the sinusoidal function
    float sinusoidalGameDifficultyAmplitude = 1/21f;
    // To not be considered stagnant, the player must increase their DPS by this percentage each wave
    float requiredDPSIncreasePercentageForStagnancy = 0.10f;
    // The number of waves that the player can be stagnant before penalties will be applied
    int stagnationThreshold = 2;
    // Stagnation difficulty increase per wave
    float stagnationDifficultyIncrease = 0.2f;

    public EndlessUtils(WavesData data) {
        this.requiredDPSIncreasePercentageForStagnancy = data.requiredDPSIncreasePercentageForStagnancy;
        this.dpsThresholdScale = data.dpsThresholdScale;
        this.sinusoidalCenterValue = data.sinusoidalCenterValue;
        this.sinusoidalGameDifficultyPeriod = data.sinusoidalGameDifficultyPeriod;
        this.sinusoidalGameDifficultyAmplitude = data.sinusoidalGameDifficultyAmplitude;
        this.stagnationThreshold = data.stagnationThreshold;
        this.stagnationDifficultyIncrease = data.stagnationDifficultyIncrease;
    }    

    public Wave generateDynamicWave(GameManager manager) {
        // Create an empty wave
        int currentWave = manager.getWorld().getWave();
        Wave wave = new Wave(getWaveDuration(currentWave));
        return wave;
    }

    transient float previousRoundDensity = 1; // enemies / tile. used for calculating approximateDPS with fire towers

    public void generateWaveUnits(Wave wave, GameManager gameManager) {
        ArrayList<TeamData> teamData = new ArrayList<>();
        for (TeamData team : gameManager.getTeams()) {
            if (team.getHealth() > 0 && gameManager.doesTeamHavePlayers(team.team)) {
                teamData.add(team);
            }
        }
        if (teamData.size() == 0) {
            // Every team is dead, so attempt to generate enemies for any team with players
            for (TeamData team : gameManager.getTeams()) {
                if (gameManager.doesTeamHavePlayers(team.team)) {
                    teamData.add(team);
                }
            }
        } 
        if (teamData.size() == 0) {
            // If there are no players on any of the teams, use every team
            teamData.addAll(gameManager.getTeams());
        }
        int teams = teamData.size();

        int currentWave = gameManager.getWorld().getWave();
        ArrayList<TowerEntity>[] teamTowers = new ArrayList[teams];
        for (int i = 0; i < teams; i++) {
            teamTowers[i] = gameManager.getTowers(teamData.get(i).team);
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

        boolean isStagnant = targetDPS <= maximumDPS * (1 + requiredDPSIncreasePercentageForStagnancy) && currentWave >= 2; // If DPS hasn't increased by stagnantIncreasePercentage, increase the stagnant counter
        if (isStagnant) {
            stagnation++;
            Log.info("Player was STAGNANT... current stagnant progress: "+stagnation);
        } else {
            stagnation--;
            if (stagnation < 0) {
                stagnation = 0;
            }
        }

        if (Float.isNaN(maximumDPS)) {
            maximumDPS = 0;
        }
        targetDPS = Math.max(maximumDPS, targetDPS);
        maximumDPS = Math.max(maximumDPS, targetDPS);

        float max = sinusoidalCenterValue;
        double sinusoidalFunctionOutput = -Math.cos(2*currentWave*Math.PI/sinusoidalGameDifficultyPeriod);
        double scale = (sinusoidalFunctionOutput + (max / sinusoidalGameDifficultyAmplitude) - 1d)*sinusoidalGameDifficultyAmplitude;
        if (stagnation >= stagnationThreshold) { // Punish players for not upgrading their towers
            Log.info("Player was STAGNANT for "+stagnation+" waves. Punishing...");
            scale += stagnationDifficultyIncrease * (stagnation - stagnationThreshold + 1);
        }
        // Don't fluctuate the difficulty during the beginning few waves
        if (currentWave <= 3) {
            scale = 1;
        }

        targetDPS *= scale;

        targetDPS = Math.max(2, targetDPS); // Minimum DPS (to prevent waves with no enemies)

        // TODO: FIX RESUMING ENDLESS MODE.
        int waveType = currentWave % 4; // 0 = default, 1 = small type enemy wave, 2 = large type enemy wave, 3 = default
        if (currentWave <= 3) {
            waveType = 0;
        }

        float targetHealthPoints = targetDPS*wave.getWaveTime();
        Log.info("Difficulty Scale: "+((scale-1)*100f)+"%, Target DPS: "+targetDPS+", Target HP: "+targetHealthPoints+", Wave Type: "+waveType);

        wave.addWaveUnit(new WaveUnit(0, EnemyType.BASIC_ONE, 0)); // To prevent a bug where this method is called repeatedly when no enemies are added

        // Generate wave to get density estimate
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
        // With the new density, generate the wave again
        wave.clearWaveUnits();
        if (waveType == 0 || waveType == 1) {
            defaultWave(targetDPS, targetHealthPoints, wave);
        }
        else if (waveType == 2) {
            smallWave(targetDPS, targetHealthPoints, wave);
        }
        else if (waveType == 3) {
            largeWave(targetDPS, targetHealthPoints, wave);
        }
        //fillWaveUnit(EnemyType.HEALER_ONE, 1000, wave); // Add a healer to the wave for debugging

        for (WaveUnit unit : wave.getWaveUnits()) {
            Log.info("- Added " + unit.getQuantity() + " " + unit.getType() + " enemies");
        }
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
        int maxSmallEnemies = (int) Math.max(25, Math.round(5f*Math.sqrt(targetDPS)));
        for (int i = 0; i < EnemyType.values().length; i++) {
            EnemyType type = EnemyType.values()[i];
            if (type == EnemyType.HEALER_ONE) {continue;}
            if (targetDPS >= type.getHealth()*dpsThresholdScale) {targetHealthPoints = fillWaveUnitMaximum(type, targetHealthPoints, wave, (int) (maxSmallEnemies/(1 + i*.5f)));}
        }
    } 

    private void largeWave(float targetDPS, float targetHealthPoints, Wave wave) {
        for (int i = EnemyType.values().length-1; i >= 0; i--) {
            EnemyType type = EnemyType.values()[i];
            if (type == EnemyType.HEALER_ONE) {continue;}
            if (targetDPS >= type.getHealth()*dpsThresholdScale) {targetHealthPoints = fillWaveUnit(type, targetHealthPoints, wave);}
        }
    }

    public float fillWaveUnit(EnemyType enemy, float targetHP, Wave wave) {
        int totalEnemies = (int) (targetHP / enemy.getHealth());
        WaveUnit unit = new WaveUnit(wave, enemy, totalEnemies);
        wave.addWaveUnit(unit);
        targetHP -= totalEnemies * enemy.getHealth();
        return targetHP;
    }

    public float fillWaveUnitMaximum(EnemyType enemy, float targetHP, Wave wave, int maxEnemies) {
        int totalEnemies = Math.min((int) (targetHP / enemy.getHealth()), maxEnemies);
        WaveUnit unit = new WaveUnit(wave, enemy, totalEnemies);
        wave.addWaveUnit(unit);
        targetHP -= totalEnemies * enemy.getHealth();
        return targetHP;
    }

    public float calculateApproximateDPS(ArrayList<TowerEntity> towers) {
        float totalDPS = 0;
        for (TowerEntity tower : towers) {
            if (tower instanceof MiningTower) {
                continue;
            }
            if (tower instanceof FireTower) {
                totalDPS += tower.getDamage(false);
                totalDPS += ((FireTower)tower).calculateApproximateFireballDPS(Math.min(10, previousRoundDensity));
                continue;
            }
            if (tower.getAttackSpeed() == 0) {
                continue;
            }
            totalDPS += tower.getDamage(false) / tower.getAttackSpeed();
        }
        return totalDPS;
    }

}
