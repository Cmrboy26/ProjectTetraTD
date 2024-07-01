package net.cmr.rtd.game.packets;

public class GameSpeedChangePacket extends Packet {

    public float speed;

    public GameSpeedChangePacket() { }
    public GameSpeedChangePacket(float speed) {
        this.speed = speed;
    }

    @Override
    public Object[] packetVariables() {
        return new Object[] { speed };
    }
    
}
