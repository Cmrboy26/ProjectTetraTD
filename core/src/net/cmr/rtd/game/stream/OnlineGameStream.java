package net.cmr.rtd.game.stream;

import java.util.ArrayList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import net.cmr.rtd.game.packets.ConnectPacket;
import net.cmr.rtd.game.packets.DisconnectPacket;
import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.packets.PacketEncryption;

public class OnlineGameStream extends GameStream {

    Object packetLock = new Object();
    ArrayList<Object> packets;
    Connection connection;

    public OnlineGameStream(PacketEncryption encryptor, Connection connection) {
        super(encryptor);
        this.connection = connection;
        this.packets = new ArrayList<Object>();
        this.connection.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                synchronized (packetLock) {
                    packets.add(object);
                }
            }
            @Override
            public void disconnected(Connection arg0) {
                onClose();
            }
            @Override
            public void connected(Connection arg0) {
                onOpen();
            }
        });
    }

    @Override
    public void update() {
        receivePackets();
    }

    @Override
    public void sendPacket(Packet packet) {
        packet.beforeSend(getEncryptor());
        connection.sendTCP(packet);
    }

    @Override
    public void receivePackets() {
        synchronized (packetLock) {
            for (Object packet : packets) {
                notifyListeners(packet);
            }
            packets.clear();
        }
    }

    public static void registerPackets(Kryo kryo) {
        kryo.setRegistrationRequired(true);
        kryo.register(String.class);
        kryo.register(Packet.class);
        kryo.register(ConnectPacket.class);
        kryo.register(DisconnectPacket.class);
    }
    
}
