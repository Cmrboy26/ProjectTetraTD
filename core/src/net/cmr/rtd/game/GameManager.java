package net.cmr.rtd.game;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import net.cmr.rtd.ProjectTetraTD;
import net.cmr.rtd.game.commands.CommandHandler;
import net.cmr.rtd.game.files.QuestFile;
import net.cmr.rtd.game.packets.AESEncryptionPacket;
import net.cmr.rtd.game.packets.ConnectPacket;
import net.cmr.rtd.game.packets.DisconnectPacket;
import net.cmr.rtd.game.packets.GameInfoPacket;
import net.cmr.rtd.game.packets.GameObjectPacket;
import net.cmr.rtd.game.packets.GameOverPacket;
import net.cmr.rtd.game.packets.GameResetPacket;
import net.cmr.rtd.game.packets.GameSpeedChangePacket;
import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.packets.PacketEncryption;
import net.cmr.rtd.game.packets.PasswordPacket;
import net.cmr.rtd.game.packets.PlayerPacket;
import net.cmr.rtd.game.packets.PlayerPositionsPacket;
import net.cmr.rtd.game.packets.RSAEncryptionPacket;
import net.cmr.rtd.game.packets.SetPlayerShopPacket;
import net.cmr.rtd.game.packets.StatsUpdatePacket;
import net.cmr.rtd.game.packets.TeamUpdatePacket;
import net.cmr.rtd.game.packets.WavePacket;
import net.cmr.rtd.game.stream.GameStream;
import net.cmr.rtd.game.stream.GameStream.PacketListener;
import net.cmr.rtd.game.stream.LocalGameStream;
import net.cmr.rtd.game.stream.OnlineGameStream;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject.GameType;
import net.cmr.rtd.game.world.TeamData;
import net.cmr.rtd.game.world.TeamData.NullTeamException;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.game.world.entities.Player;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.screen.TutorialScreen;
import net.cmr.rtd.waves.Wave;
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
    private QuestFile quest = null;

    private UpdateData data;
    private World world;
    private float gameSpeed = 1.0f;
    private ArrayList<TeamData> teams;
    private ArrayList<TeamData> winningTeams = new ArrayList<TeamData>();
    private Stack<TeamData> teamWinOrder = new Stack<TeamData>();  
    private boolean pauseWaves = true;

    public static final int WRITE_BUFFER_SIZE = 16384 * 4;
    public static final int READ_BUFFER_SIZE = 2048 * 4;

    /**
     * Create a new game manager.
     * After this is called, {@link #initialize(GameSave)} should be called to initialize the game.
     * @param details
     */
    public GameManager(GameManagerDetails details) {
        this.details = details;
        this.data = new UpdateData(this);
        if (details.isHostedOnline()) {
            // Create server objects
            server = new Server(WRITE_BUFFER_SIZE, READ_BUFFER_SIZE);
            // Register packets
            OnlineGameStream.registerPackets(server.getKryo());
            server.addListener(new Listener() {
                @Override
                public void connected(Connection arg0) {
                    OnlineGameStream stream = new OnlineGameStream(new PacketEncryption(), arg0);
                    arg0.setBufferPositionFix(true);
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
                if (packet instanceof GameInfoPacket) {
                    // Send the game info to the player.
                    ArrayList<Integer> teams = new ArrayList<Integer>();
                    for (TeamData team : GameManager.this.teams) {
                        teams.add(team.team);
                    }
                    int[] teamsArray = new int[teams.size()];
                    for (int i = 0; i < teams.size(); i++) {
                        teamsArray[i] = teams.get(i);
                    }
                    GameInfoPacket gameInfo = new GameInfoPacket(teamsArray, players.size(), details.getMaxPlayers(), details.usePassword());
                    clientRecieverStream.sendPacket(gameInfo);
                    clientRecieverStream.sendPacket(new DisconnectPacket(GamePlayer.QUIT));
                    remove = true;
                    return;
                }

                // When a player sends a ConnectPacket, set their username and put them in the players map.
                if (packet instanceof ConnectPacket) {
                    ConnectPacket connectPacket = (ConnectPacket) packet;
                    // If it's full, send a disconnect packet.
                    if (players.size() >= details.getMaxPlayers()) {
                        // The game is full.
                        clientRecieverStream.sendPacket(new DisconnectPacket(GamePlayer.GAME_FULL));
                        remove = true;
                        return;
                    }
                    // If the username is too long, send a disconnect packet.
                    String username = connectPacket.username;
                    if (username.length() > GamePlayer.USERNAME_LENGTH) {
                        // The username is too long.
                        clientRecieverStream.sendPacket(new DisconnectPacket(GamePlayer.USERNAME_TOO_LONG));
                        remove = true;
                        return;
                    }
                    // If the username is already taken, send a disconnect packet.
                    if (players.containsKey(username)) {
                        // The username is already taken.
                        clientRecieverStream.sendPacket(new DisconnectPacket(GamePlayer.USERNAME_TAKEN));
                        remove = true;
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
                        Log.info("Password is CORRECT!");
                        onPlayerJoin(player);
                        remove = true;
                    } else {
                        // If the password is incorrect, send a disconnect packet.
                        Log.info("Password is INCORRECT!");
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
        sendPacketToAll(new PlayerPacket(player.getUsername(), player.getPlayer().getX(), player.getPlayer().getY(), PlayerPacket.PlayerPacketType.CONNECTING));
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

    public GameType[] getBuyableTowers() {
        WavesData data = world.getWavesData();
        return data.getBuyableTowers();
    }

    public void start() {
        // Start the game manager.
        if (running) {
            return;
        }
        if (quest == null) {
            throw new IllegalStateException("The game manager has not been initialized.");
        }
        if (details.isHostedOnline()) {
            // Attempt to port forward
            attemptPortForward();
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
        updateThread.setDaemon(true);
        updateThread.start();
        Log.info("Game started.");

        if (details.isHostedOnline()) {
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
        if (details.isHostedOnline()) {
            // Stop the server
            server.stop();
            removePortForward();
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
     * @param save The save file to load from
     */
    public void initialize(QuestFile quest) {
        Objects.requireNonNull(quest, "The save file cannot be null.");
        this.quest = quest;
        load(quest);

        Log.info("Game \"" + quest.toString() + "\" initialized.");
    }

    /**
     * Save the game state.
     */
    public void save() {
        // If the game is a singleplayer game, add the resume button to the main menu.
        Log.info("Wave: "+getWorld().getWave()+", Total Waves: "+getWorld().getWavesData().getTotalWaves());
        if (ProjectTetraTD.instanceExists() && !details.isHostedOnline() && (getWorld().getWave() < getWorld().getWavesData().getTotalWaves() || getWorld().getWavesData().endlessMode)) {
            ProjectTetraTD instance = ProjectTetraTD.getInstance(ProjectTetraTD.class);
            String[] serializedQuestFile = quest.serialize();
            if (!(instance.getScreen() instanceof TutorialScreen)) {
                instance.setLastPlayedQuest(serializedQuestFile);
            }
        } else {
            ProjectTetraTD instance = ProjectTetraTD.getInstance(ProjectTetraTD.class);
            instance.setLastPlayedQuest(null);
        }
        // Save the game state.
        try {
            quest.saveGame(world);
            Log.info("Saved game successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteSave() {
        quest.deleteSave();
    }

    /**
     * Loads the game from file.
     * If there is no save, then a new game will be created.
     * If there is a save, then the game will be loaded from the save.
     * @param save The save file to load from.
     * @return true if a save was loaded, false if no save was loaded.
     */
    public boolean load(QuestFile quest) {
        // Load the game state.
        FileHandle saveFolder = quest.getSaveFolder();
        if (!saveFolder.exists()) {
            // Create a default game
            initializeNewGame();
            save();
            return false;
        }

        World readWorld = quest.loadWorld();
        if (readWorld == null) {
            initializeNewGame();
        } else {
            world = readWorld;
        }

        WavesData wavesData = quest.loadWavesData();
        if (wavesData == null) {
            throw new IllegalStateException("No waves data was found in the save.");
        }
        world.setWavesData(wavesData);

        
        this.teams = new ArrayList<TeamData>(MAX_TEAMS);
        for (int i = 0; i < MAX_TEAMS; i++) {
            try {
                TeamData data = new TeamData(world, i);
                teams.add(data);
                if (data.getHealth() > 0) {
                    winningTeams.add(data);
                }
            } catch (NullTeamException e) {
                // This team does not exist in the world.
                //System.out.println(e.getMessage());
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

        for (GamePlayer player : players.values()) {
            //world.addEntity(player.getPlayer());
        }
        /*for (GamePlayer player : players.values()) {
            player.sendPacket(new GameObjectPacket(world));
            sendPacketToAll(new PlayerPacket(player.getUsername(), player.getPlayer().getX(), player.getPlayer().getY(), PlayerPacket.PlayerPacketType.INITIALIZE_WORLD));
            sendWaveData(player, world);
        }*/
        for (GamePlayer player : players.values()) {
            onPlayerJoin(player);
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
    }

    public void synchronizeWorld(GamePlayer player) {
        // Send the world data to the player.
        player.sendPackets(retrieveWorldSnapshot());
        sendStatsUpdatePacket(player);
        sendWaveData(player, world);
        setGameSpeed(gameSpeed);
        SetPlayerShopPacket shopPacket = new SetPlayerShopPacket(getBuyableTowers());
        player.sendPacket(shopPacket);
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
        StatsUpdatePacket packet = new StatsUpdatePacket(player.getPlayer().getHealth(), team.getInventory(), team.getHealth());
        player.sendPacket(packet);
    }

    public void sendStatsUpdatePacket(Player player) {
        GamePlayer gamePlayer = getPlayer(player);
        if (gamePlayer == null) return;
        sendStatsUpdatePacket(gamePlayer);
    }

    private WavePacket getCurrentWavePacket() {
        Wave currentWave = world.getCurrentWave();
        int wave = world.getWave();
        float waveCountdown = world.getWaveCountdown();
        float waveDuration = world.getCurrentWaveDuration();
        boolean warn = false;
        if (currentWave != null) {
            warn = currentWave.shouldWarnPlayer();
        }
        
        return new WavePacket(areWavesPaused(), waveCountdown, waveDuration, wave, warn);
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

        ArrayList<GamePlayer> playerpositions = new ArrayList<>();
        Iterator<GamePlayer> it2 = players.values().iterator();
        while (it2.hasNext()) {
            GamePlayer player = it2.next();
            player.update(delta);
            if (player.getStream().isClosed()) {
                it2.remove();
            } else {
                playerpositions.add(player);
            }
        }
        // CRASHES AFTER A BIT OF TIME
        /*
         * Exception in thread "Thread-2" java.nio.BufferOverflowException
                at java.nio.HeapByteBuffer.put(Unknown Source)
                at com.esotericsoftware.kryo.io.ByteBufferOutputStream.write(ByteBufferOutputStream.java:60)
                at com.esotericsoftware.kryo.io.Output.flush(Output.java:207)
                at com.esotericsoftware.kryonet.KryoSerialization.write(KryoSerialization.java:51)
                at com.esotericsoftware.kryonet.TcpConnection.send(TcpConnection.java:192)
                at com.esotericsoftware.kryonet.Connection.sendTCP(Connection.java:59)
                at net.cmr.rtd.game.stream.OnlineGameStream.sendPacket(OnlineGameStream.java:80)
                at net.cmr.rtd.game.GamePlayer.sendPacket(GamePlayer.java:114)
                at java.lang.Thread.run(Unknown Source)
         */
        PlayerPositionsPacket packet = new PlayerPositionsPacket(playerpositions);
        sendPacketToAll(packet);

        // Update the world
        if (world != null) {
            world.update(gameSpeed, delta, data);
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
        if (world != null && world.passedAllWaves()) {
            world.resetWaveCounter();   
        }
        sendWaveUpdateToAll();
    }
    public boolean areWavesPaused() {
        return pauseWaves;
    }

    public void teamLost(int team) {
        // TODO: If a player is playing a multiplayer map by themselves, they get a notification when the other team with no one loses. Fix this. 

        if (winningTeams.size() == 0) {
            // Allow the players to continue playing if they decide to resume the game
            return;
        }

        int teamsWithPlayers = 0;
        for (TeamData winningTeams : teams) {
            if (doesTeamHavePlayers(winningTeams.team)) {
                teamsWithPlayers++;
            }
        }

        // Only send wave data info about teams with players in them
        boolean teamHasPlayers = doesTeamHavePlayers(team);
        if (teamHasPlayers) {
            TeamUpdatePacket packet = new TeamUpdatePacket(team, true);
            sendPacketToAll(packet);
        }

        winningTeams.remove(teams.get(team));
        if (teamHasPlayers) {
            teamWinOrder.push(teams.get(team));
        }

        if (teamsWithPlayers > 1) {
            // Show all players in the multiplayer game the winner
            if (winningTeams.size() == 1) {
                gameOver();
            }
        } else {
            // Show the player in the single player game that they lost
            if (winningTeams.size() == 0) {
                gameOver();
            }
        }
    }
    public void gameOver() {
        // There are no more waves in the level OR there is only one team remaining in the multiplayer match OR the player lost in the single player match.
        // Send a packet to all players to show that the game is over.
        // TODO: Find out why the game sometimes sends the game over 4 teams participating in a singleplayer games.
        Log.info("GAME OVER!");
        for (TeamData data : winningTeams) {
            sendPacketToAll(new TeamUpdatePacket(data.team, false));
        }

        /*for (TeamData data : winningTeams) {
            if (doesTeamHavePlayers(data.team)) {
                teamWinOrder.push(data);
            }
        }

        int[] teamWinOrder = new int[this.teamWinOrder.size()];
        for (int i = 0; i < this.teamWinOrder.size(); i++) {
            teamWinOrder[i] = this.teamWinOrder.pop().team;
            System.out.println(teamWinOrder[i]);
        }*/

        for (TeamData data : teams) {
            GameOverPacket packet = new GameOverPacket(world.getWave(), data.getScore(), data.getHealth() > 0, new int[0]);
            for (GamePlayer player : players.values()) {
                if (player.getTeam() == data.team) {
                    player.sendPacket(packet);
                }
            }
        }
        winningTeams.clear();
        pauseWaves();
    }

    int autoSaveCounter = 0;

    public void onWaveChange(int newWave, Wave waveObj) {
        // Send the current wave data to the clients.
        sendWaveUpdateToAll();
        // Save the game every 5 waves.
        autoSaveCounter++;
        if (autoSaveCounter >= 5) {
            autoSaveCounter = 0;
            save();
        }
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

    public boolean doesTeamHavePlayers(int team) {
        for (GamePlayer player : players.values()) {
            if (player.getTeam() == team) {
                return true;
            }
        }
        return false;
    }

    public void setGameSpeed(float speed) {
        if (speed > 0) {
            this.gameSpeed = speed;
            sendPacketToAll(new GameSpeedChangePacket(speed));
        }
    }
    public float getGameSpeed() {
        return gameSpeed;
    }

    public boolean isRunning() { return running; }
    public GameManagerDetails getDetails() { return details; }
    public QuestFile getQuest() { return quest; }

    public void resetWorld() {
        quest.createSave(); // Resets the save file
        sendPacketToAll(new GameResetPacket());
        pauseWaves();
        load(quest);
    }

    /**
     * The details of the game manager.
     */
    public static class GameManagerDetails {
        private int maxPlayers = 4;
        private int tcpPort = 11265;
        private boolean hostedOnline = false;
        private String password = null;
        private boolean console;
        private boolean useUPNP = false;

        public GameManagerDetails() {

        }

        public void setHostedOnline(boolean hostedOnline) { this.hostedOnline = hostedOnline; }
        public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
        public void setTCPPort(int tcpPort) { this.tcpPort = tcpPort; }
        public void setPassword(String password) { this.password = password; }
        public void setUseConsole(boolean console) { this.console = console; }
        public void setUseUPNP(boolean useUPNP) { this.useUPNP = useUPNP; }

        public boolean isHostedOnline() { return hostedOnline; }
        public int getMaxPlayers() { return maxPlayers; }
        public int getTCPPort() { return tcpPort; }
        public String getPassword() { return password; }
        public boolean usePassword() { return password != null && !password.isEmpty(); }
        public boolean useConsole() { return console; }
        public boolean useUPNP() { return useUPNP; }
    }

    public World getWorld() {
        return world;
    }

    public ArrayList<TeamData> getTeams() { 
        return teams;
    }

    public UpdateData getUpdateData() {
        return data;
    }

    public Collection<GamePlayer> getPlayers() {
        return players.values();
    }

    public ArrayList<TowerEntity> getTowers(int team) {
        ArrayList<TowerEntity> towers = new ArrayList<TowerEntity>();
        for (Entity entity : world.getEntities()) {
            if (entity instanceof TowerEntity) {
                TowerEntity tower = (TowerEntity) entity;
                if (tower.getTeam() == team) {
                    towers.add(tower);
                }
            }
        }
        return towers;
    }

    GatewayDiscover discover;

    public boolean attemptPortForward() {
        if (!details.useUPNP()) {
            return false;
        }
        try {
            discover = new GatewayDiscover();
            discover.discover();
            GatewayDevice d = discover.getValidGateway();
            if (d == null) {
                Log.warning("Failed to port forward on port: "+details.getTCPPort()+": Device not detected! A manual port forward may be required.");
                return false;
            }
            InetAddress localAddress = d.getLocalAddress();
            String externalIPAddress = d.getExternalIPAddress();
            int port = details.getTCPPort();
            PortMappingEntry entry = new PortMappingEntry();
            if(!d.getSpecificPortMappingEntry(port, "TCP", entry)) {
                Log.info("Port already forwarded: "+details.getTCPPort());
            } else {
                boolean portMappingSuccessful = d.addPortMapping(details.getTCPPort(), details.getTCPPort(), localAddress.getHostAddress(), "TCP", "RTD");
                if (portMappingSuccessful) {
                    Log.info("Port forwarding successful: "+externalIPAddress+":"+details.getTCPPort());
                } else {
                    Log.warning("Failed to port forward on port: "+details.getTCPPort()+": Mapping unsuccessful! A manual port forward may be required.");
                }
                return true;
            }
            /*boolean portMappingSuccessful = d.addPortMapping(details.getTCPPort(), details.getTCPPort(), localAddress.getHostAddress(), "TCP", "RTD");
            if (portMappingSuccessful) {
                Log.info("Port forwarding successful: "+externalIPAddress+":"+details.getTCPPort());
            }
            if (!portMappingSuccessful) {
                Log.warning("Failed to port forward on port: "+details.getTCPPort()+": Mapping unsuccessful! A manual port forward may be required.");
            }*/
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void removePortForward() {
        if (!details.useUPNP()) {
            return;
        }
        try {
            GatewayDevice d = discover.getValidGateway();
            if (d == null) {
                discover.discover(); // Attempt to discover the gateway again to delete the port mapping
                d = discover.getValidGateway();
                if (d == null) {
                    Log.warning("Failed to remove port forward on port: "+details.getTCPPort()+"!");
                    return;
                }
            }
            d.deletePortMapping(details.getTCPPort(), "TCP");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
