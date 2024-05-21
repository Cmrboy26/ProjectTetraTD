package net.cmr.rtd.game.packets;

import java.util.UUID;

// Clientbound
public class AttackPacket extends Packet {

    UUID entityUUID;

    public AttackPacket() { }

    /**
     * When the {@link TowerEntity#attack(UpdateData)} method is called, this packet is sent to the client to inform them of the attack.
     * @param entityUUID the UUID of the tower that is attacking
     */
    public AttackPacket(UUID entityUUID) {
        this.entityUUID = entityUUID;
    }

    public UUID getTowerUUID() {
        return entityUUID;
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(entityUUID);
    }
    

}
