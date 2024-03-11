package net.cmr.rtd.screen;

import java.security.KeyPair;
import java.util.ArrayList;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
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
import net.cmr.rtd.game.packets.PlayerInputPacket;
import net.cmr.rtd.game.packets.PlayerPacket;
import net.cmr.rtd.game.packets.RSAEncryptionPacket;
import net.cmr.rtd.game.packets.StatsUpdatePacket;
import net.cmr.rtd.game.packets.WavePacket;
import net.cmr.rtd.game.stream.GameStream;
import net.cmr.rtd.game.stream.GameStream.PacketListener;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.game.world.entities.Player;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.entities.towers.FireTower;
import net.cmr.rtd.game.world.entities.towers.IceTower;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Log;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.SpriteType;

public class GameScreen extends AbstractScreenEX {
    
    GameStream ioStream;
    @Null GameManager gameManager; // Will be null if the local player is not the host
    World world;
    Viewport viewport;
    ShapeRenderer shapeRenderer;
    UpdateData data;
    Player localPlayer = null;
    final String password;

    Label lifeLabel, structureLifeLabel, cashLabel, waveLabel, waveCountdownLabel;
    Image life, structureLife, cash;

    ArrayList<Entity> entityQueue = new ArrayList<Entity>();
    float waveCountdown = -1, waveDuration = 0;
    int wave = 0;
    boolean areWavesPaused = false;

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

    @Override
    public void show() {
        super.show();

        float iconSize = 32;

        life = new Image(Sprites.drawable(SpriteType.HEART));
        life.setSize(iconSize, iconSize);
        life.setPosition(5, 360-5, Align.topLeft);

        lifeLabel = new Label("100", Sprites.skin(), "small");
        lifeLabel.setAlignment(Align.center);
        lifeLabel.setSize(iconSize, iconSize);
        lifeLabel.setPosition(5 + iconSize, 360-5, Align.topLeft);
        
        add(Align.topLeft, life);
        add(Align.topLeft, lifeLabel);

        cash = new Image(Sprites.drawable(SpriteType.CASH));
        cash.setSize(iconSize, iconSize);
        cash.setPosition(5, 360-5-iconSize, Align.topLeft);

        cashLabel = new Label("100", Sprites.skin(), "small");
        cashLabel.setAlignment(Align.center);
        cashLabel.setSize(iconSize, iconSize);
        cashLabel.setPosition(5 + iconSize, 360-5-iconSize, Align.topLeft);

        add(Align.topLeft, cash);
        add(Align.topLeft, cashLabel);

        structureLife = new Image(Sprites.drawable(SpriteType.STRUCTURE_LIFE));
        structureLife.setSize(iconSize, iconSize);
        structureLife.setPosition(5, 360-5-iconSize*2, Align.topLeft);

        structureLifeLabel = new Label("100", Sprites.skin(), "small");
        structureLifeLabel.setAlignment(Align.center);
        structureLifeLabel.setSize(iconSize, iconSize);
        structureLifeLabel.setPosition(5 + iconSize, 360-5-iconSize*2, Align.topLeft);

        add(Align.topLeft, structureLife);
        add(Align.topLeft, structureLifeLabel);

        waveLabel = new Label("Waiting to start...", Sprites.skin(), "small");
        waveLabel.setAlignment(Align.right);
        waveLabel.setSize(200, iconSize);
        waveLabel.setPosition(640-5, 360-5, Align.topRight);

        add(Align.topRight, waveLabel);

        waveCountdownLabel = new Label("", Sprites.skin(), "small");
        waveCountdownLabel.setAlignment(Align.right);
        waveCountdownLabel.setSize(200, iconSize);
        waveCountdownLabel.setPosition(640-5, 360-5-iconSize, Align.topRight);

        add(Align.topRight, waveCountdownLabel);
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
                for (Entity entity : entityQueue) {
                    world.addEntity(entity);
                }
            }
            if (object instanceof Entity) {
                Entity entity = (Entity) object;
                if (world == null) {
                    entityQueue.add(entity);
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

        if (packet instanceof PlayerInputPacket) {
            // The server and client have been desynchronized. Set the local player's position to the server's position.
            PlayerInputPacket inputPacket = (PlayerInputPacket) packet;
            Player player = getLocalPlayer();
            if (player != null) {
                player.setPosition(inputPacket.getPosition());
            }
        }

        if (packet instanceof StatsUpdatePacket) {
            StatsUpdatePacket statsPacket = (StatsUpdatePacket) packet;
            lifeLabel.setText(String.valueOf(statsPacket.getHealth()));
            cashLabel.setText(String.valueOf(statsPacket.getMoney()));
            structureLifeLabel.setText(String.valueOf(statsPacket.getStructureHealth()));
        }

        if (packet instanceof WavePacket) {
            WavePacket wavePacket = (WavePacket) packet;

            this.waveDuration = wavePacket.getWaveLength();
            this.waveCountdown = wavePacket.getDuration();
            this.wave = wavePacket.getWaveNumber();
            this.areWavesPaused = wavePacket.isPaused();

            /*if (wavePacket.getWaveNumber() == 0) {
                waveLabel.setText("Waiting to start...");
                waveCountdownLabel.setText("");
            } else {
                waveLabel.setText("Wave " + wavePacket.getWaveNumber());
                waveCountdown = wavePacket.getDuration();
            }*/
        }

        if (packet instanceof PlayerPacket) {
            PlayerPacket playerPacket = (PlayerPacket) packet;
            if (playerPacket.isConnecting()) {
                // Add a player object to the world
                Player player = new Player(playerPacket.username);
                player.setPosition(playerPacket.x, playerPacket.y);
                world.addEntity(player);
            } else {
                // Remove the player object from the world
                for (Entity entity : world.getEntities()) {
                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        if (player.getName().equals(playerPacket.username)) {
                            world.removeEntity(player);
                            break;
                        }
                    }
                }
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

    public void update(float delta) {
        ioStream.update();

        if (areWavesPaused) {
            waveCountdownLabel.setText("Waves Paused");
        } else if (waveCountdown != -1) {

            String waveText = "Wave " + wave;
            waveCountdown -= delta;
            if (waveCountdown < 0) {
                waveCountdown = 0;
            }

            float preparationTime = waveCountdown - waveDuration;
            float displayCountdown = waveCountdown;
            if (preparationTime > 0) {
                displayCountdown = preparationTime;
                waveText = "Prepare for " + waveText;
            }

            waveCountdownLabel.setText(String.format("%.2f", displayCountdown));
            waveLabel.setText(waveText);
        }

        if (world != null) {
            // FIXME: When the window is frozen, new game objects are not added to the world. This causes newly added enemies to be bunched up after the window is unfrozen.
            world.update(delta, data);
        }
        processInput(delta);
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
                camera.position.x = player.getX() + player.getBounds().getWidth() / 2;
                camera.position.y = player.getY() + player.getBounds().getHeight() / 2;
            }
        }
    }

    private void processInput(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (gameManager != null) {
                gameManager.save();
            }
            game.setScreen(new MainMenuScreen());
            return;
        }

        stages.actAll(delta);

        int mx = Gdx.input.getX();
        int my = Gdx.input.getY();
        Vector2 mousePos = viewport.unproject(new Vector2(mx, my));
        mousePos.x = (int) Math.floor(mousePos.x/Tile.SIZE) * Tile.SIZE;
        mousePos.y = (int) Math.floor(mousePos.y/Tile.SIZE) * Tile.SIZE;

        int tileX = (int) Math.floor(mousePos.x/Tile.SIZE);
        int tileY = (int) Math.floor(mousePos.y/Tile.SIZE);
        
        processPlayerMovement(delta);
        processMouse(tileX, tileY);
    }

    private void processMouse(int tileX, int tileY) {
        TowerEntity toPlace = null;
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            // Place down an ice tower
            toPlace = new FireTower();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            // Place down a fire tower
            toPlace = new IceTower();
        }
        if (toPlace == null) {
            return;
        }
        toPlace.setPosition((tileX + .5f) * Tile.SIZE, (tileY + .5f) * Tile.SIZE);
        world.addEntity(toPlace);
        if (gameManager != null) {
            gameManager.getWorld().addEntity(toPlace);
        }
    }

    float lastVelocityX = 0, lastVelocityY = 0;
    boolean lastSprinting = false;

    private void processPlayerMovement(float delta) {
        if (getLocalPlayer() == null) {
            return;
        }
        float vx = (Gdx.input.isKeyPressed(Input.Keys.D) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.A) ? 1 : 0);
        float vy = (Gdx.input.isKeyPressed(Input.Keys.W) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.S) ? 1 : 0);
        boolean sprinting = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);

        if (lastVelocityX != vx || lastVelocityY != vy || sprinting != lastSprinting) {
            // Something changed, send the new input to the server.
            PlayerInputPacket inputPacket = new PlayerInputPacket(new Vector2(vx, vy), getLocalPlayer().getPosition(), lastSprinting);
            ioStream.sendPacket(inputPacket);
        }

        lastVelocityX = vx;
        lastVelocityY = vy;
        lastSprinting = sprinting;

        getLocalPlayer().updateInput(new Vector2(vx, vy), lastSprinting);
    }

    @Override
    public void render(float delta) {
        update(delta);

        if (world != null) {
            viewport.apply();
            batch.setProjectionMatrix(viewport.getCamera().combined);
            batch.begin();
            world.render(batch, delta);
            batch.end();
        }

        stages.drawAll(batch);
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
        if (world == null) { return null; }
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

    public World getWorld() {
        return world;
    }

}
