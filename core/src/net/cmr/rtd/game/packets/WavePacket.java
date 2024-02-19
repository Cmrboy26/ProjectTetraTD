package net.cmr.rtd.game.packets;

/**
 * Notifies the client that a wave has started for a certain duration.
 * Wave number of 0 indicates that the game is not playing.
 */
public class WavePacket extends Packet {
    
    public float duration;
    public int waveNumber;
    
    public WavePacket() { super(); }
    
    public WavePacket(float duration, int waveNumber) {
        this();
        this.duration = duration;
        this.waveNumber = waveNumber;
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(duration);
    }

    public float getDuration() {
        return duration;
    }

    public int getWaveNumber() {
        return waveNumber;
    }

}
