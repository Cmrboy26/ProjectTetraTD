package net.cmr.rtd.game.packets;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.particles.ParticleCatalog;
import net.cmr.rtd.game.world.particles.ParticleEffect;

public class ParticlePacket extends Packet {
    
    byte[] particleData;

    public ParticlePacket() { super(); }
    public ParticlePacket(ParticleEffect effect) throws IOException {
        super();
        DataBuffer buffer = new DataBuffer();
        effect.serialize(buffer);
        particleData = buffer.getBuffer();
        buffer.close();
    }

    public static void sendPacket(UpdateData data, ParticleEffect effect) {
        if (data.isClient()) {
            return;
        }
        try {
            ParticlePacket packet = new ParticlePacket(effect);
            data.getManager().sendPacketToAll(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ParticleEffect getParticleEffect() {
        ParticleEffect effect = null;
        try (DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(particleData))) {
            effect = ParticleEffect.deserializeEffect(inputStream);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } 
        return effect;
    }
    @Override
    public Object[] packetVariables() {
        return toPacketVariables(particleData != null ? particleData.length : particleData);
    }

}
