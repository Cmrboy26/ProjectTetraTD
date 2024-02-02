package net.cmr.rtd.game.packets;

public class DisconnectPacket extends Packet {

    public String reason;

    public DisconnectPacket() { super(); }
    public DisconnectPacket(String reason) { 
        super();
        this.reason = reason;
    }
    
}
