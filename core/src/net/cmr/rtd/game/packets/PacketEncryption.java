package net.cmr.rtd.game.packets;

/**
 * A class that processes encrypted packets depending on the encryption type.
 */
public class PacketEncryption {

    public PacketEncryption() {

    }

    public void setRSAData() {
        // TODO: Implement RSA encryption
    }
    public void encryptRSA(Packet packet) {
        // TODO: Implement RSA encryption
    }
    public void decryptRSA(Packet packet) {
        // TODO: Implement RSA decryption
    }

    public void setAESData() {
        // TODO: Implement AES encryption
    }
    private void encryptAES(Packet packet) {
        // TODO: Implement AES encryption
    }
    private void decryptAES(Packet packet) {
        // TODO: Implement AES decryption
    }

    public void encrypt(Packet packet) {
        switch (packet.getEncryptionType()) {
            case RSA:
                encryptRSA(packet);
                break;
            case AES:
                encryptAES(packet);
                break;
            default:
                break;
        }
    }

    public void decrypt(Packet packet) {
        switch (packet.getEncryptionType()) {
            case RSA:
                decryptRSA(packet);
                break;
            case AES:
                decryptAES(packet);
                break;
            default:
                break;
        }
    }
    
}
