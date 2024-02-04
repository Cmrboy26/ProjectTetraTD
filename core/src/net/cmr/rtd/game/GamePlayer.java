package net.cmr.rtd.game;

import java.security.PublicKey;
import java.util.Iterator;
import java.util.Objects;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import net.cmr.rtd.game.packets.AESEncryptionPacket;
import net.cmr.rtd.game.packets.DisconnectPacket;
import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.packets.PacketEncryption;
import net.cmr.rtd.game.packets.RSAEncryptionPacket;
import net.cmr.rtd.game.stream.GameStream;
import net.cmr.rtd.game.stream.GameStream.PacketListener;
import net.cmr.rtd.game.stream.GameStream.StateListener;
import net.cmr.rtd.game.stream.OnlineGameStream;
import net.cmr.util.Log;

/**
 * Represents a player in a game.
 * This class is used to store {@link GameStream} and other player specific information.
 */
public class GamePlayer {
    
    public static final String GAME_FULL = "The game is full.";
    public static final String USERNAME_TOO_LONG = "The username is too long.";
    public static final String USERNAME_TAKEN = "The username is already taken.";
    public static final String SERVER_CLOSING = "The server is closing.";
    public static final String QUIT = "Quit the game.";
    
    public static final int USERNAME_LENGTH = 16;

    private final GameManager manager;
    private final GameStream stream;
    private final String username;
    private StateListener stateListener;

    private boolean disconnected = false;

    public GamePlayer(GameManager manager, GameStream stream, String username) {
        this.manager = manager;
        this.stream = stream;
        this.username = username;
    }

    public void create() {
        stateListener = new StateListener() {
            @Override
            public void onClose(Iterator<StateListener> it) {
                // Remove the player from the game.
                System.out.println("Player disconnected: " + username);
                onDisconnect();
                it.remove();
            }
            @Override
            public void onOpen(Iterator<StateListener> it) {
                // It's already open.
            }
        };
        this.stream.addStateListener(stateListener);
        this.stream.addListener(new PacketListener() {
            @Override
            public void packetReceived(Packet packet) {
                onRecievePacket(packet);
            }
        });
    }

    public void update(float delta) {
        // Update the player.
        if (stateListener == null) {
            create();
        }
        getStream().update();
    }

    public void kick(String reason) {
        // Kick the player.
        getStream().sendPacket(new DisconnectPacket(reason));
        getStream().onClose();
        onDisconnect();
    }

    private void onDisconnect() {
        // Update the player's state on every other client.
        if (disconnected) {
            return;
        }
        disconnected = true;
        manager.onPlayerDisconnect(this);
    }

    public void sendPacket(Packet packet) {
        // Send a packet to the player.
        Objects.requireNonNull(packet);
        getStream().sendPacket(packet);
    }

    public GameStream getStream() { return stream; }
    public String getUsername() { return username; }

    public boolean equals(Object obj) {
        if (obj instanceof GamePlayer) {
            return ((GamePlayer) obj).getUsername().equals(getUsername());
        }
        if (obj instanceof GameStream) {
            return ((GameStream) obj).equals(getStream());
        }
        return false;
    }

    public void onRecievePacket(Packet packet) {
        Log.debug("Player sent packet: " + packet);

        if (packet instanceof RSAEncryptionPacket) {
            // Set the RSA public key.
            RSAEncryptionPacket rsaPacket = (RSAEncryptionPacket) packet;
            PublicKey publicKey = PacketEncryption.publicKeyFromBytes(rsaPacket.RSAData);
            if (getStream() instanceof OnlineGameStream) {
                OnlineGameStream onlineStream = (OnlineGameStream) getStream();
                onlineStream.getEncryptor().setRSAPublic(publicKey);
            }
        }
        
        if (packet instanceof AESEncryptionPacket) {
            // Set the AES data.
            AESEncryptionPacket aesPacket = (AESEncryptionPacket) packet;
            SecretKey secretKey = PacketEncryption.secretKeyFromBytes(aesPacket.AESData);
            IvParameterSpec iv = PacketEncryption.ivFromBytes(aesPacket.IVData);
            if (getStream() instanceof OnlineGameStream) {
                OnlineGameStream onlineStream = (OnlineGameStream) getStream();
                onlineStream.getEncryptor().setAESData(secretKey, iv);
            }
        }
    }


}
