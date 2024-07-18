package net.cmr.rtd.game.world;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.DataBuffer;
import com.badlogic.gdx.utils.Null;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.GamePlayer;
import net.cmr.rtd.game.world.EnemyFactory.EnemyType;
import net.cmr.rtd.game.world.entities.Player;
import net.cmr.rtd.game.world.entities.WorldSerializationExempt;
import net.cmr.rtd.game.world.tile.StructureTileData;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.game.world.tile.Tile.TileType;
import net.cmr.rtd.game.world.tile.TileData;
import net.cmr.rtd.waves.Wave;
import net.cmr.rtd.waves.WavesData;
import net.cmr.util.Log;
import net.cmr.util.Point;

/**
 * The world class will handle the game world
 * and all of the game objects.
 * The main world will be stored in {@link GameManager},
 * and the manager will send data about the world to
 * the client for them to reconstruct.
 */
public class World extends GameObject {

    public static final int DEFAULT_WORLD_SIZE = 32;
    public static final int LAYERS = 3;
    public float PREPARATION_TIME = 5;

    private int worldSize;
    private ConcurrentHashMap<UUID, Entity> entities; // <Entity ID, Entity>
    private TileType[][][] tiles;
    private HashMap<Point3D, TileData> tileDataMap;
    private HashSet<UUID> removalList;
    private HashMap<String, Player> storedPlayerData;
    public Color worldColor = Color.valueOf("#6663ff");

    // Server only
    private int wave = 0;
    private float waveCountdown = PREPARATION_TIME;
    private WavesData wavesData;

    public static class Point3D {
        public int x, y, z;
        public Point3D(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        public Point3D() {
            this(0, 0, 0);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;
            if (obj.getClass() != getClass()) return false;
            Point3D other = (Point3D) obj;
            return other.x == x && other.y == y && other.z == z;
        }

        @Override
        public String toString() {
            return "Point3D{x=" + x + ", y=" + y + ", z=" + z + "}";
        }
    }

    public World() {
        super(GameType.WORLD);
        this.worldSize = DEFAULT_WORLD_SIZE;
        this.entities = new ConcurrentHashMap<>();
        this.tiles = new TileType[DEFAULT_WORLD_SIZE][DEFAULT_WORLD_SIZE][LAYERS];
        this.tileDataMap = new HashMap<>();
        this.removalList = new HashSet<>();
        this.storedPlayerData = new HashMap<>();
    }

    public void update(float delta, UpdateData data) {
        throw new UnsupportedOperationException("World cannot be updated without a speed.");
    }

    public void update(float speed, float delta, UpdateData data) {
        super.update(delta, data);

        // Segment the delta time into smaller chunks to prevent entities from moving too far.
        // This is to prevent entities from moving through walls.
        float segment = 1/60f;
        float tempDelta = delta;
        while (tempDelta > 0) {
            float d = Math.min(tempDelta, segment);
            updateWorld(speed, d, data);
            tempDelta -= d;
        }
    }

    private void updateWorld(float speed, float delta, UpdateData data) {
        if (data.isServer() && !data.getManager().areWavesPaused()) {
            Wave waveObj = wavesData.getWave(this.wave);
            if (wavesData.endlessMode && waveObj == null && wave != 0) {
                waveObj = wavesData.getNextWave(this.wave, data);
                data.getManager().onWaveChange(wave, waveObj);
            }
            // TODO: SPAWN ENTITIES

            if (waveObj != null) {
                float elapsedTime = waveObj.getWaveTime() - waveCountdown;
                EnemyType[] entities = wavesData.getEntities(data, elapsedTime, delta * speed, this.wave);
                for (EnemyType type : entities) {
                    for (TeamData teamData : data.getManager().getTeams()) {
                        teamData.spawnEnemy(type);
                    }
                }
            }

            waveCountdown -= delta * speed;
            if (waveCountdown <= 0) {
                wave++;
                waveObj = wavesData.getNextWave(this.wave, data);
                if (waveObj == null) {
                    // The game has ended!
                    //data.getManager().stop();
                    data.getManager().gameOver();
                    if (wave > wavesData.getTotalWaves()) {
                        //wave = 0;
                        //waveObj = wavesData.getWave(wave);
                    }
                    return;
                }
                waveCountdown = PREPARATION_TIME + waveObj.getWaveTime() + waveObj.getAdditionalPrepTime();
                data.getManager().onWaveChange(wave, waveObj);
            }
        }
        for (UUID id : removalList) {
            Entity removed = entities.remove(id);
            if (removed == null) continue;
            removed.setWorld(null);
            removed.remove();
        }
        removalList.clear();

        for (Point3D point : tileDataMap.keySet()) {
            TileData tileData = tileDataMap.get(point);
            tileData.update(delta * speed, point.x, point.y, data);
        }
        for (Entity entity : entities.values()) {
            float calculatedDelta = delta * speed;
            if (entity instanceof Player) {
                calculatedDelta = delta;
            }
            entity.update(calculatedDelta, data);
        }
    }
    HashSet<GamePlayer> skipRequests = null;
    public void resetWaveCounter() {
        this.wave = 0;
    }
    public boolean passedAllWaves() {
        return wave > wavesData.getTotalWaves() && !wavesData.endlessMode;
    }
    public void requestSkip(GamePlayer player) {
        Objects.requireNonNull(player);
        Wave waveObj = wavesData.getNextWave(this.wave, player.getManager().getUpdateData());
        float calculatedWaveCountdown = PREPARATION_TIME + waveObj.getWaveTime() + waveObj.getAdditionalPrepTime();
        float elapsedTime = calculatedWaveCountdown - this.waveCountdown;
        if (elapsedTime < waveObj.getAdditionalPrepTime() + PREPARATION_TIME) {
            // Process the skip request.
            if (skipRequests == null) {
                skipRequests = new HashSet<>();
                for (GamePlayer p : player.getManager().getPlayers()) {
                    skipRequests.add(p);
                }
            }
            skipRequests.remove(player);
            if (skipRequests.size() == 0) {
                // Skip the wave.
                this.waveCountdown = waveObj.getWaveTime();
                // Notify the clients that the wave has ended
                skipRequests = null;
                player.getManager().sendWaveUpdateToAll();
            }
        }
    }

    public void addEntity(Entity entity) {
        Objects.requireNonNull(entity);
        entities.put(entity.getID(), entity);
        entity.setWorld(this);
        entity.create();
    }

    public void removeEntity(Entity entity) {
        Objects.requireNonNull(entity);
        removeEntity(entity.getID());
    }

    public void removeEntity(UUID id) {
        Objects.requireNonNull(id);
        this.removalList.add(id);
    }

    public ConcurrentHashMap<UUID, Entity> getEntityMap() { return entities; }
    public Set<UUID> getEntityIDs() { return entities.keySet(); }
    public Entity getEntity(UUID id) { return entities.get(id); }
    public Collection<Entity> getEntities() { return entities.values(); }

    public TileType getTile(int x, int y, int z) {
        if (x < 0 || y < 0 || x >= worldSize || y >= worldSize) {
            return null;
        }
        if (z < 0 || z >= LAYERS) {
            return null;
        }

        return tiles[x][y][z];
    }

    public void setTile(int x, int y, int z, TileType type) {
        tiles[x][y][z] = type;
        setTileData(x, y, z, null);
    }
    public void setTileData(int x, int y, int z, TileData data) {
        if (data == null) {
            tileDataMap.remove(new Point3D(x, y, z));
            return;
        }
        tileDataMap.put(new Point3D(x, y, z), data);
    }
    public TileData getTileData(int x, int y, int z) {
        return tileDataMap.get(new Point3D(x, y, z));
    }

    public int getWorldSize() {
        return worldSize;
    }

    /**
     * Should only be called on the server.
     * Returns the player data for the given name.
     * @param name The name of the player.
     * @return The player data.
     */
    public Player getPlayer(GamePlayer gamePlayer) {
        String name = gamePlayer.getUsername();
        Player player = storedPlayerData.get(name);
        if (player == null) {
            player = new Player(name);
            Point spawn = gamePlayer.getManager().getTeam(gamePlayer.getTeam()).getStructurePosition();
            player.setPosition(spawn.x * Tile.SIZE + Tile.SIZE / 2, spawn.y * Tile.SIZE + Tile.SIZE / 2);
            storedPlayerData.put(name, player);
        }
        return player;
    }

    public void serializePlayerData(DataBuffer buffer) throws IOException {
        ArrayList<Entity> players = new ArrayList<>();
        for (String name : storedPlayerData.keySet()) {
            Player player = storedPlayerData.get(name);
            players.add(player);
        }

        buffer.writeInt(players.size());
        for (Entity player : players) {
            byte[] data = GameObject.serializeGameObject(player);
            buffer.writeInt(data.length);
            buffer.write(data);
        }
    }

    public HashMap<String, Player> deserializePlayerData(DataInputStream input) throws IOException {
        HashMap<String, Player> players = new HashMap<>();
        int playerCount = input.readInt();
        for (int i = 0; i < playerCount; i++) {
            int length = input.readInt();
            byte[] data = new byte[length];
            input.read(data);
            Player player = (Player) GameObject.deserializeGameObject(data);
            players.put(player.getName(), player);
        }
        this.storedPlayerData = players;
        for (Player player : players.values()) {
            Log.info("Player " + player.getName() + " was saved.");
        }
        return players;
    }

    @Override
    protected void serialize(DataBuffer buffer) throws IOException {
        int tempWorldSize = worldSize;
        // Serialized WorldSize: AXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX.
        // If A is true, then the world size is the default size.
        // (If worldsize is negative, then the world size is the default size.)
        if (worldSize == DEFAULT_WORLD_SIZE) {
            tempWorldSize *= -1;
        }
        buffer.writeInt(tempWorldSize);
        buffer.writeFloat(worldColor.r);
        buffer.writeFloat(worldColor.g);
        buffer.writeFloat(worldColor.b);

        buffer.writeInt(wave);
        buffer.writeFloat(waveCountdown);

        for (int x = 0; x < worldSize; x++) {
            for (int y = 0; y < worldSize; y++) {
                for (int z = 0; z < LAYERS; z++) {
                    if (tiles[x][y][z] == null) {
                        buffer.writeInt(Tile.NULL_TILE_ID);
                    } else {
                        buffer.writeInt(tiles[x][y][z].getID());
                    }
                }
            }
        }

        // Check if any entity is exempt from serialization.
        ArrayList<Entity> validEntities = new ArrayList<>();
        for (Entity entity : entities.values()) {
            if (entity.getClass().isAnnotationPresent(WorldSerializationExempt.class)) {
                continue;
            }
            validEntities.add(entity);
        }

        // Serialize entity data.
        int entityCount = validEntities.size();
        buffer.writeInt(entityCount);
        for (Entity entity : validEntities) {
            byte[] data = GameObject.serializeGameObject(entity);
            buffer.writeInt(data.length);
            buffer.write(data);
        }

        // Serialize tile data.
        int tileDataCount = tileDataMap.size();
        buffer.writeInt(tileDataCount);
        Kryo kryo = new Kryo();
        TileData.registerSerializeKryo(kryo);
        Output output = new Output(buffer);
        for (Point3D point : tileDataMap.keySet()) {
            TileData data = tileDataMap.get(point);
            kryo.writeClassAndObject(output, point);
            kryo.writeClassAndObject(output, data);
        }
        output.flush();
        output.close();
    }

    /**
     * Deserializes the world from the given input stream.
     * @implNote This method does NOT save/load players.
     * @param object The object to deserialize into.
     * @param input The input stream to read from.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void deserialize(GameObject object, DataInputStream input) throws IOException {
        World world = (World) object;

        // Serialized WorldSize: AXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX.
        // If A is true, then the world size is the default size.
        // (If worldsize is negative, then the world size is the default size.)
        int tempWorldSize = input.readInt();
        if (tempWorldSize < 0) {
            world.worldSize = DEFAULT_WORLD_SIZE;
        } else {
            world.worldSize = tempWorldSize;
        }
        float r = input.readFloat();
        float g = input.readFloat();
        float b = input.readFloat();
        world.worldColor = new Color(r, g, b, 1);

        world.wave = input.readInt();
        world.waveCountdown = input.readFloat();

        for (int x = 0; x < worldSize; x++) {
            for (int y = 0; y < worldSize; y++) {
                for (int z = 0; z < LAYERS; z++) {
                    world.tiles[x][y][z] = TileType.getType(input.readInt());
                }
            }
        }

        // Deserialize entity data.
        int entityCount = input.readInt();
        for (int i = 0; i < entityCount; i++) {
            int length = input.readInt();
            byte[] data = new byte[length];
            input.read(data);
            Entity entity = (Entity) GameObject.deserializeGameObject(data);
            world.addEntity(entity);
        }

        // Deserialize tile data.
        int tileDataCount = input.readInt();
        Kryo kryo = new Kryo();
        TileData.registerDeserializeKryo(kryo);
        Input kryoInput = new Input(input);
        for (int i = 0; i < tileDataCount; i++) {
            Point3D point = (Point3D) kryo.readClassAndObject(kryoInput);
            TileData data = (TileData) kryo.readClassAndObject(kryoInput);
            world.tileDataMap.put(point, data);
        }
        kryoInput.close();
    }

    public void render(UpdateData udata, Batch batch, float delta) {
        throw new UnsupportedOperationException("World cannot be rendered without a game speed.");
    }
    
    public void render(UpdateData udata, Batch batch, float delta, float gameSpeed) {
        batch.setColor(worldColor);
        float renderDelta = delta * gameSpeed;
        for (int x = 0; x < worldSize; x++) {
            for (int y = 0; y < worldSize; y++) {
                for (int z = 0; z < LAYERS; z++) {
                    if (tiles[x][y][z] == null) {
                        continue;
                    }
                    TileType type = tiles[x][y][z];
                    if (type == null) {
                        continue;
                    }
                    Tile tile = type.getTile();
                    if (tile == null) {
                        continue;
                    }
                    if (type == TileType.END) {
                        Color beforeColor = new Color(batch.getColor());
                        TileData data = tileDataMap.get(new Point3D(x, y, z));
                        if (data instanceof StructureTileData) {
                            StructureTileData structureData = (StructureTileData) data;
                            if (udata.getScreen() != null) {
                                if (structureData.team == udata.getScreen().team) {
                                    batch.setColor(Color.WHITE);
                                }
                                if (structureData.health <= 0) {
                                    batch.setColor(Color.GRAY);
                                }
                            }
                        }
                        tile.render(batch, renderDelta, this, x, y, z);
                        batch.setColor(beforeColor);
                    } else {
                        tile.render(batch, renderDelta, this, x, y, z);
                    }
                    if (tileDataMap.containsKey(new Point3D(x, y, z))) {
                        TileData data = tileDataMap.get(new Point3D(x, y, z));
                        data.render(batch, x, y);
                    }
                }
            }
        }
        batch.setColor(Color.WHITE);
        ArrayList<Entity> renderEntities = new ArrayList<>(this.entities.values());
        // Sort by Y position.
        renderEntities.sort((a, b) -> Float.compare(b.position.y + b.getRenderOffset(), a.position.y + a.getRenderOffset()));

        for (Entity entity : renderEntities) {
            float entityDelta = delta * gameSpeed;
            if (entity instanceof Player) {
                entityDelta = delta;
            }
            entity.render(udata, batch, entityDelta);
        }
        super.render(udata, batch, delta);
    }

    /**
     * Moves the entity and handles with collision.
     * @param entity The entity to move.
     * @param delta The time since the last frame.
     * @param velocity The velocity of the entity.
     */
    public void moveHandleCollision(Entity entity, float delta, Vector2 velocity) {
        linearCollisionHandle(entity, velocity.scl(delta));
    }

        
    final float steps = 3;
    final int detectionRadius = 3;

    private Vector2 linearCollisionHandle(Entity entity, Vector2 velocity) {

        for (int i = 0; i < steps; i++) {
            float velX = velocity.x / steps;
            Vector2 x = linearCollisionHandleStep(entity, new Vector2(velX, 0));
            if (x.x == 0) {
                break;
            }
            entity.position.add(x.x, 0);
        }

        for (int i = 0; i < steps; i++) {
            float velY = velocity.y / steps;
            Vector2 y = linearCollisionHandleStep(entity, new Vector2(0, velY));
            if (y.y == 0) {
                break;
            }
            entity.position.add(0, y.y);
        }

        return null;
    }

    private Vector2 linearCollisionHandleStep(Entity entity, Vector2 velocity) {
        int entityTX = Entity.getTileX(entity);
        int entityTY = Entity.getTileY(entity);

        Vector2 endVelocity = new Vector2(velocity);
        Rectangle entityBounds = entity.getBounds();
        entityBounds.x += velocity.x;
        entityBounds.y += velocity.y;

        for (int tx = -detectionRadius; tx < detectionRadius; tx++) {
            for (int ty = -detectionRadius; ty < detectionRadius; ty++) {
                int x = entityTX + tx;
                int y = entityTY + ty;
                TileType type = getTile(x, y, 1);
                if (type == null) {
                    continue;
                }
                if (!type.isSolid()) {
                    continue;
                }
                Rectangle bounds = new Rectangle(x*Tile.SIZE, y*Tile.SIZE, Tile.SIZE, Tile.SIZE);
                if (bounds.overlaps(entityBounds)) {
                    endVelocity.x = 0;
                    endVelocity.y = 0;
                    return endVelocity;
                }
            }
        }

        return endVelocity;
    }

    public void updateWorldSize(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("World size must be at least 1.");
        }
        this.worldSize = size;

        // Copy the old tiles to the new tiles.
        TileType[][][] newTiles = new TileType[size][size][LAYERS];
        for (int x = 0; x < worldSize; x++) {
            for (int y = 0; y < worldSize; y++) {
                for (int z = 0; z < LAYERS; z++) {
                    if (x < tiles.length && y < tiles[x].length && z < tiles[x][y].length) {
                        newTiles[x][y][z] = tiles[x][y][z];
                    }
                }
            }
        }
        tiles = newTiles;
    }

    public void setWavesData(WavesData wavesData) {
        this.wavesData = wavesData;
        this.PREPARATION_TIME = wavesData.getPreparationTime();
    }
    public WavesData getWavesData() {
        return wavesData;
    }

    public @Null Wave getCurrentWave() {
        return wavesData.getWave(this.wave);
    }
    public float getCurrentWaveDuration() {
        Wave wave = wavesData.getWave(this.wave);
        if (wave == null) {
            return 0;
        }
        return wave.getWaveTime();
    }
    public int getWave() {
        return wave;
    }
    public float getWaveCountdown() {
        return waveCountdown;
    }

    @Override
    public String toString() {
        return "[World]{worldSize=" + worldSize + ", entities=" + entities.size() + "}";
    }

}
