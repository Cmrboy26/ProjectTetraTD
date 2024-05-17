package net.cmr.rtd.game;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.badlogic.gdx.math.Vector2;

import net.cmr.rtd.game.packets.DisconnectPacket;
import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.packets.PlayerInputPacket;
import net.cmr.rtd.game.packets.PurchaseItemPacket;
import net.cmr.rtd.game.packets.SkipRequestPacket;
import net.cmr.rtd.game.stream.GameStream;
import net.cmr.rtd.game.stream.GameStream.PacketListener;
import net.cmr.rtd.game.stream.GameStream.StateListener;
import net.cmr.rtd.game.world.entities.Player;
import net.cmr.rtd.game.world.store.ShopManager;
import net.cmr.rtd.game.world.tile.Tile;
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
    public static final String KICKED = "You have been kicked.";
    public static final String PASSWORD_INCORRECT = "The password is incorrect.";
    
    public static final int USERNAME_LENGTH = 16;

    private final GameManager manager;
    private final GameStream stream;
    private final String username;
    private Player player;
    private int team = -1;
    private StateListener stateListener;
    private PlayerInputPacket lastInput;

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
                try {
                    onRecievePacket(packet);
                } catch (Exception e) {
                    // Handle errors with processing the packet.
                    Log.error("Error while handling packet " + packet + " with player "+getUsername(), e);
                }
            }
        });
    }

    public void update(float delta) {
        // Update the player.
        if (stateListener == null) {
            create();
        }
        getStream().update();
        if (player != null) {
            if (lastInput != null) {
                player.updateInput(lastInput.getInput(), lastInput.isSprinting());
            }
        }
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

    public void sendPackets(List<Packet> packets) {
        // Send a collection of packets to the player.
        Objects.requireNonNull(packets);
        for (int i = 0; i < packets.size(); i++) {
            Packet packet = packets.get(i);
            sendPacket(packet);
        }
    }

    public GameManager getManager() { return manager; }
    public GameStream getStream() { return stream; }
    public String getUsername() { return username; }
    public int getTeam() { return team; }
    public void setTeam(int team) { this.team = team; }
    public void setPlayer(Player player) { this.player = player; }
    public Player getPlayer() { return player; }

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

        // Handle the packet.
        if (packet instanceof PlayerInputPacket) {
            // Update the player's input.
            PlayerInputPacket input = (PlayerInputPacket) packet;
            lastInput = input;

            Vector2 playerLastPosition = player.getPosition();
            Vector2 playerNewPosition = input.getPosition();
            float difference = playerLastPosition.dst(playerNewPosition);

            // TODO: Make a better system that assumes the player is cheating

            // If the player's position has changed significantly, send an update packet.
            if (difference > Tile.SIZE / 8f) {
                // Send the player an update position packet.
                sendPacket(new PlayerInputPacket(null, playerNewPosition, false));
            }

            player.setPosition(input.getPosition());
            player.updateInput(input.getInput(), input.isSprinting());
        }

        if (packet instanceof PurchaseItemPacket) {
            // Purchase an item from the shop.
            PurchaseItemPacket purchase = (PurchaseItemPacket) packet;
            ShopManager.processPurchase(manager, this, purchase);
        }

        if (packet instanceof SkipRequestPacket) {
            SkipRequestPacket request = (SkipRequestPacket) packet;
            getManager().getWorld().requestSkip(this);
        }

    }


}
