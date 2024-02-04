package net.cmr.rtd.game.stream;

import java.util.ArrayList;
import java.util.Iterator;

import com.esotericsoftware.kryo.util.Null;
import com.esotericsoftware.kryonet.Connection;

import net.cmr.rtd.game.packets.ConnectPacket;
import net.cmr.rtd.game.packets.DisconnectPacket;
import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.packets.PacketEncryption;

/**
 * This class should be used to send and receive packets from the server or client.
 * It can be used on both ends of the connection.
 * Different implementations can be used for singleplayer games and multiplayer games.
 * Singleplayer: LocalGameStream
 * - This implementation will send/recieve packets directly from the local machine.
 * - 
 * Multiplayer: OnlineGameStream
 * - This implementation will send/recieve packets via a Connection object.
 */
public abstract class GameStream {

    private ArrayList<PacketListener> listeners;
    private ArrayList<StateListener> stateListeners;
    private @Null PacketEncryption encryptor;
    private boolean open = false;
    private Object listenerLock = new Object();
    private Object stateListenerLock = new Object();

    public GameStream(@Null PacketEncryption encryptor) {
        listeners = new ArrayList<PacketListener>();
        stateListeners = new ArrayList<StateListener>();
        this.encryptor = encryptor;
    }

    public static interface PacketListener {
        /**
         * This method is called when a packet is received.
         * @param packet The packet that was received.
         * @return IMPORTANT: return true to REMOVE the listener from the list of listeners.
         */
        public void packetReceived(Packet packet);
        public default boolean shouldRemoveListener() {
            return false;
        }
    }

    public static interface StateListener {
        public void onClose(Iterator<StateListener> it);
        public void onOpen(Iterator<StateListener> it);
    }

    public void addListener(PacketListener listener) {
        synchronized (listenerLock) {
            listeners.add(listener);
        }
    }
    public void removeListener(PacketListener listener) {
        synchronized (listenerLock) {
            listeners.remove(listener);
        }
    }
    public void addStateListener(StateListener listener) {
        synchronized (stateListenerLock) {
            stateListeners.add(listener);
        }
    }
    public void removeStateListener(StateListener listener) {
        synchronized (stateListenerLock) {
            stateListeners.remove(listener);
        }
    }

    /**
     * Notifies all the listeners that a packet has been received.
     * This should be called at the end of {@link #receivePackets() receivePackets()}
     * @param packet The packet that was received.
     * @see #receivePackets()
     */
    protected synchronized void notifyListeners(Object packet) {
        if (!(packet instanceof Packet)) {
            return;
        }
        Packet p = (Packet) packet;
        processPacket(p);
        
        synchronized (listenerLock) {
            Iterator<PacketListener> it = listeners.iterator();
            while (it.hasNext()) {
                PacketListener listener = it.next();
                listener.packetReceived(p);
            }
            it = listeners.iterator();
            while (it.hasNext()) {
                PacketListener listener = it.next();
                if (listener.shouldRemoveListener()) {
                    it.remove();
                }
            }
        }
    }

    /**
     * This method should be called in the game manager update method.
     * It should be called in both the client and game manager update methods.
     * Implemenations of this method should call {@link #notifyListeners(Packet) notifyListeners} to check for received packets.
     */
    public abstract void update();

    /**
     * This method is used to send packets to the server or client.
     */
    public abstract void sendPacket(Packet packet);

    /**
     * This method should be used to check for received packets from the server or client.
     * This method should call {@link #notifyListeners(Packet) notifyListeners} when a packet is received.
     * @see #notifyListeners(Packet)
     */
    public abstract void receivePackets();

    /**
     * This method should be used to process connection packets.
     * @param packet
     */
    private void processPacket(Packet packet) {
        packet.afterReceive(encryptor);
        if (packet instanceof DisconnectPacket) {
            // Handle disconnect packet.
            onClose();
        }
        if (packet instanceof ConnectPacket) {
            // Handle connect packet.
            onOpen();
        }
    }

    /**
     * This method should be called when the game stream is closed.
     * For {@link LocalGameStream} this method should close the GameManager.
     * For {@link OnlineGameStream} this method should close the connection.
     */
    public void onClose() {
        if (!open) return;
        open = false;
        synchronized (stateListenerLock) {
            Iterator<StateListener> it = stateListeners.iterator();
            while (it.hasNext()) {
                StateListener listener = it.next();
                listener.onClose(it);
            }
        }
    }

    /**
     * This method should be called when the game stream is opened.
     * For {@link LocalGameStream} this method should open the GameManager.
     * For {@link OnlineGameStream} this method should open the connection.
     */
    public void onOpen() {
        if (open) return;
        open = true;
        synchronized (stateListenerLock) {
            Iterator<StateListener> it = stateListeners.iterator();
            while (it.hasNext()) {
                StateListener listener = it.next();
                listener.onOpen(it);
            }
        }
    }

    public boolean isOpen() {
        return open;
    }
    public boolean isClosed() {
        return !open;
    }

    public PacketEncryption getEncryptor() {
        return encryptor;
    }

    public void setEncryptor(@Null PacketEncryption encryptor) {
        this.encryptor = encryptor;
    }

}
