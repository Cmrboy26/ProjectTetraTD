package net.cmr.rtd;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.stream.Collectors;

import com.esotericsoftware.kryo.Kryo;

import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.packets.PacketEncryption;
import net.cmr.rtd.game.stream.OnlineGameStream;

public class TestLauncher {
    
    public static void main(String[] args) {
        Kryo kryo = new Kryo();
        OnlineGameStream.registerPackets(kryo);
        
        System.out.println("Checking if all packet classes are registered...");
        Set<Class> packetClasses = findAllClassesUsingClassLoader("net.cmr.rtd.game.packets");
        boolean errorFound = false;
        for (Class clazz : packetClasses) {
            try {

                if (clazz == PacketEncryption.class) continue;
                if (clazz == Packet.PacketSerializer.class) continue;

                kryo.getRegistration(clazz);
            } catch (Exception e) {
                System.out.println("Packet class not registered: " + clazz);
                errorFound = true;
            }
        }
        if (!errorFound) {
            System.out.println("All packet classes registered successfully!");
        }
    }

    public static Set<Class> findAllClassesUsingClassLoader(String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader()
          .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
          .filter(line -> line.endsWith(".class"))
          .map(line -> getClass(line, packageName))
          .collect(Collectors.toSet());
    }
 
    private static Class getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
              + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            // handle the exception
        }
        return null;
    }

}
