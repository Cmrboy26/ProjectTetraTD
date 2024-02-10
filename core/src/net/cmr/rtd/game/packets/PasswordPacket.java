package net.cmr.rtd.game.packets;

public class PasswordPacket extends Packet {
    
    String password;
    
    public PasswordPacket(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public EncryptionType getEncryptionType() {
        return EncryptionType.AES;
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(password);
    }
    
}
