package net.cmr.game;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.commons.util.ReflectionUtils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;

import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.stream.OnlineGameStream;

public class GameManagerTest {

    @Test
    public void testIfPacketsAreRegistered() {
        Kryo kryo = new Kryo();
        OnlineGameStream.registerPackets(kryo);

        ClassFilter packetFilter = ClassFilter.of(clazz -> Packet.class.isAssignableFrom(clazz));
        ReflectionUtils.findAllClassesInPackage("net.cmr.rtd.game.packets", packetFilter).forEach(clazz -> {
            Object registration;
            try {
                registration = kryo.getRegistration(clazz);
            } catch (IllegalArgumentException e) {
                registration = null;
            }
            assertNotNull(registration, "Packet " + clazz.getSimpleName() + " is not registered! (Add it to OnlineGameStream.registerPackets(Kryo kryo)");
        });
    }

}

