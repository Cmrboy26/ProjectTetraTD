package net.cmr.rtd.game.world.entities.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Null;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.effects.EntityEffects.EntityStat;

/**
 * Alters the behavior of an entity.
 */
public abstract class Effect {
    
    EntityEffects target;
    float duration;
    int level;
    final float maxDuration;

    protected final float NOTHING = 1;

    public Effect(EntityEffects target, float duration, int level) {
        this(target, duration, duration, level);
    }

    public Effect(EntityEffects target, float duration, float maxDuration, int level) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be greater than 0");
        }
        if (level < 1) {
            throw new IllegalArgumentException("Level must be greater than 0");
        }
        this.target = target;
        this.duration = duration;
        this.maxDuration = maxDuration;
        this.level = level;
        this.target.addEffect(this);
    }

    public void onInflict(UpdateData data) {

    }

    public void update(float delta, UpdateData data) {
        if (duration == getMaxDuration()) {
            onInflict(data);
        }
        duration -= delta;
    }

    public boolean effectFinished() {
        return duration <= 0;
    }

    public float getDuration() {
        return duration;
    }
    public float getMaxDuration() {
        return maxDuration;
    }
    public Entity getEntity() {
        return target.getEntity();
    }
    public int getLevel() {
        return level;
    }
    public EntityEffects getTarget() {
        return target;
    }

    /**
     * Returns a multiplier for the given stat.
     * If the stat is not affected by this effect, 1 is returned.
     * @param stat the stat to modify
     * @return the multiplier for the stat
     */
    public abstract float getStatModifier(EntityStat stat);

    /**
     * An optional method to apply a shade of color to the entity while the effect is active.
     * If null is returned, no color is applied.
     * @return the color to apply to the entity
     */
    public @Null Color getDiscoloration() {
        return null;
    }

    @Override
    public int hashCode() {
        return target.hashCode() + getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Effect)) {
            return false;
        }
        Effect effect = (Effect) obj;
        return target.equals(effect.target) && getClass().equals(effect.getClass());
    }

    public String toString() {
        return getClass().getSimpleName() + "[" + duration + "s / " + maxDuration + "s, level=" + level + "]";
    }

}
