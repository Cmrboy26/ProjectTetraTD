package net.cmr.rtd.game.packets;

import net.cmr.rtd.game.world.GameObject.GameType;

public class SetPlayerShopPacket extends Packet {

    public GameType[] types;

    public SetPlayerShopPacket() { }
    public SetPlayerShopPacket(GameType[] types) {
        this.types = types;
    }

    @Override
    public Object[] packetVariables() {
        return types;
    }
    
}
