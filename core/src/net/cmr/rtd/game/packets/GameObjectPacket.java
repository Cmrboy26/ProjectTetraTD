package net.cmr.rtd.game.packets;

import java.util.Objects;

import net.cmr.rtd.game.world.GameObject;

public class GameObjectPacket extends Packet {

    private byte[] data;

    protected GameObjectPacket() { super(); }
    public GameObjectPacket(GameObject object) {
        super();
        Objects.requireNonNull(object);
        this.data = GameObject.serializeGameObject(object);
    }

    public GameObject getObject() {
        return GameObject.deserializeGameObject(data);
    }

    @Override
    public boolean shouldCompressPacket() {
        return true;
    }

    @Override
    public Object[] packetVariables() {
        return new Object[] { data.length };
    }

}
