package net.cmr.rtd.game.packets;

public class SkipRequestPacket extends Packet {

    public SkipRequestPacket() { }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables();
    }
    
}
