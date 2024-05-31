package net.cmr.rtd.game.packets;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.util.UUIDUtils;

public class JumpPacket extends Packet {
    
    byte[] data;

    public JumpPacket () { }
    public JumpPacket(UUID playerUUID) {
        try {
            DataBuffer buffer = new DataBuffer();
            UUIDUtils.serializeUUID(buffer, playerUUID);
            buffer.flush();
            data = buffer.toArray();
            buffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UUID getPlayerUUID() {
        try {
            DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));
            UUID uuid = UUIDUtils.deserializeUUID(stream);
            stream.close();
            return uuid;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object[] packetVariables() {
        return new Object[] { data };
    }

}
