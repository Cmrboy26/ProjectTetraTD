package net.cmr.rtd.game.packets;

public class ConnectPacket extends Packet {
    
    public String username;

    public ConnectPacket() { super(); }
    public ConnectPacket(String username) {
        super();
        this.username = username;
    }

}
