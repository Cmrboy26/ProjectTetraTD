package net.cmr.rtd.game.achievements.custom;

import net.cmr.rtd.game.achievements.Achievement;
import net.cmr.util.Sprites.SpriteType;

public class FeedbackAchievement extends Achievement<Boolean> {

    @Override
    public String getID() {
        return "feedback_submitted";
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
        return "Feedbacked";
    }

    @Override
    public String getDescription() {
        return "Submit feedback in the main menu.";
    }

    @Override
    public SpriteType getDisplayIcon() {
        return SpriteType.COPY_ICON;
    }

}
