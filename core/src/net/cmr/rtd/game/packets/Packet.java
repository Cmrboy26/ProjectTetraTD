package net.cmr.rtd.game.packets;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import net.cmr.rtd.game.stream.GameStream;

/**
 * A packet that can be sent over the game network.
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
            if (encryptor != null) {
                //encryptor.encrypt(this);
            }
        }
    }

    /**
     * Called after the packet is received.
     * In {@link GameStream}, this is automatically called in {@link GameStream#notifyListeners(Packet)}
     * @param encryptor The encryptor to use for the packet.
     */
    public void afterReceive(PacketEncryption encryptor) {
        if (getEncryptionType() != EncryptionType.NONE) {
            if (encryptor != null) {
                //encryptor.decrypt(this);
            }
        }
    }

    public abstract Object[] packetVariables();
    public Object[] toPacketVariables(Object... objects) {
        return objects;
    }

    @Override
    public String toString() {
        String header = "[" +getClass().getSimpleName() + "]{";
        for (Object o : packetVariables()) {
            header += "\"" + o + "\", ";
        }
        header = header.substring(0, header.length() - 2);
        header += "}";
        return header;
    }

    /**
     * Can be overriden.
     * @return Whether or not the packet should be compressed.
     */
    public boolean shouldCompressPacket() {
        return false;
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

    // NOTE: IF YOU DELETE THIS, LOOK AT THIS POST: https://stackoverflow.com/questions/39905881/kryonet-how-to-get-the-sender-connection-during-serialization
    
    public static class PacketSerializer<T extends Packet> extends Serializer<T> {

        private final Serializer<T> defaultSerializer;

        @SuppressWarnings("unchecked")
        public PacketSerializer(Kryo kryo, Class<T> type) {
            this.defaultSerializer = kryo.getDefaultSerializer(type);
        }

        @Override
        public void write(Kryo kryo, Output output, T object) {
            Packet packet = (Packet) object;
            // Get the encryptor from Kryo's context
            @SuppressWarnings("unchecked")
            PacketEncryption encryption = (PacketEncryption) kryo.getContext().get(PacketEncryption.class);
            output.writeBoolean(packet.shouldCompressPacket());
            output.writeByte(packet.getEncryptionType().ordinal());

            // Write the packet
            try {
                OutputStream combinedOutput = output;
                if (packet.shouldCompressPacket()) {
                    combinedOutput = new GZIPOutputStream(combinedOutput);
                }
                combinedOutput = encryption.encrypt(packet, combinedOutput);
                // All encrypted packets should be compressed before encryption
                if (packet.getEncryptionType() == EncryptionType.NONE) {
                    combinedOutput = new GZIPOutputStream(combinedOutput);
                }
                Output finalOutput = new Output(combinedOutput);

                defaultSerializer.write(kryo, finalOutput, object);
                finalOutput.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public T read(Kryo kryo, Input input, Class<? extends T> type) {
            boolean compressed = input.readBoolean();
            byte encryptionType = input.readByte();
            EncryptionType typeEnum = EncryptionType.values()[encryptionType];

            // Get the encryptor from Kryo's context
            @SuppressWarnings("unchecked")
            PacketEncryption encryption = (PacketEncryption) kryo.getContext().get(PacketEncryption.class);
            try {
                InputStream combinedInput = input;
                if (compressed) {
                    combinedInput = new GZIPInputStream(combinedInput);
                }
                combinedInput = encryption.decrypt(typeEnum, combinedInput);
                // All encrypted packets should be compressed before encryption, so undo that here
                if (typeEnum == EncryptionType.NONE) {
                    combinedInput = new GZIPInputStream(combinedInput);
                }

                Input finalInput = new Input(combinedInput);
                T packet = defaultSerializer.read(kryo, finalInput, type);
                finalInput.close();
                return packet;
            } catch (Exception e) {
                e.printStackTrace();
            }
            throw new KryoException("Failed to read packet.");
        }

    }
    
}
