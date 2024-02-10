package net.cmr.rtd.screen;

import java.security.KeyPair;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.kryo.util.Null;

import net.cmr.rtd.RetroTowerDefense;
import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.GamePlayer;
import net.cmr.rtd.game.packets.AESEncryptionPacket;
import net.cmr.rtd.game.packets.DisconnectPacket;
import net.cmr.rtd.game.packets.GameObjectPacket;
import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.packets.PacketEncryption;
import net.cmr.rtd.game.packets.PasswordPacket;
import net.cmr.rtd.game.packets.PlayerPacket;
import net.cmr.rtd.game.packets.RSAEncryptionPacket;
import net.cmr.rtd.game.stream.GameStream;
import net.cmr.rtd.game.stream.GameStream.PacketListener;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.game.world.entities.Player;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.game.world.tile.Tile.TileType;
import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Log;

public class GameScreen extends AbstractScreenEX {
    
    GameStream ioStream;
    @Null GameManager gameManager; // Will be null if the local player is not the host
    World world;
    Viewport viewport;
    ShapeRenderer shapeRenderer;
    UpdateData data;
    Player localPlayer = null;
    final String password;

    public GameScreen(GameStream ioStream, @Null GameManager gameManager, @Null String password) {
        super(INITIALIZE_ALL);
        this.ioStream = ioStream;
        this.gameManager = gameManager;
        this.ioStream.addListener(new PacketListener() {
            @Override
            public void packetReceived(Packet packet) {
                onRecievePacket(packet);
            }
        });
        this.viewport = new ExtendViewport(640, 360);
        this.shapeRenderer = new ShapeRenderer();
        this.data = new UpdateData(this);
        if (gameManager != null) {
            this.password = gameManager.getDetails().getPassword();
        } else {
            this.password = password;
        }
    }

    public void onRecievePacket(Packet packet) {
        if (packet instanceof RSAEncryptionPacket) {
            // Set the RSA public key and send our RSA public key.
            RSAEncryptionPacket rsaPacket = (RSAEncryptionPacket) packet;
            ioStream.getEncryptor().setRSAPublic(PacketEncryption.publicKeyFromBytes(rsaPacket.RSAData));

            KeyPair keyPair = PacketEncryption.createRSAKeyPair();
            ioStream.getEncryptor().setRSAPrivate(keyPair.getPrivate());

            RSAEncryptionPacket rsaResponse = new RSAEncryptionPacket(keyPair.getPublic());
            ioStream.sendPacket(rsaResponse);

            // Create AES data.
            SecretKey secretKey = PacketEncryption.createAESKey();
            IvParameterSpec iv = PacketEncryption.createIV();
            ioStream.getEncryptor().setAESData(secretKey, iv);

            // Send AES data.
            AESEncryptionPacket aesPacket = new AESEncryptionPacket(secretKey, iv);
            ioStream.sendPacket(aesPacket);

            // If the server requires a password, send it.
            if (password != null) {
                ioStream.sendPacket(new PasswordPacket(password));
            }
        }

        if (packet instanceof GameObjectPacket) {
            GameObjectPacket gameObjectPacket = (GameObjectPacket) packet;
            Log.debug("Received GameObject");
            GameObject object = gameObjectPacket.getObject();
            Log.debug("Object: " + object.getClass().getSimpleName(), object);
            if (object instanceof World) {
                this.world = (World) object;
            }
            if (object instanceof Entity) {
                Entity entity = (Entity) object;
                if (world == null) {
                    Log.error("World is null", new NullPointerException());
                    return;
                }
                if (gameObjectPacket.shouldRemove()) {
                    world.removeEntity(entity);
                } else {
                    world.addEntity(entity);
                }
            }
            return;
        }

        if (packet instanceof PlayerPacket) {
            PlayerPacket playerPacket = (PlayerPacket) packet;
            if (playerPacket.isConnecting()) {
                // Add a player object to the world
                Player player = new Player(playerPacket.username);
                world.addEntity(player);
            } else {
                // Remove the player object from the world
                Player player = new Player(playerPacket.username);
                world.removeEntity(player);
            }
            if (!playerPacket.isInitializingWorld()) {
                // If it isn't initializing the world, then notify
                // this screen that a player has joined or left.
                Log.info("Player " + playerPacket.username + " has " + (playerPacket.isConnecting() ? "joined" : "left")); 
            }
            return;
        }

        if (packet instanceof DisconnectPacket) {
            // The player was disconnected, set the screen to the main menu.
            DisconnectPacket disconnectPacket = (DisconnectPacket) packet;
            Log.info("Client disconnected: " + disconnectPacket.reason);
            game.setScreen(new MainMenuScreen());
        }
    }

    int lastNumber = 0;

    public void update(float delta) {
        ioStream.update();
        if (world != null) {
            world.update(delta, data);
        }
        updateCamera();
    }

    /**
     * Updates the camera to center on the local player.
     */
    private void updateCamera() {
        // Get the local player's position and center the camera on it.
        if (world != null) {
            Player player = getLocalPlayer();
            if (player != null) {
                OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
                camera.position.x = player.getX();
                camera.position.y = player.getY();
            }
        }
    }

    private void processInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen());
            return;
        }

        int mx = Gdx.input.getX();
        int my = Gdx.input.getY();
        Vector2 mousePos = viewport.unproject(new Vector2(mx, my));
        mousePos.x = (int) Math.floor(mousePos.x/Tile.SIZE) * Tile.SIZE;
        mousePos.y = (int) Math.floor(mousePos.y/Tile.SIZE) * Tile.SIZE;

        int tileX = (int) Math.floor(mousePos.x/Tile.SIZE);
        int tileY = (int) Math.floor(mousePos.y/Tile.SIZE);
        processMouse(tileX, tileY);
    }

    private void processMouse(int tileX, int tileY) {

    }

    @Override
    public void render(float delta) {
        update(delta);

        /*boolean zero = Gdx.input.isKeyPressed(Input.Keys.NUM_0);
        boolean one = Gdx.input.isKeyPressed(Input.Keys.NUM_1);
        boolean two = Gdx.input.isKeyPressed(Input.Keys.NUM_2);
        boolean three = Gdx.input.isKeyPressed(Input.Keys.NUM_3);
        boolean four = Gdx.input.isKeyPressed(Input.Keys.NUM_4);
        boolean five = Gdx.input.isKeyPressed(Input.Keys.NUM_5);
        boolean six = Gdx.input.isKeyPressed(Input.Keys.NUM_6);

        lastNumber = zero ? 0 : one ? 1 : two ? 2 : three ? 3 : four ? 4 : five ? 5 : six ? 6 : lastNumber;

        int x = (Gdx.input.isKeyPressed(Input.Keys.D) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.A) ? 1 : 0);
        int y = (Gdx.input.isKeyPressed(Input.Keys.W) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.S) ? 1 : 0);
        boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
        OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
        camera.position.x += x * delta * Tile.SIZE * 5 * (shift ? 5 : 1);
        camera.position.y += y * delta * Tile.SIZE * 5 * (shift ? 5 : 1);
        
        int zoom = (Gdx.input.isKeyPressed(Input.Keys.E) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.Q) ? 1 : 0);
        camera.zoom += zoom * delta * (shift ? 5 : 1);
        camera.zoom = Math.max(0.1f, Math.min(1000f, camera.zoom));


        if (world != null) {
            viewport.apply();
            batch.setProjectionMatrix(viewport.getCamera().combined);
            batch.begin();
            world.render(batch, delta);
            batch.end();
        }

        int mx = Gdx.input.getX();
        int my = Gdx.input.getY();
        Vector2 mousePos = viewport.unproject(new Vector2(mx, my));
        mousePos.x = (int) Math.floor(mousePos.x/Tile.SIZE) * Tile.SIZE;
        mousePos.y = (int) Math.floor(mousePos.y/Tile.SIZE) * Tile.SIZE;

        int tileX = (int) Math.floor(mousePos.x/Tile.SIZE);
        int tileY = (int) Math.floor(mousePos.y/Tile.SIZE);

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, 1);
        shapeRenderer.rect(mousePos.x, mousePos.y, Tile.SIZE, Tile.SIZE);
        shapeRenderer.end();

        boolean left = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        boolean right = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
        if (left || right) {
            int z = 0;
            if (right) {
                z = 1;
            }
            if (!(tileX < 0 || tileY < 0 || tileX >= world.getWorldSize() || tileY >= world.getWorldSize())) {
                Log.info("Clicked on tile: " + tileX + ", " + tileY);
                world.setTile(tileX, tileY, z, TileType.getType(lastNumber));
            }
        }*/

        super.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height);
    }

    @Override
    public void hide() {
        super.hide();
        ioStream.sendPacket(new DisconnectPacket(GamePlayer.QUIT));
        ioStream.onClose();
        if (gameManager != null) {
            gameManager.stop();
        }
    }

    public @Null Player getLocalPlayer() {
        if (localPlayer != null) {
            return localPlayer;
        }
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                if (player.getName().equals(((RetroTowerDefense) game).getUsername())) {
                    localPlayer = player;
                    return player;
                }
            }
        }
        return null;
    }

}
