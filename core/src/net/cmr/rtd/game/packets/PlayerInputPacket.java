package net.cmr.rtd.game.packets;

import com.badlogic.gdx.math.Vector2;

public class PlayerInputPacket extends Packet {

    public Vector2 input;
    public Vector2 playerPosition;
    public boolean sprinting;

    public PlayerInputPacket() { super(); }
    public PlayerInputPacket(Vector2 input, Vector2 playerPosition, boolean sprinting) {
        super();
        this.input = input;
        this.playerPosition = playerPosition;
        this.sprinting = sprinting;
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(input, playerPosition);
    }

    public Vector2 getInput() {
        return input.nor();
    }
    public Vector2 getPosition() {
        return playerPosition;
    }
    public boolean isSprinting() {
        return sprinting;
    }
    
}
