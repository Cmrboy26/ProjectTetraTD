package net.cmr.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

import com.badlogic.gdx.utils.DataBuffer;

public class UUIDUtils {

    public static void serializeUUID(DataBuffer buffer, UUID uuid) throws IOException {
        buffer.writeLong(uuid.getMostSignificantBits());
        buffer.writeLong(uuid.getLeastSignificantBits());
    }

    public static UUID deserializeUUID(DataInputStream input) throws IOException {
        return new UUID(input.readLong(), input.readLong());
    }
    
}
