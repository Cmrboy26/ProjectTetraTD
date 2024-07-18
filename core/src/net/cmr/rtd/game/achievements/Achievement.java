package net.cmr.rtd.game.achievements;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import net.cmr.rtd.ProjectTetraTD;
import net.cmr.rtd.shader.ShaderManager.CustomShader;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.SpriteType;

public abstract class Achievement<T> {

    private static HashMap<String, Class<? extends Achievement<?>>> achievementRegistry;
    private static ArrayList<Class<? extends Achievement<?>>> achievementRegisterOrder = new ArrayList<>();

    static {
        achievementRegistry = new HashMap<>();
        initialize(TutorialCompleteAchievement.class);
        initialize(DiamondMinerAchievement.class);
    }
    
    private T value;

    public static HashMap<String, Class<? extends Achievement<?>>> getAchievementRegistry() {
        return achievementRegistry;
    }

    public static ArrayList<Class<? extends Achievement<?>>> getAchievementRegisterOrder() {
        return achievementRegisterOrder;
    }

    public static Class<? extends Achievement<?>> getAchievement(String id) {
        return achievementRegistry.get(id);
    }

    public static Achievement<?> createAchievementInstance(Class<? extends Achievement<?>> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected static void initialize(Class<? extends Achievement<?>> clazz) {
        achievementRegistry.put(createAchievementInstance(clazz).getID(), clazz);
        achievementRegisterOrder.add(clazz);
    }

    protected Achievement() {
        reset();
    }

    public abstract String getID();
    public abstract boolean isAchievementComplete();
    public abstract T getDefaultValue();
    
    /**
     * This should only be overridden if the achievement is progress-based.
     * @return a float between 0 and 1 representing the progress of the achievement.
     * If the achievement is not progress-based, return -1.
     */
    public float getProgress() {
        return -1;
    }

    public SpriteType getDisplayIcon() {
        return SpriteType.TROPHY;
    }

    public String getReadableName() {
        return "Generic Achievement";
    }

    /*
     * Include punctiation in the description.
     */
    public String getDescription() {
        return "Default achievement description :(";
    }

    public void reset() {
        setValue(getDefaultValue());
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public Class<T> getValueType() {
        return (Class<T>) value.getClass();
    }

    @Override
    public int hashCode() {
        return getID().hashCode();
    }

    public static class AchievementDisplay extends Table {
        
        public AchievementDisplay(ProjectTetraTD game, Achievement<?> achievement) {
            super(Sprites.skin());
            setOrigin(Align.top);
            setScale(.7f);

            Table textGroup = new Table();
            textGroup.defaults().space(5).center();
            Label name = new Label(achievement.getReadableName(), Sprites.skin(), "small");
            textGroup.add(name).row();
            Label description = new Label(achievement.getDescription(), Sprites.skin(), "small");
            description.setFontScale(.4f);
            description.setAlignment(Align.center);
            description.setWrap(true);
            textGroup.add(description).prefWidth(200).row();

            boolean isProgressBased = achievement.getProgress() != -1;
            if (isProgressBased) {
                ProgressBar progressBar = new ProgressBar(0, 1, .01f, false, Sprites.skin());
                progressBar.setValue(achievement.getProgress());
                progressBar.setDisabled(true);
                textGroup.add(progressBar).width(150).row();
            }

            Image icon = new Image(Sprites.sprite(achievement.getDisplayIcon())) {
                @Override
                public void draw(Batch batch, float parentAlpha) {
                    boolean grayscale = !achievement.isAchievementComplete();
                    if (grayscale) {
                        game.enableShader(batch, CustomShader.GRAYSCALE);
                    }
                    super.draw(batch, parentAlpha);
                    if (grayscale) {
                        game.disableShader(batch);
                    }
                }
            };
            icon.setColor(Color.GRAY);
            add(icon).size(32);
            add(textGroup).expand().fill();
        }
        
    }

}
