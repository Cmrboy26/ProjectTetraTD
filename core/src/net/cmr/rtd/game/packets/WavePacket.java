package net.cmr.rtd.game.packets;

/**
 * Notifies the client that a wave has started for a certain duration.
 * Wave number of 0 indicates that the game is not playing.
 */
public class WavePacket extends Packet {
    
    public float duration, waveLength;
    public int waveNumber;
    public boolean paused, warn;
    
    public WavePacket() { super(); }
    
    public WavePacket(boolean paused, float duration, float waveLength, int waveNumber, boolean warn) {
        this();
        this.duration = duration;
        this.waveLength = waveLength;
        this.waveNumber = waveNumber;
        this.paused = paused;
        this.warn = warn;
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(duration);
    }

    /**
     * Returns the current progress in the wave. If the duration is greater than
     * the wave length, duration - waveLength is the amount of time until the next wave begins
     * @return The current progress in the wave in seconds
     */
    public float getDuration() {
        return duration;
    }

    /**
     * Returns the wave number
     */
    public int getWaveNumber() {
        return waveNumber;
    }

    /**
     * Returns how long the next wave will be in seconds
     */
    public float getWaveLength() {
        return waveLength;
    }

    /**
     * Returns whether the wave is paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Returns whether the player should be warned about the wave
     */
    public boolean shouldWarn() {
        return warn;
    }

}
