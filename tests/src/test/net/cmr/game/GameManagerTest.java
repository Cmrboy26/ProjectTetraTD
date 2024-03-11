package net.cmr.game;

import java.lang.reflect.Field;
import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.commons.util.ReflectionUtils;

import com.esotericsoftware.kryo.Kryo;

import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.stream.OnlineGameStream;

public class GameManagerTest {

    /**
     * Tests if all packets are registered in the Kryo instance.
     * This is important for the network protocol to work.
     * {@link OnlineGameStream#registerPackets(Kryo)}
     * @see OnlineGameStream#registerPackets(Kryo)
     */
    @Test
    public void testPacketRegistration() {
        Kryo kryo = new Kryo();
        OnlineGameStream.registerPackets(kryo);

        HashSet<Class<?>> nonRegistredClasses = new HashSet<>();

        ClassFilter packetFilter = ClassFilter.of(clazz -> Packet.class.isAssignableFrom(clazz));
        ReflectionUtils.findAllClassesInPackage("net.cmr.rtd.game.packets", packetFilter).forEach(clazz -> {
            // Test if the packet itself is registered
            try {
                // Throws an IllegalArgumentException if the class is not registered
                kryo.getRegistration(clazz);
            } catch (IllegalArgumentException e) {
                nonRegistredClasses.add(clazz);
            }

            // Test if the variables in the packet are registered
            Field[] fields = clazz.getFields();
            for (Field field : fields) {
                try {
                    // Throws an IllegalArgumentException if the class is not registered
                    kryo.getRegistration(field.getType());
                } catch (IllegalArgumentException e) {
                    nonRegistredClasses.add(field.getType());
                }
            }
        });

        // If the list is empty, all packets are registered
        if (nonRegistredClasses.isEmpty()) {
            return;
        }
        // If not, list the classes that need to be registered

        String errorMessage = "The following packets are not registered ({@see OnlineGameStream#registerPackets(Kryo)}):\n";
        for (Class<?> clazz : nonRegistredClasses) {
            errorMessage += "- " + clazz.getSimpleName() + ".class, " + "\n";
        }
        errorMessage = errorMessage.substring(0, errorMessage.length() - 3);

        throw new AssertionError(errorMessage);
    }

    @Test
    public void testPacketEncryptionFunctionality() {
        // TODO: Implement
    }

}

