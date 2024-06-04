package net.cmr.rtd.game.packets;

public class GameResetPacket extends Packet {

    public GameResetPacket() { /* Empty constructor for KryoNet */}

    @Override
    public Object[] packetVariables() {
        return new Object[0];
    }
    
}
