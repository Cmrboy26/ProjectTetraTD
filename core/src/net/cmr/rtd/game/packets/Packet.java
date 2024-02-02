package net.cmr.rtd.game.packets;

/**
 * A packet that can be sent over the network.
 * 
 */
public abstract class Packet {

    public Packet() { /* Empty constructor for Kryo */ }

    /**
     * Called before the packet is sent.
     * This should be called in {@link GameStream#sendPacket(Packet)}
     * @param encryptor The encryptor to use for the packet.
     */
    public void beforeSend(PacketEncryption encryptor) {
        if (getEncryptionType() != EncryptionType.NONE) {
            encryptor.encrypt(this);
        }
    }

    /**
     * Called after the packet is received.
     * In {@link GameStream}, this is automatically called in {@link GameStream#notifyListeners(Packet)}
     * @param encryptor The encryptor to use for the packet.
     */
    public void afterReceive(PacketEncryption encryptor) {
        if (getEncryptionType() != EncryptionType.NONE) {
            encryptor.decrypt(this);
        }
    }

    public enum EncryptionType {
        NONE, RSA, AES
    }

    /**
     * Can be overriden.
     * @return The type of encryption used for this packet.
     */
    public EncryptionType getEncryptionType() {
        return EncryptionType.NONE;
    }
    
}
