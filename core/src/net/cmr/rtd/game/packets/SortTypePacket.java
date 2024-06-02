package net.cmr.rtd.game.packets;

import java.util.Objects;

import net.cmr.rtd.game.world.entities.TowerEntity.SortType;

public class SortTypePacket extends Packet {

    public SortType type;
    public int tileX, tileY;

    public SortTypePacket() { }
    public SortTypePacket(int tileX, int tileY, SortType type) {
        Objects.requireNonNull(type);
        this.type = type;
        this.tileX = tileX;
        this.tileY = tileY;
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(tileX, tileY, type);
    }

    
    
}
