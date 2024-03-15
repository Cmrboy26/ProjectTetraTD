package net.cmr.rtd.game.world.entities.effects;

import com.badlogic.gdx.graphics.Color;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.rtd.game.world.entities.effects.EntityEffects.EntityStat;

public class FireEffect extends Effect {

    public FireEffect(EntityEffects target, float duration, float maxDuration, int level) {
        super(target, duration, maxDuration, level);
    }

    public FireEffect(EntityEffects target, float duration, int level) {
        super(target, duration, level);
    }

    transient float damageDelta = 0;

    @Override
    public void update(float delta) {
        super.update(delta);

        damageDelta += delta;
        if (damageDelta >= 1) {
            damageDelta -= 1;
            Entity entity = getEntity();
            if (entity instanceof EnemyEntity) {
                EnemyEntity enemy = (EnemyEntity) entity;
                enemy.damage(getLevel() * getLevel());
            }
        }
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
