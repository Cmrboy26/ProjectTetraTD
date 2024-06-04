package net.cmr.rtd.game.packets;

/**
 * This packet will be used once the game is over.
 */
public class GameOverPacket extends Packet {

    public int endingWave; // Wave that the game ended on
    public long score; // Score of the client or client's team
    public boolean stillAlive; // If the client team is still alive

    public GameOverPacket() { /* Empty constructor for KryoNet */}

    public GameOverPacket(int endingWave, long score, boolean stillAlive) {
        this.endingWave = endingWave;
        this.score = score;
        this.stillAlive = stillAlive;
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(endingWave, score, stillAlive);
    }
    
}
