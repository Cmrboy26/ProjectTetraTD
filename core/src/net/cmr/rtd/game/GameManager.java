package net.cmr.rtd.game;

import java.util.HashMap;

import com.badlogic.gdx.utils.Disposable;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import net.cmr.rtd.game.packets.ConnectPacket;
import net.cmr.rtd.game.packets.DisconnectPacket;
import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.packets.PacketEncryption;
import net.cmr.rtd.game.stream.GameStream;
import net.cmr.rtd.game.stream.OnlineGameStream;
import net.cmr.rtd.game.stream.GameStream.PacketListener;
import net.cmr.util.Log;

/**
 * This class can act like a server for a multiplayer game or a handler for a single player game.
 * It will handle the game logic, game state, and updating/sending necesary information to the client.
 */
public class GameManager implements Disposable {

    /**
     * The details of the game manager.
     */
    public static class GameManagerDetails {
        private int maxPlayers = 4;
        private boolean actAsServer = false;

        public GameManagerDetails() {

        }

        public void actAsServer(boolean actAsServer) { this.actAsServer = actAsServer; }
        public boolean isActingAsServer() { return actAsServer; }
        public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
        public int getMaxPlayers() { return maxPlayers; }
    }

    private final GameManagerDetails details;
    private final HashMap<String, GamePlayer> players = new HashMap<String, GamePlayer>();
    private final Server server;

    public GameManager(GameManagerDetails details) {
        this.details = details;
        if (details.isActingAsServer()) {
            // Create server objects
            server = new Server();
            // Register packets
            OnlineGameStream.registerPackets(server.getKryo());
            server.addListener(new Listener() {
                @Override
                public void connected(Connection arg0) {
                    PacketEncryption encryptor = new PacketEncryption();
                    OnlineGameStream stream = new OnlineGameStream(encryptor, arg0);
                    onPlayerConnect(stream);
                }
            });
            // Connect to server
            throw new UnsupportedOperationException("Not implemented yet.");
            // return;
        }
        server = null;
    }

    /**
     * Called when a player connects to the server.
     * @param clientRecieverStream
     * If it is of type {@link LocalGameStream}, then the arguement passed should be the stream that recieves packets from the client, NOT the client sender.
     * If it is of type {@link OnlineGameStream}, then the arguement passed should be a new online stream derived from the connection of a new client. 
     */
    public void onPlayerConnect(GameStream clientRecieverStream) {
        clientRecieverStream.addListener(new PacketListener() {
            @Override
            public void packetReceived(Packet packet) {
                // When a player sends a ConnectPacket, set their username and put them in the players map.
                if (packet instanceof ConnectPacket) {
                    // If it's full, send a disconnect packet.
                    if (players.size() >= details.getMaxPlayers()) {
                        // The game is full.
                        clientRecieverStream.sendPacket(new DisconnectPacket(GamePlayer.GAME_FULL));
                        return;
                    }
                    // If the username is too long, send a disconnect packet.
                    String username = ((ConnectPacket) packet).username;
                    if (username.length() > GamePlayer.USERNAME_LENGTH) {
                        // The username is too long.
                        clientRecieverStream.sendPacket(new DisconnectPacket(GamePlayer.USERNAME_TOO_LONG));
                        return;
                    }
                    // If the username is already taken, send a disconnect packet.
                    if (players.containsKey(username)) {
                        // The username is already taken.
                        clientRecieverStream.sendPacket(new DisconnectPacket(GamePlayer.USERNAME_TAKEN));
                        return;
                    }
                    // Add the player to the players map.
                    GamePlayer player = new GamePlayer(clientRecieverStream, username);
                    players.put(username, player);
                    clientRecieverStream.removeListener(this);
                    Log.info("Player connected to server: " + username);
                    return;
                }  
                // If they send a GameInfoRequestPacket, send them information about the server.
            }
        });
    }

    public void start() {

    }

    public void update() {
        // Update the game state.
    }

    public void dispose() {
        // Dispose of the game manager.
    }

}
