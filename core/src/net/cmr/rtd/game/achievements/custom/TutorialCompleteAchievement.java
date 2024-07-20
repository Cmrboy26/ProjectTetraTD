package net.cmr.rtd.game.achievements.custom;

import net.cmr.rtd.game.achievements.Achievement;

public class TutorialCompleteAchievement extends Achievement<Boolean> {

    public TutorialCompleteAchievement() {
        super();
    }

    @Override
    public String getID() {
        return "tutorial_completed";
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
        return "Beginner";
    }

    @Override
    public String getDescription() {
        return "Complete the tutorial.";
    }
    
}
