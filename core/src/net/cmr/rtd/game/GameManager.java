package net.cmr.rtd.game;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import net.cmr.rtd.RetroTowerDefense;
import net.cmr.rtd.game.packets.ConnectPacket;
import net.cmr.rtd.game.packets.DisconnectPacket;
import net.cmr.rtd.game.packets.GameObjectPacket;
import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.packets.PacketEncryption;
import net.cmr.rtd.game.packets.PlayerPacket;
import net.cmr.rtd.game.packets.RSAEncryptionPacket;
import net.cmr.rtd.game.stream.GameStream;
import net.cmr.rtd.game.stream.GameStream.PacketListener;
import net.cmr.rtd.game.stream.LocalGameStream;
import net.cmr.rtd.game.stream.OnlineGameStream;
import net.cmr.rtd.game.world.World;
import net.cmr.util.Log;

/**
 * This class can act like a server for a multiplayer game or a handler for a single player game.
 * It will handle the game logic, game state, and updating/sending necesary information to the client.
 */
public class GameManager implements Disposable {

    private final GameManagerDetails details;
    private final ConcurrentHashMap<String, GamePlayer> players = new ConcurrentHashMap<String, GamePlayer>();
    private final ArrayList<GameStream> connectingStreams = new ArrayList<GameStream>();
    private final Server server;
    private boolean running = false;
    private GameSave save = null;

    private World world;

    /**
     * Create a new game manager.
     * After this is called, {@link #initialize(GameSave)} should be called to initialize the game.
     * @param details
     */
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
                    OnlineGameStream stream = new OnlineGameStream(new PacketEncryption(), arg0);
                    onPlayerConnect(stream);
                }
            });
            // Prepare server

            return;
        }
        server = null;
    }

    /**
     * Called when a player connects to the server.
     * @param clientRecieverStream
     * If it is of type {@link LocalGameStream}, then the argument passed should be the stream that recieves packets from the client, NOT the client sender.
     * If it is of type {@link OnlineGameStream}, then the argument passed should be a new online stream derived from the connection of a new client. 
     */
    public void onPlayerConnect(GameStream clientRecieverStream) {
        Log.debug("A new player may want to join...", clientRecieverStream);
        clientRecieverStream.addListener(new PacketListener() {
            boolean remove = false;
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
                    GamePlayer player = new GamePlayer(GameManager.this, clientRecieverStream, username);
                    players.put(username, player);
                    remove = true;
                    Log.info("Player connected to game: " + username + " [" + players.size() + "/" + details.getMaxPlayers() + "]");
                    
                    KeyPair keyPair = PacketEncryption.createRSAKeyPair();
                    RSAEncryptionPacket rsaPacket = new RSAEncryptionPacket(keyPair.getPublic());
                    player.sendPacket(rsaPacket);

                    synchronizeGameWithPlayer(player);
                }  
                return;
                // If they send a GameInfoRequestPacket, send them information about the server.
            }
            @Override
            public boolean shouldRemoveListener() {
                return remove;
            }            
        });
        connectingStreams.add(clientRecieverStream);
    }

    public void onPlayerDisconnect(GamePlayer player) {
        // Remove the player from the game.
        players.remove(player.getUsername());
        Log.info("Player disconnected from game: \"" + player.getUsername() + "\" [" + players.size() + "/" + details.getMaxPlayers() + "]");
        PlayerPacket packet = new PlayerPacket(player.getUsername(), PlayerPacket.PlayerPacketType.DISCONNECTING);
        for (GamePlayer p : players.values()) {
            // Send a packet to all other players to show that the player has disconnected.
            p.sendPacket(packet);
        }
    }

    public void start() {
        // Start the game manager.
        if (running) {
            return;
        }
        if (save == null) {
            throw new IllegalStateException("The game manager has not been initialized.");
        }
        if (details.isActingAsServer()) {
            // Start the server
            server.start();
            try {
                server.bind(details.getTCPPort());
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            Log.info("Server started on port " + details.getTCPPort());
        }

        running = true;
        Thread updateThread = new Thread(() -> {
            long lastTime = System.nanoTime();
            while (running) {
                long currentTime = System.nanoTime();
                float delta = (currentTime - lastTime) / 1000000000.0f;
                lastTime = currentTime;

                // To prevent any floating point issues, sleep for a few milliseconds if the delta is less than 0.001f.
                if (delta < 0.001f) {
                    try {
                        Thread.sleep((long) 5);
                        delta += 0.05f;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                
                update(delta);
            }
        });
        updateThread.start();
        Log.info("Game started.");

        if (details.isActingAsServer()) {
            // Start the server
            return;
        }
    }

    public void stop() {
        // Stop the game manager.
        if (!running) {
            return;
        }
        Log.info("Game stopping...");

        for (GamePlayer player : players.values()) {
            player.kick(GamePlayer.SERVER_CLOSING);
        }

        running = false;
        if (details.isActingAsServer()) {
            // Stop the server
            return;
        }
        dispose();
    }

    /**
     * Initialize the game manager.
     * @param save The save file to load from. (A game save with nothing saved can still be passed).
     */
    public void initialize(GameSave save) {
        Objects.requireNonNull(save, "The save file cannot be null.");
        this.save = save;
        load(save);

        Log.info("Game \"" + save.getName() + "\" initialized.");
    }

    /**
     * Save the game state.
     */
    public void save() {
        // Save the game state.
        FileHandle saveFolder = null;
        if (RetroTowerDefense.instanceExists()) {
            saveFolder = save.getSaveFolder(FileType.External);
        } else {
            saveFolder = save.getSaveFolder(FileType.Absolute);
        }
        saveFolder.mkdirs();
    }

    /**
     * Loads the game from file.
     * If there is no save, then a new game will be created.
     * If there is a save, then the game will be loaded from the save.
     * @param save The save file to load from.
     * @return true if a save was loaded, false if no save was loaded.
     */
    public boolean load(GameSave save) {
        // Load the game state.
        FileHandle saveFolder = null;
        if (RetroTowerDefense.instanceExists()) {
            saveFolder = save.getSaveFolder(FileType.External);
        } else {
            saveFolder = save.getSaveFolder(FileType.Absolute);
        }

        if (!saveFolder.exists()) {
            // Create a default game
            initializeNewGame();
            return false;
        }
        // Load the game from the save
        

        return true;
    }

    public void initializeNewGame() {
        // Initialize a new game.
        world = new World();
    }

    public void synchronizeGameWithPlayer(GamePlayer player) {
        // Send the world data to the player.
        player.sendPacket(new GameObjectPacket(world));
    }
    
    public void update(float delta) {
        // Update connections
        Iterator<GameStream> it = connectingStreams.iterator();
        while (it.hasNext()) {
            GameStream connectingStream = it.next();
            connectingStream.update();
            for (GamePlayer player : players.values()) {
                if (player.getStream().equals(connectingStream)) {
                    it.remove();
                    break;
                }
            }
        }

        Iterator<GamePlayer> it2 = players.values().iterator();
        while (it2.hasNext()) {
            GamePlayer player = it2.next();
            player.update(delta);
            if (player.getStream().isClosed()) {
                it2.remove();
            }
        }
    }

    public void dispose() {
        // Dispose of the game manager.

    }

    public boolean isRunning() { return running; }

    /**
     * The details of the game manager.
     */
    public static class GameManagerDetails {
        private int maxPlayers = 4;
        private int tcpPort = 11265;
        private boolean actAsServer = false;

        public GameManagerDetails() {

        }

        public void actAsServer(boolean actAsServer) { this.actAsServer = actAsServer; }
        public boolean isActingAsServer() { return actAsServer; }
        public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
        public int getMaxPlayers() { return maxPlayers; }
        public void setTCPPort(int tcpPort) { this.tcpPort = tcpPort; }
        public int getTCPPort() { return tcpPort; }
    }

}
