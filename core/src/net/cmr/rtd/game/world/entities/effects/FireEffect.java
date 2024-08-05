package net.cmr.rtd.game.world.entities.effects;

import com.badlogic.gdx.graphics.Color;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.rtd.game.world.entities.EnemyEntity.DamageType;
import net.cmr.rtd.game.world.entities.effects.EntityEffects.EntityStat;
import net.cmr.rtd.game.world.particles.ParticleCatalog;
import net.cmr.rtd.game.world.particles.ParticleEffect;
import net.cmr.rtd.game.world.particles.SpreadEmitterEffect;
import net.cmr.util.Sprites.AnimationType;

public class FireEffect extends Effect {

    public FireEffect(UpdateData data, EntityEffects target, float duration, float maxDuration, int level) {
        super(data, target, duration, maxDuration, level);
    }

    public FireEffect(UpdateData data, EntityEffects target, float duration, int level) {
        super(data, target, duration, level);
    }

    transient float damageDelta = 0;

    @Override
    public void onInflict(UpdateData data) {
        if (data.isClient()) {
            ParticleEffect effect = getEffect();
            data.getScreen().addEffect(effect);
        }
    }

    public static int getDPS(int level) {
        return level * level;
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
                enemy.damage(getDPS(level), data, DamageType.FIRE);
            }
        }
    }

    private ParticleEffect getEffect() {
        return ParticleCatalog.fireEffect(getEntity());
    }

    @Override
    public float getStatModifier(EntityStat stat) {
        return NOTHING;
    }

    @Override
    public Color getDiscoloration() {
        return new Color(255f/255f, 80/255f, 80/255f, 1f).mul(2f);
    }
    
}
