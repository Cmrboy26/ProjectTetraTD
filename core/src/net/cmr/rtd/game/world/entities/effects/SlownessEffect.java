package net.cmr.rtd.game.world.entities.effects;

import com.badlogic.gdx.graphics.Color;

import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.effects.EntityEffects.EntityStat;
import net.cmr.rtd.game.world.particles.ParticleCatalog;
import net.cmr.rtd.game.world.particles.ParticleEffect;
import net.cmr.rtd.game.world.particles.SpreadEmitterEffect;
import net.cmr.util.Sprites.SpriteType;

public class SlownessEffect extends Effect {

    public SlownessEffect(UpdateData data, EntityEffects target, float duration, int level) {
        super(data, target, duration, level);
    }

    public SlownessEffect(UpdateData data, EntityEffects target, float duration, float maxDuration, int level) {
        super(data, target, duration, maxDuration, level);
    }

    @Override
    public void onInflict(UpdateData data) {
        ParticleEffect effect = ParticleCatalog.frozenEffect(getEntity());
        if (data.isClient()) {
            data.getScreen().addEffect(effect);
        }
    }

    public static float getSlowdownMultiplier(int level) {
        // 1-.075x if < 8, else 1 - (2 * arctan(.171x) / pi)
        // return 1f - .45f * (float) Math.log10(level + 1);
        if (level < 8) {
            return 1f - .075f * level;
        } else {
            return 1f - (2f * (float) Math.atan(.171f * level) / (float) Math.PI);
        }
    }

    @Override
    public float getStatModifier(EntityStat stat) {
        //float m = .9f;
        //float b = .4f;

        if (stat == EntityStat.SPEED) {
            //return (1.5f/(getLevel()+1f));
            return getSlowdownMultiplier(getLevel());
        }
        return NOTHING;
    }

    @Override
    public Color getDiscoloration() {
        return new Color(195f/255f, 200f/255f, 255f/255f, 1f);
    }
    
}
