package net.cmr.rtd.game.world.entities.effects;

import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.effects.EntityEffects.EntityStat;

public class StunEffect extends Effect {
    
    public StunEffect(UpdateData data, EntityEffects target, float duration, int level) {
        super(data, target, duration, 1);
    }

    public StunEffect(UpdateData data, EntityEffects target, float duration, float maxDuration, int level) {
        super(data, target, duration, maxDuration, 1);
    }

    @Override
    public float getStatModifier(EntityStat stat) {
        if (stat == EntityStat.SPEED) {
            return 0;
        }
        return NOTHING;
    }
    
}
