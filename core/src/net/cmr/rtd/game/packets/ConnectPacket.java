package net.cmr.rtd.game.packets;

public class ConnectPacket extends Packet {
    
    public String username;
    public int team;

    public ConnectPacket() { super(); }
    public ConnectPacket(String username, int team) {
        super();
        this.username = username;
        this.team = team;
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(username);
    }
    

}
