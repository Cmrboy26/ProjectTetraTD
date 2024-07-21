package net.cmr.rtd.game.achievements.custom;

import java.util.HashSet;

import net.cmr.rtd.game.achievements.Achievement;
import net.cmr.rtd.game.world.EnemyFactory.EnemyType;
import net.cmr.util.Sprites.SpriteType;

public class FullBestiaryAchievement extends Achievement<String> {
    
    private static String completedString = "";

    static {
        for (int i = 0; i < EnemyType.values().length; i++) {
            completedString += i + "_";
        }
    }

    public FullBestiaryAchievement() {
        super();
    }

    @Override
    public String getID() {
        return "full_bestiary";
    }

    @Override
    public boolean isAchievementComplete() {
        return getProgress() >= 1;
    }

    @Override
    public String getDefaultValue() {
        return "";
    }

    @Override
    public String getReadableName() {
        return "Bestiary Master";
    }

    @Override
    public SpriteType getDisplayIcon() {
        return SpriteType.HEART;
    }

    @Override
    public String getDescription() {
        return "Encounter every type of enemy.";
    }

    @Override
    public float getProgress() {
        int fullLength = completedString.length();
        fullLength /= 2;
        int currentLength = getValue().length();
        currentLength /= 2;
        return (float) currentLength / fullLength;
    }

    public static String addEntity(String valueString, EnemyType type) {
        if (type == null) {
            System.out.println("WARNING: Attempted to add null enemy type to bestiary");
            return valueString;
        }
        int index = type.ordinal();
        String[] values = valueString.split("_");
        HashSet<Integer> encountered = new HashSet<>();
        for (String value : values) {
            if (value.length() == 0) continue;
            encountered.add(Integer.parseInt(value));
        }
        // If the enemy type has already been encountered, return the original value
        if (encountered.contains(index)) {
            return valueString;
        }

        // Add the new enemy type to the list
        encountered.add(index);
        String newValue = "";
        for (int i = 0; i < EnemyType.values().length; i++) {
            if (encountered.contains(i)) {
                newValue += i + "_";
                continue;
            }
        }
        return newValue;

    }

}
