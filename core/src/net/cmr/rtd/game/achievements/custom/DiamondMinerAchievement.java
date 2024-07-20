package net.cmr.rtd.game.achievements.custom;

import net.cmr.rtd.game.achievements.Achievement;
import net.cmr.util.Sprites.SpriteType;

public class DiamondMinerAchievement extends Achievement<Integer> {

    int amount = 2;

    public DiamondMinerAchievement() {
        super();
    }

    @Override
    public String getID() {
        return "diamond_miner";
    }

    @Override
    public boolean isAchievementComplete() {
        return getValue() >= amount;
    }

    @Override
    public Integer getDefaultValue() {
        return 0;
    }

    @Override
    public float getProgress() {
        return (float) getValue() / amount;
    }

    @Override
    public String getReadableName() {
        return "Diamond Miner";
    }

    @Override
    public String getDescription() {
        return "Collect " + amount + " diamonds through gemstone mining.";
    }

    @Override
    public SpriteType getDisplayIcon() {
        return SpriteType.DIAMOND;
    }
    
}
