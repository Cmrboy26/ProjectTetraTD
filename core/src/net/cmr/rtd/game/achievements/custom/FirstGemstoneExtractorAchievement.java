package net.cmr.rtd.game.achievements.custom;

import net.cmr.rtd.game.achievements.Achievement;

public class FirstGemstoneExtractorAchievement extends Achievement<Boolean> {

    public FirstGemstoneExtractorAchievement() {
        super();
    }

    @Override
    public String getID() {
        return "first_gemstone_extractor";
    }

    @Override
    public boolean isAchievementComplete() {
        return getValue();
    }

    @Override
    public Boolean getDefaultValue() {
        return false;
    }

    @Override
    public String getReadableName() {
        return "Gemstone Extractor";
    }

    @Override
    public String getDescription() {
        return "Construct a Gemstone Extractor for the first time";
    }
    
}
