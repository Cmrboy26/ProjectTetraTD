package net.cmr.rtd.game.achievements.custom;

import net.cmr.rtd.game.achievements.Achievement;
import net.cmr.util.Sprites.SpriteType;

public class HighLevelTowerAchievement extends Achievement<Long> {
    
    final static long targetLevel = 8;

    public HighLevelTowerAchievement() {
        super();
    }

    @Override
    public String getID() {
        return "high_level_tower";
    }

    @Override
    public boolean isAchievementComplete() {
        return getProgress() >= .98; // Not one because of rounding errors :) 
    }

    @Override
    public Long getDefaultValue() {
        return 0L;
    }

    @Override
    public String getReadableName() {
        return "Towermaxxing";
    }

    @Override
    public SpriteType getDisplayIcon() {
        return SpriteType.UPGRADE;
    }

    @Override
    public String getDescription() {
        return "Upgrade a tower to level "+targetLevel;
    }

    @Override
    public float getProgress() {
        return (float) getValue() / targetLevel;
    }
    
}
