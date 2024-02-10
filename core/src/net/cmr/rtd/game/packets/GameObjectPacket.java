package net.cmr.rtd.game.packets;

import java.util.Objects;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;

public class GameObjectPacket extends Packet {

    private byte[] data;
    private boolean remove;

    protected GameObjectPacket() { super(); }
    
    public GameObjectPacket(GameObject object) {
        this(object, false);
        if (object instanceof Entity) {
            throw new IllegalArgumentException("The object must be an entity. (Append a boolean to the end of the constructor).");
        }
    }

    public GameObjectPacket(GameObject object, boolean remove) {
        super();
        Objects.requireNonNull(object);
        this.data = GameObject.serializeGameObject(object);
        this.remove = remove;
    }

    public GameObject getObject() {
        return GameObject.deserializeGameObject(data);
    }

    public boolean shouldRemove() {
        return remove;
    }

    @Override
    public boolean shouldCompressPacket() {
        return true;
    }

    @Override
    public Object[] packetVariables() {
        return new Object[] { data.length, remove };
    }

}
