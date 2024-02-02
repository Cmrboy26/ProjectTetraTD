package net.cmr.rtd.game.stream;

import java.util.ArrayList;

import net.cmr.rtd.game.packets.Packet;

public class LocalGameStream extends GameStream {

    Object packetLock = new Object();
    LocalGameStream otherStream;
    ArrayList<Object> packets;

    private LocalGameStream() {
        super(null);
        packets = new ArrayList<Object>();
    }

    public LocalGameStream(LocalGameStream stream) {
        super(null);
        this.otherStream = stream;
        packets = new ArrayList<Object>();
    }

    public static LocalGameStream[] createStreamPair() {
        LocalGameStream[] streams = new LocalGameStream[2];
        streams[0] = new LocalGameStream();
        streams[1] = new LocalGameStream();
        streams[0].otherStream = streams[1];
        streams[1].otherStream = streams[0];
        return streams;
    }

    @Override
    public void update() {
        receivePackets();
    }

    @Override
    public void sendPacket(Packet packet) {
        synchronized (packetLock) {
            packet.beforeSend(getEncryptor());
            otherStream.packets.add(packet);
        }
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


    
}
