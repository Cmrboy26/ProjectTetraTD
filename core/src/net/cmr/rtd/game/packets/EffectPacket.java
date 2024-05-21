package net.cmr.rtd.game.packets;

import java.util.UUID;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.effects.Effect;
import net.cmr.rtd.game.world.entities.effects.EntityEffects;
import net.cmr.rtd.screen.GameScreen;

public class EffectPacket extends Packet {
    
    UUID entityUUID;
    String effectClass;
    int effectLevel;
    int duration;
    int maxDuration;

    public EffectPacket() { }
    public EffectPacket(UUID entityUUID, Effect effect) {
        this.entityUUID = entityUUID;
        this.effectClass = effect.getClass().getName();
        this.effectLevel = effect.getLevel();
        if (effect.effectFinished()) {
            effectLevel = -1;
        }
        this.duration = (int) effect.getDuration();
        this.maxDuration = (int) effect.getMaxDuration();
    }
    @Override
    public Object[] packetVariables() {
        return toPacketVariables(entityUUID, effectClass, effectLevel);
    }

    public boolean removeEffect() {
        return effectLevel == -1;
    }

    @SuppressWarnings("unchecked")
    public void apply(GameScreen screen) {
        Entity entity = screen.getEntity(entityUUID);
        if (entity == null) {
            return;
        }

        Class<? extends Effect> effectClazz = null;
        try {
            effectClazz = (Class<? extends Effect>) Class.forName(effectClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        if (effectClazz == null) {
            return;
        }
        if (removeEffect()) {
            entity.getEffects().removeEffect(null, effectClazz);
            return;
        }

        Effect effect = null;
        try {
            effect = (Effect) effectClazz.getConstructor(UpdateData.class, EntityEffects.class, float.class, float.class, int.class).newInstance(null, entity.getEffects(), duration, maxDuration, effectLevel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
