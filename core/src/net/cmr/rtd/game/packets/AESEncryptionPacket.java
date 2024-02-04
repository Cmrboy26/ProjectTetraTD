package net.cmr.rtd.game.packets;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class AESEncryptionPacket extends Packet {

    public byte[] AESData;
    public byte[] IVData;

    public AESEncryptionPacket() { super(); }

    public AESEncryptionPacket(SecretKey AESData, IvParameterSpec IVData) {
        super();
        this.AESData = PacketEncryption.secretKeyToBytes(AESData);
        this.IVData = PacketEncryption.ivToBytes(IVData);
    }

    @Override
    public EncryptionType getEncryptionType() {
        return EncryptionType.RSA;
    }

    @Override
    public Object[] packetVariables() {
        return new Object[] { AESData.length, IVData.length };
    }
    
}
