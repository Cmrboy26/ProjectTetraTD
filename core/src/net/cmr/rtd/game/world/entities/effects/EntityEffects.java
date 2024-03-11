package net.cmr.rtd.game.world.entities.effects;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashSet;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.world.Entity;

/**
 * Handles the buffs and debuffs of a specific entity.
 * Since the game is "deterministic" (for our purposes), we do not need to worry about
 * client/server synchronization of effects.
 */
public class EntityEffects {
    
    private HashSet<Effect> effects;
    private HashSet<Class<? extends Effect>> immunity;
    private final Entity entity;

    public static enum EntityStat {
        SPEED,
    }

    public EntityEffects(Entity entity) {
        this.entity = entity;
        effects = new HashSet<Effect>();
        immunity = new HashSet<Class<? extends Effect>>();
    }

    public Effect getEffect(Class<? extends Effect> effectClass) {
        for (Effect effect : effects) {
            if (effect.getClass().equals(effectClass)) {
                return effect;
            }
        }
        return null;
    }

    public void addImmunity(Class<? extends Effect> effectClass) {
        immunity.add(effectClass);
    }

    public void addEffect(Effect effect) {
        for (Class<? extends Effect> immune : immunity) {
            if (immune.isInstance(effect)) {
                return;
            }
        }
        effects.add(effect);
    }

    public void update(float delta) {
        for (Effect effect : effects) {
            effect.update(delta);
        }
        effects.removeIf(Effect::effectFinished);
    }

    public void serialize(DataBuffer buffer) throws IOException {
        buffer.writeInt(effects.size());
        for (Effect effect : effects) {
            buffer.writeUTF(effect.getClass().getName());
            buffer.writeFloat(effect.getDuration());
            buffer.writeFloat(effect.getMaxDuration());
            buffer.writeInt(effect.getLevel());
        }
    }

    public static EntityEffects deserialize(Entity entity, DataInputStream input) throws IOException {
        EntityEffects effects = new EntityEffects(entity);
        int size = input.readInt();
        for (int i = 0; i < size; i++) {
            try {
                Class<?> effectClass = Class.forName(input.readUTF());
                float duration = input.readFloat();
                float maxDuration = input.readFloat();
                int level = input.readInt();
                Effect effect = (Effect) effectClass.getConstructor(EntityEffects.class, float.class, float.class, int.class).newInstance(effects, duration, maxDuration, level);
            } catch (Exception e) {
                throw new IOException("Failed to deserialize effect", e);
            }
        }
        return effects;
    }

    public float getStatMultiplier(EntityStat stat) {
        float value = 1;
        for (Effect effect : effects) {
            value *= effect.getStatModifier(stat);
        }
        return value;
    }
    
    public Color getDiscoloration() {
        // Color must start as black for screen blending to work properly
        Color color = Color.BLACK;
        for (Effect effect : effects) {
            Color discoloration = effect.getDiscoloration();
            if (discoloration != null) {
                // Screen blending of multiple effects
                color = new Color(
                    1f - (1f - color.r) * (1f - discoloration.r),
                    1f - (1f - color.g) * (1f - discoloration.g),
                    1f - (1f - color.b) * (1f - discoloration.b),
                    1f
                );
            }
        }
        // If the color is black, no effect was applied, so return no filter
        if (color.equals(Color.BLACK)) {
            return Color.WHITE;
        }
        return color;
    }

    public Entity getEntity() {
        return entity;
    }

}
