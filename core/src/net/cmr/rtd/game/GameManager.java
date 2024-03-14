package net.cmr.rtd.game;

import java.io.DataInputStream;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.io.input.CloseShieldInputStream;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.DataBuffer;
import com.badlogic.gdx.utils.Disposable;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import net.cmr.rtd.RetroTowerDefense;
import net.cmr.rtd.game.commands.CommandHandler;
import net.cmr.rtd.game.packets.AESEncryptionPacket;
import net.cmr.rtd.game.packets.ConnectPacket;
import net.cmr.rtd.game.packets.DisconnectPacket;
import net.cmr.rtd.game.packets.GameObjectPacket;
import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.packets.PacketEncryption;
import net.cmr.rtd.game.packets.PasswordPacket;
import net.cmr.rtd.game.packets.PlayerPacket;
import net.cmr.rtd.game.packets.RSAEncryptionPacket;
import net.cmr.rtd.game.packets.StatsUpdatePacket;
import net.cmr.rtd.game.packets.WavePacket;
import net.cmr.rtd.game.stream.GameStream;
import net.cmr.rtd.game.stream.GameStream.PacketListener;
import net.cmr.rtd.game.stream.LocalGameStream;
import net.cmr.rtd.game.stream.OnlineGameStream;
import net.cmr.rtd.game.world.EnemyFactory.EnemyType;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.TeamData;
import net.cmr.rtd.game.world.TeamData.NullTeamException;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.game.world.entities.Player;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.entities.towers.FireTower;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.waves.Wave;
import net.cmr.rtd.waves.WaveUnit;
import net.cmr.rtd.waves.WavesData;
import net.cmr.util.Log;

/**
 * This class can act like a server for a multiplayer game or a handler for a single player game.
 * It will handle the game logic, game state, and updating/sending necesary information to the client.
 */
public class GameManager implements Disposable {

    public static final int MAX_TEAMS = 4;

    private final GameManagerDetails details;
    private final ConcurrentHashMap<String, GamePlayer> players = new ConcurrentHashMap<String, GamePlayer>();
    private final ArrayList<GameStream> connectingStreams = new ArrayList<GameStream>();
    private final Server server;
    private boolean running = false;
    private GameSave save = null;

    private UpdateData data;
    private World world;
    private ArrayList<TeamData> teams;
    private boolean pauseWaves = true;

    /**
     * Create a new game manager.
     * After this is called, {@link #initialize(GameSave)} should be called to initialize the game.
     * @param details
     */
    public GameManager(GameManagerDetails details) {
        this.details = details;
        this.data = new UpdateData(this);
        if (details.isActingAsServer()) {
            // Create server objects
            server = new Server();
            // Register packets
            OnlineGameStream.registerPackets(server.getKryo());
            server.addListener(new Listener() {
                @Override
                public void connected(Connection arg0) {
                    OnlineGameStream stream = new OnlineGameStream(new PacketEncryption(), arg0);
                    onNewConnection(stream);
                }
            });
            // Prepare server
            return;
        }
        server = null;
    }

    /**
     * This method should be called when a player connects to the server.
     * It will add the player to the game, establish encryption, validate if
     * the player has the correct password, and send the player the world data.
     * @param clientRecieverStream
     * If it is of type {@link LocalGameStream}, then the argument passed should be the stream that recieves packets from the client, NOT the client sender.
     * If it is of type {@link OnlineGameStream}, then the argument passed should be a new online stream derived from the connection of a new client. 
     */
    public void onNewConnection(GameStream clientRecieverStream) {
        Log.debug("A new player may want to join...", clientRecieverStream);
        clientRecieverStream.addListener(new PacketListener() {
            boolean remove = false;
            GamePlayer player = null;

            @Override
            public void packetReceived(Packet packet) {
                // When a player sends a ConnectPacket, set their username and put them in the players map.
                if (packet instanceof ConnectPacket) {
                    ConnectPacket connectPacket = (ConnectPacket) packet;
                    // If it's full, send a disconnect packet.
                    if (players.size() >= details.getMaxPlayers()) {
                        // The game is full.
                        clientRecieverStream.sendPacket(new DisconnectPacket(GamePlayer.GAME_FULL));
                        return;
                    }
                    // If the username is too long, send a disconnect packet.
                    String username = connectPacket.username;
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
                    Log.info("Player joining the game... " + username);
                    GamePlayer player = new GamePlayer(GameManager.this, clientRecieverStream, username);
                    player.setTeam(connectPacket.team);
                    players.put(username, player);
                    this.player = player;

                    KeyPair keyPair = PacketEncryption.createRSAKeyPair();
                    RSAEncryptionPacket rsaPacket = new RSAEncryptionPacket(keyPair.getPublic());
                    player.sendPacket(rsaPacket);
                    return;
                }  

                if (packet instanceof RSAEncryptionPacket) {
                    // Set the RSA public key.
                    RSAEncryptionPacket rsaPacket = (RSAEncryptionPacket) packet;
                    PublicKey publicKey = PacketEncryption.publicKeyFromBytes(rsaPacket.RSAData);
                    if (clientRecieverStream instanceof OnlineGameStream) {
                        OnlineGameStream onlineStream = (OnlineGameStream) clientRecieverStream;
                        onlineStream.getEncryptor().setRSAPublic(publicKey);
                    }
                    return;
                }
                
                if (packet instanceof AESEncryptionPacket) {
                    // Set the AES data.
                    AESEncryptionPacket aesPacket = (AESEncryptionPacket) packet;
                    SecretKey secretKey = PacketEncryption.secretKeyFromBytes(aesPacket.AESData);
                    IvParameterSpec iv = PacketEncryption.ivFromBytes(aesPacket.IVData);
                    if (clientRecieverStream instanceof OnlineGameStream) {
                        OnlineGameStream onlineStream = (OnlineGameStream) clientRecieverStream;
                        onlineStream.getEncryptor().setAESData(secretKey, iv);
                    }
                    // Encryption has been established.
                    if (!details.usePassword()) {
                        onPlayerJoin(player);
                        remove = true;
                    }
                    return;
                }

                if (packet instanceof PasswordPacket) {
                    // If the password is correct, then let the player join.
                    if (details.getPassword().equals(((PasswordPacket) packet).getPassword())) {
                        onPlayerJoin(player);
                        remove = true;
                    } else {
                        // If the password is incorrect, send a disconnect packet.
                        player.kick(GamePlayer.PASSWORD_INCORRECT);
                        remove = true;
                    }
                    return;
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

    /**
     * This is called after encryption has been established and (optionally)
     * the player has sent the correct password.
     */
    public void onPlayerJoin(GamePlayer player) {
        Log.info("Player joined game: " + player.getUsername() + " [" + players.size() + "/" + details.getMaxPlayers() + "]");

        // Add the player to the world
        Player playerEntity = world.getPlayer(player);
        player.setPlayer(playerEntity);

        // Send the world data to the player.
        synchronizeWorld(player);

        world.addEntity(playerEntity);
    }

    public void onPlayerDisconnect(GamePlayer player) {
        Log.info("Player disconnected from game: \"" + player.getUsername() + "\" [" + players.size() + "/" + details.getMaxPlayers() + "]");
        
        // Remove the player from the world
        players.remove(player.getUsername());
        Player playerEntity = player.getPlayer();
        if (playerEntity != null) {
            world.removeEntity(playerEntity);
        }

        // Send a packet to all other players to show that the player has disconnected.
        PlayerPacket packet = new PlayerPacket(player.getUsername(), 0, 0, PlayerPacket.PlayerPacketType.DISCONNECTING);
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

        if (details.useConsole()) {
            openConsole();
        }
        
        Thread updateThread = new Thread(() -> {
            long lastTime = System.nanoTime();
            while (running) {
                long currentTime = System.nanoTime();
                float delta = (currentTime - lastTime) / 1e9f;
                lastTime = currentTime;

                // To prevent any floating point issues, sleep for a few milliseconds if the delta is less than 0.001f.
                if (delta < 0.001f) {
                    try {
                        Thread.sleep((long) 5);
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
        save();

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

    private void openConsole() {
        CommandHandler.register(new CommandHandler() {
            @Override
            public void handleCommand(String command, String[] args) {
                if (command.equals("exit")) {
                    Log.info("Recieved command: exit. Closing server...");
                    stop();
                    return;
                }
            }
        });
        Thread consoleThread = new Thread(() -> {
            CloseShieldInputStream shield = CloseShieldInputStream.wrap(System.in);
            Scanner scanner = new Scanner(shield);
            
            while (isRunning()) {
                String input = scanner.nextLine();
                CommandHandler.handleCommand(input);
            }

            scanner.close();
        });
        consoleThread.start();
    }

    /**
     * Initialize the game manager.
     * @param save The save file to load from. (A game save with nothing saved can still be passed).
     */
    public void initialize(GameSave save) {
        Objects.requireNonNull(save, "The save file cannot be null.");
        this.save = save;
        System.out.println(save.getSaveFolder().file().getAbsolutePath());
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

        // Save the world
        if (world != null) {
            byte[] data = GameObject.serializeGameObject(world);
            FileHandle worldFile = saveFolder.child("world.dat");
            worldFile.writeBytes(data, false);
            Log.info("Saved world to " + worldFile.path());
        }

        // Save the players
        if (world != null) {
            try {
                FileHandle playersFile = saveFolder.child("players.dat");
                DataBuffer buffer = new DataBuffer();
                world.serializePlayerData(buffer);
                playersFile.writeBytes(buffer.toArray(), false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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
            save();
            return false;
        }

        // Load the game from the save
        FileHandle worldFile = saveFolder.child("world.dat");
        if (worldFile.exists()) {
            byte[] data = worldFile.readBytes();
            world = (World) GameObject.deserializeGameObject(data);
            Log.info("Loaded world from " + worldFile.path());
        } else {
            initializeNewGame();
        }

        // Load the wave data from the save
        FileHandle wavesDataFile = saveFolder.child("wave.json");
        if (wavesDataFile.exists()) {
            // Load the waves
            WavesData wavesData = WavesData.load(wavesDataFile);
            world.setWavesData(wavesData);
        } else {
            WavesData wavesData = new WavesData();
            Wave wave = new Wave(1000);
            wave.addWaveUnit(new WaveUnit(wave, EnemyType.BASIC_ONE, 1000));
            wavesData.waves.put(1, wave);
            world.setWavesData(wavesData);
        }
        
        this.teams = new ArrayList<TeamData>(MAX_TEAMS);
        for (int i = 0; i < MAX_TEAMS; i++) {
            try {
                teams.add(new TeamData(world, i));
            } catch (NullTeamException e) {
                // This team does not exist in the world.
                System.out.println("TEAM DOES NOT EXIST: " + e.getMessage());
            }
        }
        if (teams.size() == 0) {
            throw new IllegalStateException("No teams were found in the world. (A start and end point is required for each team)");
        } else {
            Log.info("Loaded " + teams.size() + " teams from the world.");
        }

        FileHandle playersFile = saveFolder.child("players.dat");
        if (playersFile.exists()) {
            try {
                DataInputStream stream = new DataInputStream(playersFile.read());
                world.deserializePlayerData(stream);
                Log.info("Loaded players from " + playersFile.path());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public void sendPacketToAll(Packet packet) {
        for (GamePlayer player : players.values()) {
            player.sendPacket(packet);
        }
    }

    public void initializeNewGame() {
        // Initialize a new game.
        world = new World();
        FireTower tower = new FireTower(0);
        tower.setPosition(4 * Tile.SIZE, 2 * Tile.SIZE);

        world.addEntity(tower);
    }

    public void synchronizeWorld(GamePlayer player) {
        // Send the world data to the player.
        player.sendPackets(retrieveWorldSnapshot());
        sendStatsUpdatePacket(player);
        sendWaveData(player, world);
    }

    public GamePlayer getPlayer(Player player) {
        return players.get(player.getName());
    }

    public void updateTeamStats(int team) {
        for (GamePlayer player : players.values()) {
            if (player.getTeam() == team) {
                sendStatsUpdatePacket(player);
            }
        }
    }

    public void sendStatsUpdatePacket(GamePlayer player) {
        if (player.getPlayer() == null) return;
        TeamData team = teams.get(player.getTeam());
        StatsUpdatePacket packet = new StatsUpdatePacket(player.getPlayer().getHealth(), team.getMoney(), team.getHealth());
        System.out.println("Sending stats packet from server: "+packet);
        player.sendPacket(packet);
    }

    public void sendStatsUpdatePacket(Player player) {
        GamePlayer gamePlayer = getPlayer(player);
        if (gamePlayer == null) return;
        sendStatsUpdatePacket(gamePlayer);
    }

    private WavePacket getCurrentWavePacket() {
        int wave = world.getWave();
        float waveCountdown = world.getWaveCountdown();
        float waveDuration = world.getCurrentWaveDuration();

        return new WavePacket(areWavesPaused(), waveCountdown, waveDuration, wave);
    }

    public void sendWaveData(GamePlayer player, World world) {
        player.sendPacket(getCurrentWavePacket());
    }

    public void sendWaveUpdateToAll() {
        WavePacket packet = getCurrentWavePacket();
        for (GamePlayer player : players.values()) {
            player.sendPacket(packet);
        }
    }

    public List<Packet> retrieveWorldSnapshot() {
        ArrayList<Packet> packets = new ArrayList<Packet>();
        packets.add(new GameObjectPacket(world));
        for (GamePlayer player : players.values()) {
            packets.add(new PlayerPacket(player.getUsername(), player.getPlayer().getX(), player.getPlayer().getY(), PlayerPacket.PlayerPacketType.INITIALIZE_WORLD));
        }
        return packets;
    }

    public TeamData getTeam(int team) {
        return teams.get(team);
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

        // Update the world
        if (world != null) {
            world.update(delta, data);
        }
    }
    float time = 0;

    public void dispose() {
        // Dispose of the game manager.

    }

    public void pauseWaves() {
        this.pauseWaves = true;
        sendWaveUpdateToAll();
    }
    public void resumeWaves() {
        this.pauseWaves = false;
        sendWaveUpdateToAll();
    }
    public boolean areWavesPaused() {
        return pauseWaves;
    }

    /**
     * Place a tower at the specified tile.
     * @param tower The tower to place.
     * @param tileX The x position of the tile.
     * @param tileY The y position of the tile.
     * @return true if the tower was placed, false if the tower was not placed.
     */
    public boolean placeTower(TowerEntity tower, int tileX, int tileY) {
        // If there's a tower there, don't do anything
        for (Entity entity : world.getEntities()) {
            if (entity instanceof TowerEntity) {
                TowerEntity other = (TowerEntity) entity;
                if (other.getX() == tileX * Tile.SIZE && other.getY() == tileY * Tile.SIZE) {
                    return false;
                }
            }
        }
        // Place the tower
        tower.setPosition((tileX + .5f) * Tile.SIZE, (tileY + .5f) * Tile.SIZE);
        
        // Place it in the world and, as a result, send the packet to all players
        world.addEntity(tower);
        return true;
    }

    public boolean isRunning() { return running; }
    public GameManagerDetails getDetails() { return details; }
    public FileHandle getSaveFolder() {
        if (RetroTowerDefense.instanceExists()) {
            return save.getSaveFolder(FileType.External);
        } else {
            return save.getSaveFolder(FileType.Absolute);
        }
    }

    /**
     * The details of the game manager.
     */
    public static class GameManagerDetails {
        private int maxPlayers = 4;
        private int tcpPort = 11265;
        private boolean actAsServer = false;
        private String password = null;
        private boolean console;

        public GameManagerDetails() {

        }

        public void actAsServer(boolean actAsServer) { this.actAsServer = actAsServer; }
        public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
        public void setTCPPort(int tcpPort) { this.tcpPort = tcpPort; }
        public void setPassword(String password) { this.password = password; }
        public void setUseConsole(boolean console) { this.console = console; }
        

        public boolean isActingAsServer() { return actAsServer; }
        public int getMaxPlayers() { return maxPlayers; }
        public int getTCPPort() { return tcpPort; }
        public String getPassword() { return password; }
        public boolean usePassword() { return password != null; }
        public boolean useConsole() { return console; }
    }

    public World getWorld() {
        return world;
    }

    public ArrayList<TeamData> getTeams() { 
        return teams;
    }

}
