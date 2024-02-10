package net.cmr.rtd.game.packets;

import java.security.PublicKey;
import java.util.Base64;

/**
 * Handles syncing encryption between the client and server.
 * 
 * Server -> Client: Send ServerRSA
 * Client -> Server: Send ClientRSA
 * Server -> Client: Send ServerAES(Encrypted with ClientRSA)
 * Client: Set AES and RSA data
 * Everything is synced.
 */
public class RSAEncryptionPacket extends Packet {

    public byte[] RSAData;

    public RSAEncryptionPacket() { super(); }

    public RSAEncryptionPacket(PublicKey RSAData) {
        super();
        this.RSAData = PacketEncryption.publicKeyToBytes(RSAData);
    }

    @Override
    public Object[] packetVariables() {
        return toPacketVariables(new String(Base64.getEncoder().encode(RSAData)));
    }


}
