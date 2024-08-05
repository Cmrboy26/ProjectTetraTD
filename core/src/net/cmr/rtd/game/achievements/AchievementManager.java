package net.cmr.rtd.game.achievements;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.cmr.rtd.ProjectTetraTD;
import net.cmr.util.Log;

public class AchievementManager {
    
    private static AchievementManager instance;
    private static Object lock = new Object();

    static {
        // Load achievements
        synchronized (lock) {
            instance = new AchievementManager();
            instance.loadAchievements();
        }
    }

    public static AchievementManager getInstance() {
        synchronized (lock) {
            return instance;
        }
    }

    final ProjectTetraTD game;
    private HashMap<Class<? extends Achievement<?>>, Achievement<?>> achievements;
    private boolean dirty = false; // Whether achievements need to be saved to file

    private AchievementManager() {
        game = ProjectTetraTD.getInstance(ProjectTetraTD.class);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void loadAchievements() {
        // Load achievements from file
        achievements = new HashMap<>();
        // Fill achievements with default state of all achievements
        for (Class<? extends Achievement<?>> clazz : Achievement.getAchievementRegistry().values()) {
            Achievement<?> achievement = Achievement.createAchievementInstance(clazz);
            achievements.put(clazz, achievement);
        }

        Map<String, Object> achievementMap = ProjectTetraTD.readUserData("achievements", Map.class, new HashMap<>());
        if (achievementMap == null) {
            Log.info("No achievement data found.");
            saveAchievements();
            return;
        }
        for (Map.Entry<String, Object> entry : achievementMap.entrySet()) {
            String id = entry.getKey();
            Object value = entry.getValue();
            Class<? extends Achievement<?>> clazz = Achievement.getAchievement(id);
            if (clazz != null) {
                Achievement achievement = Achievement.createAchievementInstance(clazz);
                if (achievement.getValueType().isInstance(value)) {
                    achievement.setValue(value);
                    achievements.put((Class<? extends Achievement<?>>) achievement.getClass(), achievement);
                }
            }
        }

        Log.info("Loaded " + achievements.size() + " achievements");
        for (Entry<Class<? extends Achievement<?>>, Achievement<?>> entry : achievements.entrySet()) {
            Log.info(entry.getValue().getID() + ": " + entry.getValue().getValue());
        }
    }

    public static void save() {
        getInstance().saveAchievements();
    }

    private void saveAchievements() {
        // Save achievements to file
        if (dirty) {
            Map<String, Object> achievementsMap = new HashMap<>();
            for (Achievement<?> achievement : achievements.values()) {
                achievementsMap.put(achievement.getID(), achievement.getValue());
            }
            ProjectTetraTD.writeUserData("achievements", achievementsMap);
            dirty = false;
        }
    }

    public Map<Class<? extends Achievement<?>>, Achievement<?>> getAchievements() {
        return achievements;
    }


    public static <T> void setValue(Class<? extends Achievement<T>> clazz, T value) {
        getInstance().setAchievementValue(clazz, value);
    }
    public static <T> T getValue(Class<? extends Achievement<T>> clazz) {
        return getInstance().getAchievementValue(clazz);
    }
    @SuppressWarnings("unchecked")
    public static <T> void addValue(Class<? extends Achievement<T>> clazz, T value) {
        AchievementManager manager = getInstance();
        Class<T> valueType = (Class<T>) manager.getAchievementValue(clazz).getClass();
        if (valueType == Integer.class) {
            Integer valueInteger = (Integer) manager.getAchievementValue(clazz);
            Integer valueToAdd = (Integer) value;
            manager.setAchievementValue(clazz, (T) (Integer) (valueInteger + valueToAdd));
        } else if (valueType == Float.class) {
            Float valueFloat = (Float) manager.getAchievementValue(clazz);
            Float valueToAdd = (Float) value;
            manager.setAchievementValue(clazz, (T) (Float) (valueFloat + valueToAdd));
        } else if (valueType == Double.class) {
            Double valueDouble = (Double) manager.getAchievementValue(clazz);
            Double valueToAdd = (Double) value;
            manager.setAchievementValue(clazz, (T) (Double) (valueDouble + valueToAdd));
        } else if (valueType == Long.class) {
            Long valueLong = (Long) manager.getAchievementValue(clazz);
            Long valueToAdd = (Long) value;
            manager.setAchievementValue(clazz, (T) (Long) (valueLong + valueToAdd));
        } else {
            throw new IllegalArgumentException("Unsupported value type for adding: " + valueType);
        }
    }

    public <T> void setAchievementValue(Class<? extends Achievement<T>> clazz, T value) {
        @SuppressWarnings("unchecked")
        Achievement<T> achievement = (Achievement<T>) achievements.get(clazz);
        boolean preiouslyIncomplete = achievement != null && !achievement.isAchievementComplete();
        if (achievement.getValue().equals(value)) {
            return;
        }
        if (achievement != null) {
            achievement.setValue(value);
            dirty = true;
        }
        if (achievement.isAchievementComplete() && preiouslyIncomplete) {
            game.onAchievementComplete(achievement);
        }
        saveAchievements();
    }

    public <T> T getAchievementValue(Class<? extends Achievement<T>> clazz) {
        @SuppressWarnings("unchecked")
        Achievement<T> achievement = (Achievement<T>) achievements.get(clazz);
        if (achievement != null) {
            return achievement.getValue();
        }
        return null;
    }

}
