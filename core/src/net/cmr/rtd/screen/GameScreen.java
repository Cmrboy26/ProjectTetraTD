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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
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
import net.cmr.rtd.game.packets.PlayerPacket;
import net.cmr.rtd.game.packets.RSAEncryptionPacket;
import net.cmr.rtd.game.stream.GameStream;
import net.cmr.rtd.game.stream.GameStream.PacketListener;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.game.world.entities.Player;
import net.cmr.rtd.game.world.tile.TeamTileData;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.game.world.tile.Tile.TileType;
import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Log;
import net.cmr.util.Sprites;

public class GameScreen extends AbstractScreenEX {
    
    GameStream ioStream;
    @Null GameManager gameManager; // Will be null if the local player is not the host
    World world;
    Viewport viewport;
    ShapeRenderer shapeRenderer;
    UpdateData data;
    Player localPlayer = null;
    final String password;

    Window window;
    TextButton wall, floor, clear, path , start, end;

    // Editor Variables
    boolean entered;
    int selectedTile = 2;
    int teamNumber = 0;


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

        window = new Window("Creator", Sprites.getInstance().getSkin(), "small");
        window.getTitleLabel().setAlignment(Align.center);
        window.setScale(.5f);
        window.setSize(150, 300);
        window.setOrigin(Align.center);
        window.setPosition(5, (360 / 2), Align.left);
        window.padTop(20);
        window.setVisible(true);
        window.setMovable(true);
        window.setResizable(true);
        window.setModal(false);
        
        add(Align.left, window);

        float size = 40;

        ButtonGroup<Button> group = new ButtonGroup<Button>();
        wall = new TextButton("Wall", Sprites.skin(), "small");
        wall.setSize(size, size);
        wall.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedTile = 2;
            }
        });

        floor = new TextButton("Floor", Sprites.skin(), "small");
        floor.setSize(size, size);
        floor.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedTile = 1;
            }
        });

        clear = new TextButton("Clear", Sprites.skin(), "small");
        clear.setSize(size, size);
        clear.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedTile = 0;
            }
        });

        path = new TextButton("Path", Sprites.skin(), "small");
        path.setSize(size, size);
        path.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedTile = 3;
            }
        });

        start = new TextButton("Start", Sprites.skin(), "small");
        start.setSize(size, size);
        start.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedTile = 4;
            }
        });

        end = new TextButton("End", Sprites.skin(), "small");
        end.setSize(size, size);
        end.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedTile = 5;
            }
        });

        Label teamLabel = new Label("#" + teamNumber, Sprites.skin(), "small");
        teamLabel.setSize(size, size);
        teamLabel.setAlignment(Align.center);

        TextButton add = new TextButton("+", Sprites.skin(), "small");
        add.setSize(size, size);
        add.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                teamNumber++;
                if (teamNumber >= GameManager.MAX_TEAMS) teamNumber = GameManager.MAX_TEAMS - 1;
                teamLabel.setText("#" + teamNumber);
            }
        });

        TextButton sub = new TextButton("-", Sprites.skin(), "small");
        sub.setSize(size, size);
        sub.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                teamNumber--;
                if (teamNumber < 0) teamNumber = 0;
                teamLabel.setText("#" + teamNumber);
            }
        });


        group.add(wall);
        group.add(floor);
        group.add(clear);
        group.add(path);
        group.add(start);
        group.add(end);

        window.add(wall).size(size).pad(4);
        window.add(floor).size(size).pad(4);
        window.add(clear).size(size).pad(4).row();
        window.add(path).size(size).pad(4);
        window.add(start).size(size).pad(4);
        window.add(end).size(size).pad(4).row();
        window.add(sub).size(size).pad(4);
        window.add(teamLabel).size(size).pad(4);
        window.add(add).size(size).pad(4).row();


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
                player.setPosition(Tile.SIZE*2, Tile.SIZE*2);
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

    public void update(float delta) {
        ioStream.update();
        if (world != null) {
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

        int lastMx = Gdx.input.getX() - Gdx.input.getDeltaX();
        int lastMy = Gdx.input.getY() - Gdx.input.getDeltaY();
        int mx = Gdx.input.getX();
        int my = Gdx.input.getY();

        Vector2 localMouse = window.screenToLocalCoordinates(new Vector2(mx, my));

        if (localMouse.x < window.getX() + window.getWidth() && !entered) {
            entered = true;
            window.clearActions();
            window.addAction(Actions.fadeIn(.15f));
        } else if (localMouse.x >= window.getX() + window.getWidth() && entered) {
            entered = false;
            window.clearActions();
            window.addAction(Actions.fadeOut(.15f));
        }

        // If mouse is over window, don't process mouse input.
        if (window.hit(localMouse.x, localMouse.y, true) != null) {
            return;
        }

        Vector2 mousePos = viewport.unproject(new Vector2(mx, my));
        mousePos.x = (int) Math.floor(mousePos.x/Tile.SIZE) * Tile.SIZE;
        mousePos.y = (int) Math.floor(mousePos.y/Tile.SIZE) * Tile.SIZE;

        boolean left = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        boolean right = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);

        TileType type = TileType.getType(selectedTile);
        if (right) {
            type = null;
        }
        if ((left || right) && world != null && !(tileX < 0 || tileY < 0 || tileX >= world.getWorldSize() || tileY >= world.getWorldSize())) {
            if (selectedTile == 1 || selectedTile == 0) {
                world.setTile(tileX, tileY, 0, type);
                gameManager.getWorld().setTile(tileX, tileY, 0, type);
            }
            if (selectedTile != 1 || selectedTile == 0) {
                world.setTile(tileX, tileY, 1, type);
                gameManager.getWorld().setTile(tileX, tileY, 1, type);
            }

            if (selectedTile == 3 || selectedTile == 4 || selectedTile == 5) {
                world.setTileData(tileX, tileY, 1, new TeamTileData(teamNumber));
                gameManager.getWorld().setTileData(tileX, tileY, 1, new TeamTileData(teamNumber));
            }
        }
    }

    private void processPlayerMovement(float delta) {
        if (getLocalPlayer() == null) {
            return;
        }
        float vx = (Gdx.input.isKeyPressed(Input.Keys.D) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.A) ? 1 : 0);
        float vy = (Gdx.input.isKeyPressed(Input.Keys.W) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.S) ? 1 : 0);
        float speed = 5;

        vx *= Tile.SIZE * speed;
        vy *= Tile.SIZE * speed;

        getLocalPlayer().setVelocity(new Vector2(vx, vy));
    }

    @Override
    public void render(float delta) {
        update(delta);
        
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
            selectedTile = 2; // wall
            wall.setChecked(true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
            selectedTile = 1; // floor
            floor.setChecked(true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_3)) {
            selectedTile = 3; // path
            path.setChecked(true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_4)) {
            selectedTile = 4; // start
            start.setChecked(true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_5)) {
            selectedTile = 5; // end
            end.setChecked(true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.GRAVE)) {
            selectedTile = 0; // clear
            clear.setChecked(true);
        }

        int mx = Gdx.input.getX();
        int my = Gdx.input.getY();
        Vector2 mousePos = viewport.unproject(new Vector2(mx, my));
        mousePos.x = (int) Math.floor(mousePos.x/Tile.SIZE) * Tile.SIZE;
        mousePos.y = (int) Math.floor(mousePos.y/Tile.SIZE) * Tile.SIZE;

        if (world != null) {
            viewport.apply();
            batch.setProjectionMatrix(viewport.getCamera().combined);
            batch.begin();
            world.render(batch, delta);
            batch.end();
        }

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, 1);
        shapeRenderer.rect(mousePos.x, mousePos.y, Tile.SIZE, Tile.SIZE);
        shapeRenderer.end();

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
