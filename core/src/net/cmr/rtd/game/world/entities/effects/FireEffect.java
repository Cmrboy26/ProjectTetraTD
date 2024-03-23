package net.cmr.rtd.game.world.entities.effects;

import com.badlogic.gdx.graphics.Color;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.rtd.game.world.entities.EnemyEntity.DamageType;
import net.cmr.rtd.game.world.entities.effects.EntityEffects.EntityStat;
import net.cmr.rtd.game.world.particles.ParticleEffect;
import net.cmr.rtd.game.world.particles.SpreadEmitterEffect;
import net.cmr.util.Sprites.AnimationType;

public class FireEffect extends Effect {

    public FireEffect(EntityEffects target, float duration, float maxDuration, int level) {
        super(target, duration, maxDuration, level);
    }

    public FireEffect(EntityEffects target, float duration, int level) {
        super(target, duration, level);
    }

    transient float damageDelta = 0;

    @Override
    public void onInflict(UpdateData data) {
        if (data.isClient()) {
            ParticleEffect effect = getEffect();
            data.getScreen().addEffect(effect);
        }
    }

    @Override
    public void update(float delta, UpdateData data) {
        super.update(delta, data);

        damageDelta += delta;

        if (damageDelta >= 1) {
            damageDelta -= 1;
            Entity entity = getEntity();

            if (data.isClient()) {
                ParticleEffect effect = getEffect();
                data.getScreen().addEffect(effect);
            }

            if (entity instanceof EnemyEntity) {
                EnemyEntity enemy = (EnemyEntity) entity;
                enemy.damage(getLevel() * getLevel(), data, DamageType.FIRE);
            }
        }
    }

    private ParticleEffect getEffect() {
        return SpreadEmitterEffect.factory()
                .setEntity(getEntity())
                .setParticle(AnimationType.FIRE)
                .setDuration(1)
                .setEmissionRate(getLevel() + 1)
                .setScale(.25f)
                .setParticleLife(.5f)
                .setFollowEntity(true)
                .setAnimationSpeed(2f)
                .create();
    }

    @Override
    public float getStatModifier(EntityStat stat) {
        return NOTHING;
    }

    @Override
    public Color getDiscoloration() {
        return new Color(255f/255f, 80/255f, 80/255f, 1f);
    }
    
}
