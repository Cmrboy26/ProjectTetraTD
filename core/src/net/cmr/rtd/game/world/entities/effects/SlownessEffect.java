package net.cmr.rtd.game.world.entities.effects;

import com.badlogic.gdx.graphics.Color;

import net.cmr.rtd.game.world.entities.effects.EntityEffects.EntityStat;

public class SlownessEffect extends Effect {

    public SlownessEffect(EntityEffects target, float duration, int level) {
        super(target, duration, level);
    }

    public SlownessEffect(EntityEffects target, float duration, float maxDuration, int level) {
        super(target, duration, maxDuration, level);
    }

    @Override
    public float getStatModifier(EntityStat stat) {
        if (stat == EntityStat.SPEED) {
            return (1.5f/(getLevel()+1f));
        }
        return NOTHING;
    }

    @Override
    public Color getDiscoloration() {
        return new Color(195f/255f, 200f/255f, 255f/255f, 1f);
    }
    
}
