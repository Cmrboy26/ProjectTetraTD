package net.cmr.rtd.game.world.entities.effects;

import com.badlogic.gdx.graphics.Color;

import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.effects.EntityEffects.EntityStat;
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
        ParticleEffect effect = SpreadEmitterEffect.factory()
                .setEntity(getEntity())
                .setParticle(SpriteType.FROZEN)
                .setDuration(1)
                .setEmissionRate(3)
                .setScale(.20f)
                .setParticleLife(.5f)
                .setFollowEntity(true)
                .setAnimationSpeed(2f)
                .create();
        if (data.isClient()) {
            data.getScreen().addEffect(effect);
        }
    }

    public static float getSlowdownMultiplier(int level) {
        return 1f - .45f * (float) Math.log10(level + 1);
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
