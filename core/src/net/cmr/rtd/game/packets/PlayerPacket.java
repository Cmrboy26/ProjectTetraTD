package net.cmr.rtd.game.packets;

public class PlayerPacket extends Packet{
    
    public String username;
    public boolean isConnecting;

    public enum PlayerPacketType {
        CONNECTING(true), DISCONNECTING(false);
        private boolean value;
        private PlayerPacketType(boolean value) {
            this.value = value;
        }
        public boolean getValue() {
            return value;
        }
    }

    public PlayerPacket() { super(); }
    public PlayerPacket(String username, PlayerPacketType type) {
        super();
        this.username = username;
        this.isConnecting = type.getValue();
    }

    public boolean isConnecting() {
        return isConnecting;
    }
    public boolean isDisconnecting() {
        return !isConnecting;
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(username, isConnecting);
    }

}
