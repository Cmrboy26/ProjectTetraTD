package net.cmr.rtd.game.achievements;

import net.cmr.util.Sprites.SpriteType;

public class DiamondMinerAchievement extends Achievement<Integer> {

    public DiamondMinerAchievement() {
        super();
    }

    @Override
    public String getID() {
        return "diamond_miner";
    }

    @Override
    public boolean isAchievementComplete() {
        return getValue() >= 3;
    }

    @Override
    public Integer getDefaultValue() {
        return 2;
    }

    @Override
    public float getProgress() {
        return (float) getValue() / 3;
    }

    @Override
    public String getReadableName() {
        return "Diamond Miner";
    }

    @Override
    public String getDescription() {
        return "Collect 3 diamonds.";
    }

    @Override
    public SpriteType getDisplayIcon() {
        return SpriteType.DIAMOND;
    }
    
}
