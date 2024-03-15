package net.cmr.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

import com.badlogic.gdx.utils.DataBuffer;

public interface SerializableEnum {
    public int getID();

    public default void serialize(DataBuffer buffer) throws IOException {
        buffer.writeInt(getID());
    }
    public default SerializableEnum deserialize(DataInputStream input) throws IOException {
        return getEnumMap().get(input.readInt());
    }
    public HashMap<Integer, SerializableEnum> getEnumMap();
}
