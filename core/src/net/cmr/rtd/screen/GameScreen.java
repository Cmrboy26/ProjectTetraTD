package net.cmr.rtd.screen;

import java.awt.Point;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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
import net.cmr.rtd.game.packets.PurchaseItemPacket;
import net.cmr.rtd.game.packets.PurchaseItemPacket.PurchaseAction;
import net.cmr.rtd.game.packets.RSAEncryptionPacket;
import net.cmr.rtd.game.packets.StatsUpdatePacket;
import net.cmr.rtd.game.packets.WavePacket;
import net.cmr.rtd.game.stream.GameStream;
import net.cmr.rtd.game.stream.GameStream.PacketListener;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.GameObject.GameType;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.rtd.game.world.entities.Player;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.entities.EnemyEntity.DamageType;
import net.cmr.rtd.game.world.particles.ParticleEffect;
import net.cmr.rtd.game.world.store.ShopManager;
import net.cmr.rtd.game.world.store.TowerOption;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Log;
import net.cmr.util.Settings;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.SpriteType;

public class GameScreen extends AbstractScreenEX {
    
    GameStream ioStream;
    @Null GameManager gameManager; // Will be null if the local player is not the host
    World world;
    Stage worldStage;
    Viewport viewport;
    ShapeRenderer shapeRenderer;
    UpdateData data;
    Player localPlayer = null;
    final String password;

    Label lifeLabel, structureLifeLabel, cashLabel, waveLabel, waveCountdownLabel;
    Image life, structureLife, cash;
    ImageButton upgradeButton, shopButton, inventoryButton, skipWaveButton;
    Window shopWindow, inventoryWindow;
    long localMoney;

    GameType typeToPurchase;
    Entity entityToPlace;
    PlacementMode placementMode = PlacementMode.NONE;
    public enum PlacementMode {
        NONE, PLACE, UPGRADE
    }

    ArrayList<ParticleEffect> particleEffects = new ArrayList<ParticleEffect>();

    Dialog quitDialog;
    Dialog upgradeDialog;

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
        this.worldStage = new Stage(viewport);
        this.stages.addProcessor(this.worldStage);
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
        lifeLabel.setAlignment(Align.left);
        lifeLabel.setSize(iconSize, iconSize);
        lifeLabel.setPosition(15 + iconSize, 360-5, Align.topLeft);
        
        add(Align.topLeft, life);
        add(Align.topLeft, lifeLabel);

        cash = new Image(Sprites.drawable(SpriteType.CASH));
        cash.setSize(iconSize, iconSize);
        cash.setPosition(5, 360-5-iconSize, Align.topLeft);

        cashLabel = new Label("100", Sprites.skin(), "small");
        cashLabel.setAlignment(Align.left);
        cashLabel.setSize(iconSize, iconSize);
        cashLabel.setPosition(15 + iconSize, 360-5-iconSize, Align.topLeft);

        add(Align.topLeft, cash);
        add(Align.topLeft, cashLabel);

        structureLife = new Image(Sprites.drawable(SpriteType.STRUCTURE_LIFE));
        structureLife.setSize(iconSize, iconSize);
        structureLife.setPosition(5, 360-5-iconSize*2, Align.topLeft);

        structureLifeLabel = new Label("100", Sprites.skin(), "small");
        structureLifeLabel.setAlignment(Align.left);
        structureLifeLabel.setSize(iconSize, iconSize);
        structureLifeLabel.setPosition(15 + iconSize, 360-5-iconSize*2, Align.topLeft);

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

        ButtonGroup<ImageButton> buttonGroup = new ButtonGroup<ImageButton>();
        buttonGroup.setMaxCheckCount(1);
        buttonGroup.setMinCheckCount(0);

        ImageButtonStyle style = new ImageButtonStyle();
        style.down = Sprites.drawable(SpriteType.BORDER_DOWN);
        style.up = Sprites.drawable(SpriteType.BORDER_DEFAULT);
        style.over = Sprites.drawable(SpriteType.BORDER_HOVER);
        style.checked = Sprites.drawable(SpriteType.BORDER_SELECTED);
        style.disabled = Sprites.drawable(SpriteType.BORDER_DISABLED);
        style.imageUp = Sprites.drawable(SpriteType.SHOP_ICON);

        shopButton = new ImageButton(style);
        shopButton.setSize(iconSize, iconSize);
        shopButton.setPosition(320 - 2.5f, 5, Align.bottomRight);
        shopButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (shopButton.isDisabled()) { return; }
                // NOTE: when switching to shop screen, deselect any other screens that are open (i.e. inventory screen)
                shopWindow.setVisible(shopButton.isChecked());
                inventoryWindow.setVisible(false);
            }
        });
        buttonGroup.add(shopButton);
        
        add(Align.bottom, shopButton);

        style = new ImageButtonStyle();
        style.down = Sprites.drawable(SpriteType.BORDER_DOWN);
        style.up = Sprites.drawable(SpriteType.BORDER_DEFAULT);
        style.over = Sprites.drawable(SpriteType.BORDER_HOVER);
        style.checked = Sprites.drawable(SpriteType.BORDER_SELECTED);
        style.disabled = Sprites.drawable(SpriteType.BORDER_DISABLED);
        style.imageUp = Sprites.drawable(SpriteType.INVENTORY_ICON);

        inventoryButton = new ImageButton(style);
        inventoryButton.setSize(iconSize, iconSize);
        inventoryButton.setPosition(320 + (2.5f), 5, Align.bottomLeft);
        inventoryButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (inventoryButton.isDisabled()) { return; }
                // NOTE: when switching to inventory screen, deselect any other screens that are open
                inventoryWindow.setVisible(inventoryButton.isChecked());
                shopWindow.setVisible(false);
            }
        });
        buttonGroup.add(inventoryButton);

        add(Align.bottom, inventoryButton);

        style = new ImageButtonStyle();
        style.down = Sprites.drawable(SpriteType.BORDER_DOWN);
        style.up = Sprites.drawable(SpriteType.BORDER_DEFAULT);
        style.over = Sprites.drawable(SpriteType.BORDER_HOVER);
        style.checked = Sprites.drawable(SpriteType.BORDER_SELECTED);
        style.disabled = Sprites.drawable(SpriteType.BORDER_DISABLED);
        style.imageUp = Sprites.drawable(SpriteType.CASH);

        upgradeButton = new ImageButton(style);
        upgradeButton.setSize(iconSize, iconSize);
        upgradeButton.setPosition(320 - 7.5f - 32, 5, Align.bottomRight);
        upgradeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (upgradeButton.isDisabled()) { return; }
                enterUpgradeMode();
            }
        });
        buttonGroup.add(upgradeButton);

        add(Align.bottom, upgradeButton);

        // TODO: Add inventory menus and functionality
        shopWindow = new Window("Shop", Sprites.skin(), "small");
        shopWindow.getTitleLabel().setAlignment(Align.center);
        shopWindow.padTop(30);
        shopWindow.setSize(300, 200);
        shopWindow.setPosition(320, 180, Align.center);
        shopWindow.setMovable(false);
        shopWindow.setVisible(false);
        add(Align.center, shopWindow);

        // TODO: Add all the shop items
        
        for (TowerOption option : ShopManager.getTowerCatalog().values()) {
            GameType type = option.type;
            TextureRegionDrawable drawable = option.sprite != null ? Sprites.drawable(option.sprite) : Sprites.drawable(option.animation);
            Table towerSection = getTowerSection(drawable, type, option.name, String.valueOf(option.cost), option.description);
            shopWindow.add(towerSection).pad(5);
            shopWindow.row();
        }
        
        /*Table iceTower = getTowerSection(Sprites.drawable(AnimationType.TESLA_TOWER), GameType.ICE_TOWER, "Ice Tower", "100", "Slows down enemies in range.");
        Table fireTower = getTowerSection(Sprites.drawable(AnimationType.TESLA_TOWER), GameType.FIRE_TOWER,  "Fire Tower", "100", "Sets enemies ablaze and deals damage over time.");
        shopWindow.add(iceTower).pad(5);
        shopWindow.row();
        shopWindow.add(fireTower).pad(5);
        shopWindow.row();*/

        inventoryWindow = new Window("Inventory", Sprites.skin(), "small");
        inventoryWindow.getTitleLabel().setAlignment(Align.center);
        inventoryWindow.padTop(30);
        inventoryWindow.setSize(200, 200);
        inventoryWindow.setPosition(320, 180, Align.center);
        inventoryWindow.setMovable(false);
        inventoryWindow.setVisible(false);
        add(Align.center, inventoryWindow);

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

            return;
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
                    if (world.getEntity(entity.getID()) != null) {
                        world.removeEntity(entity);
                        if (entity instanceof EnemyEntity) {
                            EnemyEntity enemy = (EnemyEntity) entity;
                            enemy.playHitSound(data, DamageType.PHYSICAL);
                        }
                    }
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
            return;
        }

        if (packet instanceof StatsUpdatePacket) {
            StatsUpdatePacket statsPacket = (StatsUpdatePacket) packet;
            lifeLabel.setText(String.valueOf(statsPacket.getHealth()));
            cashLabel.setText(ShopManager.costToString(statsPacket.getMoney()).substring(1));
            structureLifeLabel.setText(String.valueOf(statsPacket.getStructureHealth()));
            localMoney = statsPacket.getMoney();
            return;
        }

        if (packet instanceof WavePacket) {
            WavePacket wavePacket = (WavePacket) packet;

            this.waveDuration = wavePacket.getWaveLength();
            this.waveCountdown = wavePacket.getDuration();
            this.wave = wavePacket.getWaveNumber();
            this.areWavesPaused = wavePacket.isPaused();
            if (wavePacket.shouldWarn()) {
                notification(SpriteType.WARNING, "Special wave incoming! Be careful!");
            }

            return;
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
            return;
        }
    }

    public void update(float delta) {
        worldStage.act(delta);
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

        ArrayList<ParticleEffect> toRemove = new ArrayList<ParticleEffect>();
        for (int i = 0; i < particleEffects.size(); i++) {
            ParticleEffect effect = particleEffects.get(i);
            effect.update(this, delta);
            if (effect.isParticleFinished()) {
                toRemove.add(effect);
            }
        }
        for (ParticleEffect effect : toRemove) {
            particleEffects.remove(effect);
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
                camera.position.x = player.getX() + player.getBounds().getWidth() / 2;
                camera.position.y = player.getY() + player.getBounds().getHeight() / 2;
            }
        }
    }

    private Point getMouseTileCoordinate() {
        int mx = Gdx.input.getX();
        int my = Gdx.input.getY();
        Vector2 mousePos = viewport.unproject(new Vector2(mx, my));
        mousePos.x = (int) Math.floor(mousePos.x/Tile.SIZE) * Tile.SIZE;
        mousePos.y = (int) Math.floor(mousePos.y/Tile.SIZE) * Tile.SIZE;
        return new Point((int) Math.floor(mousePos.x/Tile.SIZE), (int) Math.floor(mousePos.y/Tile.SIZE));
    }

    private void processInput(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (inPlacementMode()) {
                exitPlacementMode();
            } else if (inMenu()) {
                closeMenu();
            } else {
                if (quitDialog != null) {
                    quitDialog.remove();
                    quitDialog = null;
                } else {
                    Dialog dialog = new Dialog("", Sprites.skin(), "small") {
                        @Override
                        protected void result(Object object) {
                            if (object.equals(false)) {
                                return;
                            }
                            if (gameManager != null) {
                                gameManager.save();
                            }
                            game.setScreen(new MainMenuScreen());
                        }
                    };
                    dialog.text("Are you sure you want to quit?", Sprites.skin().get("small", LabelStyle.class));
                    dialog.button("Yes", true, Sprites.skin().get("small", TextButton.TextButtonStyle.class));
                    dialog.button("No", false, Sprites.skin().get("small", TextButton.TextButtonStyle.class));
                    dialog.key(Input.Keys.ESCAPE, false);
                    dialog.pad(5);
                    dialog.show(stages.get(Align.center));
                    quitDialog = dialog;
                }
            }
        }

        stages.actAll(delta);

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            if (!inventoryButton.isDisabled()) {
                inventoryButton.toggle();
                shopWindow.setVisible(false);
                inventoryWindow.setVisible(inventoryButton.isChecked());
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            if (!shopButton.isDisabled()) {
                shopButton.toggle();
                shopWindow.setVisible(shopButton.isChecked());
                inventoryWindow.setVisible(false);
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.U)) {
            if (!upgradeButton.isDisabled()) {
                upgradeButton.toggle();
                inventoryWindow.setVisible(false);
                shopWindow.setVisible(false);
                enterUpgradeMode();
            }
        }

        int mx = Gdx.input.getX();
        int my = Gdx.input.getY();
        Vector2 mousePos = viewport.unproject(new Vector2(mx, my));
        mousePos.x = (int) Math.floor(mousePos.x/Tile.SIZE) * Tile.SIZE;
        mousePos.y = (int) Math.floor(mousePos.y/Tile.SIZE) * Tile.SIZE;

        int tileX = (int) Math.floor(mousePos.x/Tile.SIZE);
        int tileY = (int) Math.floor(mousePos.y/Tile.SIZE);
        
        if (inPlacementMode()) {
            updatePlacementMode(tileX, tileY);
        } else {
            if (Gdx.input.isKeyPressed(Input.Keys.R)) {
                // Sell the tower at the mouse position if it's on the same team
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    PurchaseItemPacket packet = new PurchaseItemPacket(PurchaseAction.SELL, null, tileX, tileY);
                    ioStream.sendPacket(packet);
                }
            }
        }

        processPlayerMovement(delta);
        processMouse(tileX, tileY);

        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            TowerEntity.displayRange = !TowerEntity.displayRange;
        }

        if (gameManager == null) {
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            boolean gamePaused = gameManager.areWavesPaused();
            if (gamePaused) {
                gameManager.resumeWaves();
            } else {
                gameManager.pauseWaves();
            }
        }
    }

    private void processMouse(int tileX, int tileY) {
        // TODO: if the player middle clicks, display the tower's stats
        if (Gdx.input.isButtonJustPressed(Input.Buttons.MIDDLE)) {
            TowerEntity tower = ShopManager.towerAt(world, tileX, tileY);
            if (tower != null) {
                // Display the tower's stats
                if (upgradeDialog != null) {
                    upgradeDialog.remove();
                    upgradeDialog = null;
                }
                TowerEntity.displayRange(tower);
                upgradeDialog = new Dialog(tower.getClass().getSimpleName(), Sprites.skin(), "small") {
                    @Override
                    protected void result(Object object) {
                        if (object.equals(true)) {
                            upgradeDialog(tower);
                        }
                        TowerEntity.displayRange = false;
                        TowerEntity.displayRangeTower = null;
                    }
                };
                upgradeDialog.getTitleLabel().setAlignment(Align.center);
                upgradeDialog.pad(10);
                upgradeDialog.padTop(30);
                upgradeDialog.setSize(200, 200);
                upgradeDialog.setMovable(true);
                upgradeDialog.setVisible(true);
                String text = "Level: " + tower.getLevel();
                text += "\nDamage: " + tower.getDisplayDamage();
                text += "\nRange: " + tower.getDisplayRange();
                text += "\nAttack Speed: " + tower.getAttackSpeed();
                text += "\nDescription: "+tower.getDescription();
                Label label = new Label(text, Sprites.skin(), "small");
                label.setFontScale(.25f);
                upgradeDialog.text(label);
                upgradeDialog.button("Upgrade", true, Sprites.skin().get("small", TextButton.TextButtonStyle.class));
                upgradeDialog.button("Close", false, Sprites.skin().get("small", TextButton.TextButtonStyle.class));
                upgradeDialog.key(Input.Keys.ESCAPE, false);
                upgradeDialog.show(stages.get(Align.center));
                upgradeDialog.setPosition(640 - 10, 180, Align.right);
            }
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

            if (inPlacementMode()) {
                Point mouseTile = getMouseTileCoordinate();
                batch.setColor(1, 1, 1, .5f);
                if (placementMode == PlacementMode.PLACE) {
                    entityToPlace.setPosition((mouseTile.x + .5f) * Tile.SIZE, (mouseTile.y + .5f) * Tile.SIZE);
                    entityToPlace.render(batch, delta);
                } else if (placementMode == PlacementMode.UPGRADE) {

                }
                batch.setColor(1, 1, 1, 1);
                batch.end();

                if (Settings.getPreferences().getBoolean(Settings.SHOW_PLACEMENT_GRID)) {
                    int mx = Gdx.input.getX();
                    int my = Gdx.input.getY();
                    Vector2 mousePos = viewport.unproject(new Vector2(mx, my));
                    mousePos.x = (int) Math.floor(mousePos.x/Tile.SIZE) * Tile.SIZE;
                    mousePos.y = (int) Math.floor(mousePos.y/Tile.SIZE) * Tile.SIZE;
                    
                    shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
                    shapeRenderer.begin(ShapeType.Line);
                    shapeRenderer.setColor(1, 1, 1, 1);
                    shapeRenderer.rect(mousePos.x, mousePos.y, Tile.SIZE, Tile.SIZE);
                    shapeRenderer.setColor(1, 1, 1, 1);
                    shapeRenderer.end();
                }
                batch.begin();
            }

            for (ParticleEffect effect : particleEffects) {
                effect.render(batch);
            }
            batch.end();
            batch.begin();
            worldStage.draw();
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
    public void dispose() {
        super.dispose();
        worldStage.dispose();
        shapeRenderer.dispose();
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

    public Entity getEntity(UUID id) {
        if (world == null) { return null; }
        return world.getEntity(id);
    }

    public void addEffect(ParticleEffect effect) {
        particleEffects.add(effect);
    }

    public void removeEffect(ParticleEffect effect) {
        particleEffects.remove(effect);
    }

    public World getWorld() {
        return world;
    }

    public boolean inMenu() {
        return shopButton.isChecked() || inventoryButton.isChecked() || upgradeDialog != null;
    }
    public void closeMenu() {
        shopButton.setChecked(false);
        inventoryButton.setChecked(false);
        shopWindow.setVisible(false);
        inventoryWindow.setVisible(false);
        if (upgradeDialog != null) {
            upgradeDialog.remove();
            upgradeDialog = null;
        }
    }

    HashSet<Integer> notificationsActive = new HashSet<Integer>();

    private void notification(SpriteType icon, String message) {
        // display a message at the bottom right corner
        HorizontalGroup group = new HorizontalGroup();

        int notificationPosition = 0;
        while (notificationsActive.contains(notificationPosition)) {
            notificationPosition++;
        }
        final int result = notificationPosition;
        notificationsActive.add(result);

        float targetY = 5 + 5 + 32 * result;

        group.space(5);
        group.setPosition(640+5, targetY, Align.bottomLeft);
        group.pad(5);
        Image iconImage = new Image(Sprites.drawable(icon));
        iconImage.setSize(32, 32);
        group.addActor(iconImage);
        Label label = new Label(message, Sprites.skin(), "small");
        label.setAlignment(Align.left);
        label.setFontScale(.4f);
        group.addActor(label);
        group.pack();

        add(Align.bottomRight, group);
        float fadeInSpeed = .5f;
        float fadeOutSpeed = .5f;
        group.addAction(Actions.sequence(
            Actions.parallel(Actions.fadeIn(fadeInSpeed), Actions.moveToAligned(640-5, targetY, Align.bottomRight, fadeInSpeed, Interpolation.swing)),
            Actions.delay(3),
            Actions.run(() -> {
                notificationsActive.remove(result);
            }),
            Actions.parallel(Actions.fadeOut(fadeOutSpeed), Actions.moveToAligned(640+5, targetY, Align.bottomLeft, fadeOutSpeed, Interpolation.swing)),
            Actions.removeActor()
        ));
    }

    private Table getTowerSection(TextureRegionDrawable drawable, GameType type, String name, String cost, String tooltipDescription) {
        Table table = new Table();
        Image towerImage = new Image(drawable);
        float fontScale = .5f;
        Label towerName = new Label(name, Sprites.skin(), "small");
        Label towerCost = new Label("Cost: $"+cost, Sprites.skin(), "small");
        towerName.setFontScale(fontScale);
        towerCost.setFontScale(fontScale);
        table.add(towerImage).expandX().size(32).pad(5);
        VerticalGroup group = new VerticalGroup();
        group.addActor(towerName);
        group.addActor(towerCost);
        table.add(group).expandX().pad(5);
        TextButton buyButton = new TextButton("Buy", Sprites.skin(), "small");
        buyButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // transition to the place tower mode
                enterPlacementMode(type);
            }
        });
        table.add(buyButton).size(32).expandX().pad(5);

        TextTooltip tooltip = new TextTooltip(tooltipDescription, Sprites.skin(), "small");
        tooltip.getContainer().pad(5);
        tooltip.getContainer().setScale(.25f);
        tooltip.getActor().setFontScale(.25f);
        tooltip.setInstant(true);
        towerImage.addListener(tooltip);

        return table;
    }

    public void enterUpgradeMode() {
        placementMode = PlacementMode.UPGRADE;
        shopButton.setDisabled(true);
        inventoryButton.setDisabled(true);
        upgradeButton.setDisabled(true);
        shopWindow.setVisible(false);
        inventoryWindow.setVisible(false);
    }

    public void enterPlacementMode(GameType type) {
        typeToPurchase = type;
        entityToPlace = type.createEntity();
        placementMode = PlacementMode.PLACE;
        shopButton.setDisabled(true);
        inventoryButton.setDisabled(true);
        upgradeButton.setDisabled(true);
        upgradeButton.setChecked(false);
        shopWindow.setVisible(false);
        inventoryWindow.setVisible(false);
    }

    public void exitPlacementMode() {
        typeToPurchase = null;
        entityToPlace = null;
        placementMode = PlacementMode.NONE;
        shopButton.setDisabled(false);
        inventoryButton.setDisabled(false);
        upgradeButton.setDisabled(false);
        upgradeButton.setChecked(false);
        shopWindow.setVisible(shopButton.isChecked());
        inventoryWindow.setVisible(inventoryButton.isChecked());
    }

    public boolean inPlacementMode() {
        return placementMode != PlacementMode.NONE;
    }

    public void updatePlacementMode(int tileX, int tileY) {
        boolean multiPlace = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            PurchaseItemPacket packet = null;
            if (placementMode == PlacementMode.PLACE) {
                packet = new PurchaseItemPacket(PurchaseAction.TOWER, typeToPurchase, tileX, tileY);
                ioStream.sendPacket(packet);
            } else if (placementMode == PlacementMode.UPGRADE) {
                TowerEntity towerAt = ShopManager.towerAt(world, tileX, tileY);
                if (towerAt == null) {
                    return;
                }
                if (towerAt.getRemainingUpgradeTime() >= 0) {
                    notification(SpriteType.STRUCTURE, "Tower is actively upgrading..." + (int) towerAt.getRemainingUpgradeTime() + "s");
                    return;
                }
                if (upgradeDialog != null) {
                    upgradeDialog.remove();
                    upgradeDialog = null;
                }

                // Open the upgrade dialog
                upgradeDialog(towerAt);
            }
            if (!multiPlace) {
                //exitPlacementMode();
            }
        }
    }

    public void upgradeDialog(TowerEntity towerAt) {
        int tileX = Entity.getTileX(towerAt.getX());
        int tileY = Entity.getTileY(towerAt.getY());
        long cost = ShopManager.upgradeCatalog.get(towerAt.type).cost.apply(towerAt.getLevel());
        TowerEntity.displayRange = false;
        TowerEntity.displayRangeTower = null;
        Dialog dialog = new Dialog("Upgrade?", Sprites.skin(), "small") {
            @Override
            protected void result(Object object) {
                if (object.equals(false)) {
                    return;
                }
                if (cost > localMoney) {
                    notification(SpriteType.CASH, "Not enough money to upgrade!");
                    return;
                }
                PurchaseItemPacket packet = new PurchaseItemPacket(PurchaseAction.UPGRADE, null, tileX, tileY);
                ioStream.sendPacket(packet);
                upgradeDialog = null;
            }
        };
        dialog.getTitleLabel().setAlignment(Align.center);
        dialog.getTitleLabel().setFontScale(.5f);
        dialog.pad(Tile.SIZE/2f);
        dialog.padTop(Tile.SIZE);
        dialog.setSize(Tile.SIZE*4f, Tile.SIZE*4f);
        dialog.setPosition((tileX + .5f) * Tile.SIZE, (tileY + 1.25f) * Tile.SIZE, Align.bottom);
        dialog.setMovable(false);
        dialog.setVisible(true);
        dialog.setKeepWithinStage(false);

        Label label = new Label("LVL "+(towerAt.getLevel()+1)+"\nCost: "+ShopManager.costToString(cost), Sprites.skin(), "small");
        label.setAlignment(Align.center);
        label.setFontScale(.45f);
        dialog.text(label);
        dialog.button("Yes", true, Sprites.skin().get("small", TextButton.TextButtonStyle.class));
        dialog.button("No", false, Sprites.skin().get("small", TextButton.TextButtonStyle.class));
        worldStage.addActor(dialog);
        dialog = upgradeDialog;
    }

}
