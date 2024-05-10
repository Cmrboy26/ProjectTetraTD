package net.cmr.rtd.game.packets;

public class PlayerPacket extends Packet{
    
    public String username;
    public boolean isConnecting, initializingWorld;
    public float x, y;

    public enum PlayerPacketType {
        CONNECTING(true), DISCONNECTING(false), INITIALIZE_WORLD(true);
        private boolean isConnecting;
        private PlayerPacketType(boolean isConnecting) {
            this.isConnecting = isConnecting;
        }
        public boolean isConnecting() {
            return isConnecting;
        }
    }

    public PlayerPacket() { super(); }
    public PlayerPacket(String username, float x, float y, PlayerPacketType type) {
        super();
        this.username = username;
        this.isConnecting = type.isConnecting();
        this.initializingWorld = type == PlayerPacketType.INITIALIZE_WORLD;
        this.x = x;
        this.y = y;
    }

    public boolean isConnecting() {
        return isConnecting;
    }
    public boolean isDisconnecting() {
        return !isConnecting;
    }
    public boolean isInitializingWorld() {
        return initializingWorld;
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(username, isConnecting, initializingWorld);
    }

}
