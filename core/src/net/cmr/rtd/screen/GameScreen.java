package net.cmr.rtd.screen;

import java.security.KeyPair;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
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
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.kryo.util.Null;

import net.cmr.rtd.ProjectTetraTD;
import net.cmr.rtd.ProjectTetraTD.LevelValueKey;
import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.GamePlayer;
import net.cmr.rtd.game.files.QuestFile;
import net.cmr.rtd.game.files.QuestTask;
import net.cmr.rtd.game.packets.AESEncryptionPacket;
import net.cmr.rtd.game.packets.ApplyMaterialPacket;
import net.cmr.rtd.game.packets.AttackPacket;
import net.cmr.rtd.game.packets.DisconnectPacket;
import net.cmr.rtd.game.packets.EffectPacket;
import net.cmr.rtd.game.packets.GameObjectPacket;
import net.cmr.rtd.game.packets.GameOverPacket;
import net.cmr.rtd.game.packets.GameResetPacket;
import net.cmr.rtd.game.packets.GameSpeedChangePacket;
import net.cmr.rtd.game.packets.JumpPacket;
import net.cmr.rtd.game.packets.Packet;
import net.cmr.rtd.game.packets.PacketEncryption;
import net.cmr.rtd.game.packets.PasswordPacket;
import net.cmr.rtd.game.packets.PlayerInputPacket;
import net.cmr.rtd.game.packets.PlayerPacket;
import net.cmr.rtd.game.packets.PlayerPositionsPacket;
import net.cmr.rtd.game.packets.PurchaseItemPacket;
import net.cmr.rtd.game.packets.PurchaseItemPacket.PurchaseAction;
import net.cmr.rtd.game.packets.RSAEncryptionPacket;
import net.cmr.rtd.game.packets.SetPlayerShopPacket;
import net.cmr.rtd.game.packets.SkipRequestPacket;
import net.cmr.rtd.game.packets.SortTypePacket;
import net.cmr.rtd.game.packets.StatsUpdatePacket;
import net.cmr.rtd.game.packets.TeamUpdatePacket;
import net.cmr.rtd.game.packets.WavePacket;
import net.cmr.rtd.game.storage.TeamInventory;
import net.cmr.rtd.game.storage.TeamInventory.Material;
import net.cmr.rtd.game.storage.TeamInventory.MaterialType;
import net.cmr.rtd.game.stream.GameStream;
import net.cmr.rtd.game.stream.GameStream.PacketListener;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.GameObject.GameType;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.rtd.game.world.entities.HealerEnemy;
import net.cmr.rtd.game.world.entities.HealerEnemy.HealerPacket;
import net.cmr.rtd.game.world.entities.MiningTower;
import net.cmr.rtd.game.world.entities.Player;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.entities.TowerEntity.SortType;
import net.cmr.rtd.game.world.particles.ParticleEffect;
import net.cmr.rtd.game.world.particles.SpreadEmitterEffect;
import net.cmr.rtd.game.world.store.Cost;
import net.cmr.rtd.game.world.store.ShopManager;
import net.cmr.rtd.game.world.store.TowerOption;
import net.cmr.rtd.game.world.store.UpgradeOption;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.game.world.tile.Tile.TileType;
import net.cmr.rtd.mobile.Joystick;
import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Audio;
import net.cmr.util.Audio.GameMusic;
import net.cmr.util.Audio.GameSFX;
import net.cmr.util.Log;
import net.cmr.util.Point;
import net.cmr.util.Settings;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.AnimationType;
import net.cmr.util.Sprites.SpriteType;

public class GameScreen extends AbstractScreenEX {
    
    public GameStream ioStream;
    @Null GameManager gameManager; // Will be null if the local player is not the host
    World world;
    Stage worldStage;
    Viewport viewport;
    ShapeRenderer shapeRenderer;
    UpdateData data;
    Player localPlayer = null;
    final String password;
    Label title;

    Label lifeLabel, structureLifeLabel, cashLabel, waveLabel, waveCountdownLabel;
    Image life, structureLife, cash;
    ImageButton upgradeButton, shopButton, inventoryButton;
    ImageButton wavePauseButton;
    ImageButton settingsButton, skipButton, restartButton, speedButton;
    //TextButton skipWaveButton;
    Window shopWindow, inventoryWindow, settings;

    GameType typeToPurchase;
    Entity entityToPlace;
    PlacementMode placementMode = PlacementMode.NONE;
    public enum PlacementMode {
        NONE, PLACE, UPGRADE, COMPONENT, GEMSTONE
    }

    ArrayList<ParticleEffect> particleEffects = new ArrayList<ParticleEffect>();

    Dialog quitDialog;
    Window informationUpgradeWindow;
    Dialog resetGameDialog;
    Dialog componentDialog;
    Dialog gameOverDialog;

    ArrayList<Entity> entityQueue = new ArrayList<Entity>();
    float waveCountdown = -1, waveDuration = 0;
    int wave = 0;
    boolean areWavesPaused = false;

    boolean emptyInventory = true;
    TeamInventory inventory = new TeamInventory();
    PurchaseAction componentAction = null;

    Joystick joystick;

    public static NinePatch upgradeProgress, upgradeProgressBackground;
    public final int team;
    public float gameSpeed;

    public Consumer<GameType[]> setShopContents;

    boolean uiToggleFromKeybinds = true;

    public GameScreen(GameStream ioStream, @Null GameManager gameManager, @Null String password, int team) {
        super(INITIALIZE_ALL);
        this.ioStream = ioStream;
        this.gameManager = gameManager;
        this.team = team;
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
            ProjectTetraTD instance = ProjectTetraTD.getInstance(ProjectTetraTD.class);
            instance.setLastPlayedTeam(team);
        } else {
            this.password = password;
        }
    }

    @Override
    public void show() {
        super.show();
        GameMusic random = GameMusic.random(GameMusic.GAME_1, GameMusic.GAME_2);
        Audio.getInstance().playMusic(random);

        float iconSize = 32;
        if (isMobile()) {
            iconSize += 16;
        }

        life = new Image(Sprites.drawable(SpriteType.HEART));
        life.setSize(iconSize, iconSize);
        life.setPosition(5, 360-5, Align.topLeft);

        lifeLabel = new Label("100", Sprites.skin(), "small");
        lifeLabel.setAlignment(Align.left);
        lifeLabel.setSize(iconSize, iconSize);
        lifeLabel.setPosition(15 + iconSize, 360-5, Align.topLeft);
        
        //add(Align.topLeft, life);
        //add(Align.topLeft, lifeLabel);

        cash = new Image(Sprites.drawable(SpriteType.CASH));
        cash.setSize(iconSize, iconSize);
        cash.setPosition(5, 360-5-iconSize+iconSize, Align.topLeft);

        cashLabel = new Label("100", Sprites.skin(), "small");
        cashLabel.setAlignment(Align.left);
        cashLabel.setSize(iconSize, iconSize);
        cashLabel.setPosition(15 + iconSize, 360-5-iconSize+iconSize, Align.topLeft);

        add(Align.topLeft, cash);
        add(Align.topLeft, cashLabel);

        structureLife = new Image(Sprites.drawable(SpriteType.HEART));
        structureLife.setSize(iconSize, iconSize);
        structureLife.setPosition(5, 360-5-iconSize*2+iconSize, Align.topLeft);

        structureLifeLabel = new Label("100", Sprites.skin(), "small");
        structureLifeLabel.setAlignment(Align.left);
        structureLifeLabel.setSize(iconSize, iconSize);
        structureLifeLabel.setPosition(15 + iconSize, 360-5-iconSize*2+iconSize, Align.topLeft);

        add(Align.topLeft, structureLife);
        add(Align.topLeft, structureLifeLabel);

        float waveLabelWidth = (iconSize * 5) + (5 * 2 * 2.5f);

        waveLabel = new Label("Waiting to start...", Sprites.skin(), "small");
        waveLabel.setAlignment(Align.right);
        waveLabel.setSize(waveLabelWidth, iconSize);
        waveLabel.setPosition(640-5, 360-5, Align.topRight);
        //waveLabel.setPosition(640-5, iconSize*1.65f, Align.bottomRight);

        add(Align.topRight, waveLabel);

        waveCountdownLabel = new Label("", Sprites.skin(), "small");
        waveCountdownLabel.setAlignment(Align.right);
        waveCountdownLabel.setSize(waveLabelWidth, iconSize);
        //waveCountdownLabel.setPosition(640-5, iconSize+3, Align.bottomRight);
        waveCountdownLabel.setPosition(640-5, 360-(5+iconSize/1.25f), Align.topRight);

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
        shopButton.setPosition(320, 5, Align.bottom);
        shopButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (shopButton.isDisabled()) { return; }
                // NOTE: when switching to shop screen, deselect any other screens that are open (i.e. inventory screen)
                shopWindow.setVisible(shopButton.isChecked());
                settings.setVisible(false);
                inventoryWindow.setVisible(false);
                Audio.getInstance().playSFX(GameSFX.CLICK, 1f);
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
        inventoryButton.setPosition(320 + (iconSize / 2) + (5), 5, Align.bottomLeft);
        inventoryButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (inventoryButton.isDisabled()) { return; }
                // NOTE: when switching to inventory screen, deselect any other screens that are open
                inventoryWindow.setVisible(inventoryButton.isChecked());
                shopWindow.setVisible(false);
                settings.setVisible(false);
                Audio.getInstance().playSFX(GameSFX.CLICK, 1f);
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
        style.imageUp = Sprites.drawable(SpriteType.UPGRADE);

        upgradeButton = new ImageButton(style);
        upgradeButton.setSize(iconSize, iconSize);
        upgradeButton.setPosition(320 - (iconSize / 2) - (5f), 5, Align.bottomRight);
        upgradeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (upgradeButton.isDisabled()) { return; }
                enterUpgradeMode();
                Audio.getInstance().playSFX(GameSFX.CLICK, 1f);
            }
        });
        buttonGroup.add(upgradeButton);

        add(Align.bottom, upgradeButton);

        TextButtonStyle style2 = new TextButtonStyle();
        int patch = 5;
        style2.down = new NinePatchDrawable(new NinePatch(Sprites.sprite(SpriteType.BORDER_DOWN), patch, patch, patch, patch));
        style2.up = new NinePatchDrawable(new NinePatch(Sprites.sprite(SpriteType.BORDER_DEFAULT), patch, patch, patch, patch));
        style2.over = new NinePatchDrawable(new NinePatch(Sprites.sprite(SpriteType.BORDER_HOVER), patch, patch, patch, patch));
        style2.checked = new NinePatchDrawable(new NinePatch(Sprites.sprite(SpriteType.BORDER_DEFAULT), patch, patch, patch, patch));
        style2.disabled = new NinePatchDrawable(new NinePatch(Sprites.sprite(SpriteType.BORDER_DISABLED), patch, patch, patch, patch));
        style2.font = Sprites.skin().get("small", LabelStyle.class).font;

        ImageButtonStyle smallButton = new ImageButtonStyle();
        smallButton.down = style2.checked;
        smallButton.up = style2.up;
        smallButton.over = style2.up;
        smallButton.checked = style2.checked;
        smallButton.disabled = style2.up;
        smallButton.imageUp = Sprites.drawable(SpriteType.CASH);

        // TODO: Add inventory menus and functionality
        shopWindow = new Window("Shop", Sprites.skin(), "small");
        shopWindow.getTitleLabel().setAlignment(Align.center);
        shopWindow.padTop(30);
        shopWindow.setSize(300, 250);
        shopWindow.setPosition(640 / 2, 360 / 2, Align.center);
        shopWindow.setMovable(false);
        shopWindow.setVisible(false);
        add(Align.center, shopWindow);

        Stack content = new Stack();

        Table combativeTable = new Table();
        Table miningTable = new Table();
        Table effectTable = new Table();
        content.add(combativeTable);
        content.add(miningTable);
        content.add(effectTable);

        ButtonGroup<TextButton> tabGroup = new ButtonGroup<TextButton>();
        HorizontalGroup shopTabs = new HorizontalGroup();
        TextButton combativeTab = new TextButton("Combat", Sprites.skin(), "toggle-small");
        combativeTab.setChecked(true);
        combativeTab.pad(0, 10, 0, 10);
        TextButton miningTab = new TextButton("Mining", Sprites.skin(), "toggle-small");
        miningTab.pad(0, 10, 0, 10);
        TextButton effectTab = new TextButton("Deployables", Sprites.skin(), "toggle-small");
        effectTab.pad(0, 10, 0, 10);
        shopTabs.addActor(combativeTab);
        shopTabs.addActor(miningTab);
        shopTabs.addActor(effectTab);
        shopTabs.space(5);
        shopWindow.add(shopTabs).growX().center().padLeft(5);
        shopWindow.row();

        Table contentTable = new Table();
        contentTable.add(content);
        shopWindow.add(contentTable).pad(5).padTop(0);
        shopWindow.row();

        ChangeListener tabListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                combativeTable.setVisible(combativeTab.isChecked());
                miningTable.setVisible(miningTab.isChecked());
                effectTable.setVisible(effectTab.isChecked());
            }
        };
        tabGroup.add(combativeTab, miningTab, effectTab);
        combativeTab.addListener(tabListener);
        miningTab.addListener(tabListener);
        effectTab.addListener(tabListener);
        tabGroup.setMaxCheckCount(1);
        tabGroup.setMinCheckCount(1);
        
        combativeTable.setVisible(combativeTab.isChecked());
        miningTable.setVisible(miningTab.isChecked());
        effectTable.setVisible(effectTab.isChecked());


        
        // TODO: Add all the shop items
        
        ScrollPaneStyle scrollStyle = new ScrollPaneStyle(Sprites.skin().get(ScrollPaneStyle.class));

        setShopContents = new Consumer<GameObject.GameType[]>() {
            @Override
            public void accept(GameType[] types) {
                combativeTable.clear();
                miningTable.clear();
                effectTable.clear();

                Table combativeShopTable = new Table();
                //combativeShopTable.setFillParent(true);
                ScrollPane combatScroll = new ScrollPane(combativeShopTable, scrollStyle);
                combatScroll.setScrollingDisabled(true, false);
                combatScroll.setScrollbarsVisible(false);
                combatScroll.setOverscroll(false, false);
                combativeTable.add(combatScroll).grow().pad(1).padTop(5);

                ArrayList<TowerOption> buyableTowers = new ArrayList<TowerOption>();
                for (GameType type : types) {
                    TowerOption option = ShopManager.getTowerCatalog().get(type);
                    if (option != null) {
                        buyableTowers.add(option);
                    }
                }

                ArrayList<TowerOption> combativeOptions = new ArrayList<TowerOption>(buyableTowers);
                combativeOptions.sort((a, b) -> a.order - b.order);
                combativeOptions.removeIf(option -> MiningTower.class.isAssignableFrom(option.type.getGameObjectClass()));
                for (TowerOption option : combativeOptions) {
                    GameType type = option.type;
                    Drawable drawable = option.sprite != null ? Sprites.drawable(option.sprite) : Sprites.drawable(option.animation);
                    Table towerSection = getTowerSection(drawable, type, option.name, option.cost, option.description);
                    combativeShopTable.add(towerSection).pad(5).growX();
                    combativeShopTable.row();
                }

                Table miningShopTable = new Table();
                ScrollPane miningScroll = new ScrollPane(miningShopTable, scrollStyle);
                miningScroll.setScrollingDisabled(true, false);
                miningScroll.setScrollbarsVisible(false);
                miningScroll.setOverscroll(false, false);
                miningTable.add(miningScroll).grow().pad(1).padTop(5);

                ArrayList<TowerOption> miningOptions = new ArrayList<TowerOption>(buyableTowers);
                miningOptions.sort((a, b) -> a.order - b.order);
                miningOptions.removeIf(option -> !MiningTower.class.isAssignableFrom(option.type.getGameObjectClass()));
                for (TowerOption option : miningOptions) {
                    GameType type = option.type;
                    Drawable drawable = option.sprite != null ? Sprites.drawable(option.sprite) : Sprites.drawable(option.animation);
                    Table towerSection = getTowerSection(drawable, type, option.name, option.cost, option.description);
                    miningShopTable.add(towerSection).pad(5).growX();
                    miningShopTable.row();
                }

                combativeTab.setDisabled(combativeOptions.size() < 0);
                miningTab.setDisabled(miningOptions.size() < 0);
                effectTab.setDisabled(true);

                combatScroll.layout();
                miningScroll.layout();
                shopWindow.pack();
                shopWindow.setPosition(640 / 2, 360 / 2, Align.center);
            }
        };

        inventoryWindow = new Window("Inventory", Sprites.skin(), "small");
        inventoryWindow.getTitleLabel().setAlignment(Align.center);
        inventoryWindow.padTop(30);
        inventoryWindow.setMovable(false);
        inventoryWindow.setVisible(false);
        
        Table inventoryTable = new Table();
        inventoryTable.setFillParent(true);
        int pad = 4;
        int imagePad = 0;

        TextButtonStyle transparentBackground = new TextButtonStyle();
        transparentBackground.font = Sprites.skin().get("small", LabelStyle.class).font;

        float size = 40; 
        int columns = 5;
        
        Label inventoryHelp = new Label("(Click and hover to view usage).", Sprites.skin(), "small");
        inventoryTable.add(inventoryHelp).growX().pad(5).colspan(columns);
        inventoryHelp.setFontScale(.35f);
        inventoryHelp.setAlignment(Align.center);
        inventoryTable.row();

        // NOTE: I noticed that if scrap was the last thing to be added to the inventory, it will NOT update until something else appears in the inventory
        String componentUsage = "- Can be applied to boost a tower's stats up to "+TowerEntity.MAX_COMPONENTS+" times.\n- Only one type of component can be applied to a tower at once.";
        inventoryTable.add(getInventorySlot(SpriteType.LUBRICANT, () -> getTeamInventory().getWd40(), () -> {
            Audio.getInstance().playSFX(GameSFX.SELECT, 1f);
            componentAction = PurchaseAction.APPLY_LUBRICANT;
            enterComponentMode();
        }, "Lubricant (Component)", componentUsage, size)).size(size);
        inventoryTable.add(getInventorySlot(SpriteType.SCOPE, () -> getTeamInventory().getScopes(), () -> {
            Audio.getInstance().playSFX(GameSFX.SELECT, 1f);
            componentAction = PurchaseAction.APPLY_SCOPE;
            enterComponentMode();
        }, "Scopes (Component)", componentUsage, size)).size(size);
        inventoryTable.add(getInventorySlot(SpriteType.SCRAP, () -> getTeamInventory().getScraps(), () -> {
            Audio.getInstance().playSFX(GameSFX.SELECT, 1f);
            componentAction = PurchaseAction.APPLY_SCRAP_METAL;
            enterComponentMode();
        }, "Scrap Metal (Component)", componentUsage, size)).size(size);
        String resourceUsage = "- Resource to build and upgrade special towers.";
        String gemstoneUsage = "- Used to specialize the stats of tower. \n- One gemstone per tower, gemstone will be lost if changed.";

        for (final Material material : Material.values()) {
            inventoryTable.add(getInventorySlot(material.image, () -> inventory.getMaterial(material), () -> {
                if (material.materialType == MaterialType.GEMSTONE) {
                    Audio.getInstance().playSFX(GameSFX.SELECT, 1f);
                    componentAction = PurchaseAction.APPLY_MATERIAL;
                    enterGemstoneMode(material);
                }
            }, material.materialName + (material.materialType == MaterialType.GEMSTONE ? " (Gemstone)" : " (Resource)"), material.materialType == MaterialType.GEMSTONE ? gemstoneUsage : resourceUsage, size)).size(size);
            // columns items per row
            if ((inventoryTable.getCells().size - 1) % columns == 0) {
                inventoryTable.row();
            }
        }

        inventoryWindow.setSize(230, 200);
        inventoryWindow.setOrigin(Align.center);
        inventoryWindow.setPosition(320, 360/2, Align.center);


        ImageButtonStyle waveButtonStyles = new ImageButtonStyle();
        waveButtonStyles.down = new NinePatchDrawable(new NinePatch(Sprites.sprite(SpriteType.BORDER_DOWN), patch, patch, patch, patch));
        waveButtonStyles.up = new NinePatchDrawable(new NinePatch(Sprites.sprite(SpriteType.BORDER_DEFAULT), patch, patch, patch, patch));
        waveButtonStyles.over = new NinePatchDrawable(new NinePatch(Sprites.sprite(SpriteType.BORDER_HOVER), patch, patch, patch, patch));
        waveButtonStyles.checkedOver = new NinePatchDrawable(new NinePatch(Sprites.sprite(SpriteType.BORDER_HOVER), patch, patch, patch, patch));
        waveButtonStyles.checked = new NinePatchDrawable(new NinePatch(Sprites.sprite(SpriteType.BORDER_DEFAULT), patch, patch, patch, patch));
        waveButtonStyles.disabled = new NinePatchDrawable(new NinePatch(Sprites.sprite(SpriteType.BORDER_DISABLED), patch, patch, patch, patch));
        ImageButtonStyle skipButtonStyle = new ImageButtonStyle(waveButtonStyles);
        skipButtonStyle.imageUp = Sprites.drawable(SpriteType.SKIP);
        skipButton = new ImageButton(skipButtonStyle) {
            boolean preping = false;
            boolean initialized = false;
            @Override
            public void act(float delta) {
                float preptime = waveCountdown - waveDuration;
                boolean lastPrep = preping;
                preping = preptime > 0 && !areWavesPaused && wave != 0;
                if (wave != 0 && (lastPrep != preping || !initialized || !areWavesPaused)) {
                    initialized = true;
                    setDisabled(false);
                }
                if (preptime <= 0 || areWavesPaused) {
                    setDisabled(true);
                }   

                super.act(delta);
            }
        };
        skipButton.setSize(iconSize*2, iconSize);
        skipButton.setPosition(640-5, 5, Align.bottomRight);
        skipButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (skipButton.isDisabled()) { return; }
                Audio.getInstance().playSFX(GameSFX.CLICK, 1f);
                voteSkip();
            }
        });
        add(Align.bottomRight, skipButton);
            
        ImageButtonStyle style3 = new ImageButtonStyle(waveButtonStyles);
        style3.imageChecked = Sprites.drawable(SpriteType.PAUSE);
        style3.imageUp = Sprites.drawable(SpriteType.RESUME);
        style3.checked = new NinePatchDrawable(
            new NinePatch(Sprites.sprite(SpriteType.BORDER_SELECTED), patch, patch, patch, patch));

        wavePauseButton = new ImageButton(style3);
        wavePauseButton.setSize(iconSize, iconSize);
        wavePauseButton.setPosition(640 - 10 - iconSize * 2, 5, Align.bottomRight);
        wavePauseButton.setDisabled(gameManager == null);
        wavePauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (wavePauseButton.isDisabled()) {
                    return;
                }
                if (!wavePauseButton.isChecked()) {
                    gameManager.pauseWaves();
                    Audio.getInstance().playSFX(GameSFX.DESELECT, 1f);
                } else {
                    gameManager.resumeWaves();
                    Audio.getInstance().playSFX(GameSFX.SELECT, 1f);
                }
            }
        });

        ImageButtonStyle style4 = new ImageButtonStyle(waveButtonStyles);
        style4.imageUp = Sprites.drawable(SpriteType.RESTART);
        restartButton = new ImageButton(style4);
        if (gameManager == null) {
            restartButton.setDisabled(true);
        }
        restartButton.setSize(iconSize, iconSize);
        restartButton.setPosition(640 - 5 * 3 - iconSize * 3, 5, Align.bottomRight);
        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (restartButton.isDisabled()) {
                    return;
                }
                Audio.getInstance().playSFX(GameSFX.CLICK, 1f);
                resetGameDialog.show(stages.get(Align.center));
            }
        });

        ImageButtonStyle speedStyle = new ImageButtonStyle(waveButtonStyles);
        speedStyle.imageChecked = Sprites.drawable(SpriteType.SPEED_2);
        speedStyle.imageUp = Sprites.drawable(SpriteType.SPEED_1);
        speedStyle.checked = new NinePatchDrawable(
                new NinePatch(Sprites.sprite(SpriteType.BORDER_SELECTED), patch, patch, patch, patch));

        speedButton = new ImageButton(speedStyle) {
            public void act(float delta) {
                setChecked(gameSpeed > 1);
                if (gameManager == null) {
                    setDisabled(true);
                }
                super.act(delta);
            };
        };
        speedButton.setSize(iconSize, iconSize);
        speedButton.setPosition(640 - 5 * 4 - iconSize * 4, 5, Align.bottomRight);
        speedButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Audio.getInstance().playSFX(GameSFX.CLICK, 1f);
                if (gameManager == null)
                    return;
                float currentSpeed = gameManager.getGameSpeed();
                float newSpeed = currentSpeed == 1 ? 2 : currentSpeed == 2 ? 1 : 1;
                gameManager.setGameSpeed(newSpeed);
            }
        });
        
        if (gameManager != null) {
            add(Align.bottomRight, wavePauseButton);
            add(Align.bottomRight, restartButton);
            add(Align.bottomRight, speedButton);
        }

        ImageButtonStyle settingsStyle = new ImageButtonStyle(waveButtonStyles);
        settingsStyle.imageUp = Sprites.drawable(SpriteType.SETTINGS);
        settingsStyle.checked = settingsStyle.up;
        settingsStyle.disabled = settingsStyle.up;
        settingsButton = new ImageButton(settingsStyle);
        settingsButton.setSize(iconSize, iconSize);
        settingsButton.setPosition(5, 5, Align.bottomLeft);
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (settingsButton.isDisabled()) { return; }
                settings.setVisible(!settings.isVisible());
                Audio.getInstance().playSFX(GameSFX.CLICK, 1f);
            }
        });
        add(Align.bottomLeft, settingsButton);

        inventoryWindow.add(inventoryTable).grow();

        add(Align.center, inventoryWindow);

        title = new Label("", Sprites.skin(), "small");
        title.setAlignment(Align.center);
        title.setPosition(640/2, 58+5, Align.bottom);
        title.setFontScale(.4f);
        add(Align.bottom, title);

        resetGameDialog = new Dialog("", Sprites.skin(), "small") {
            @Override
            protected void result(Object object) {
                if (object.equals(true)) {
                    gameManager.resetWorld();
                }
            }
        };
        resetGameDialog.pad(10f);
        resetGameDialog.text("Are you sure you want to reset the game?", Sprites.skin().get("small", LabelStyle.class));
        TextButton yes = new TextButton("Yes", Sprites.skin().get("small", TextButtonStyle.class));
        yes.pad(0, 10f, 0, 10f);
        TextButton no = new TextButton("No", Sprites.skin().get("small", TextButtonStyle.class));
        no.pad(0, 10f, 0, 10f);
        resetGameDialog.button(yes, true);
        resetGameDialog.button(no, false);
        resetGameDialog.key(Input.Keys.ESCAPE, false);

        upgradeProgress = new NinePatch(Sprites.sprite(SpriteType.UPGRADE_PROGRESS), 2, 2, 2, 2);
        upgradeProgressBackground = new NinePatch(Sprites.sprite(SpriteType.UPGRADE_PROGRESS_BACKGROUND), 2, 2, 2, 2);

        settings = new Window("", Sprites.skin(), "small");
        Table settingsTable = SettingsScreen.getSettingsTable(() -> {
            settings.setVisible(false);
        }, false);
        settingsTable.row();
        TextButton leave = new TextButton("Leave Game", Sprites.skin(), "small");
        leave.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settings.setVisible(false);
                showQuitDialog();
            }
        });
        settingsTable.add(leave).pad(5).padBottom(15).growX();

        settings.add(settingsTable).grow();
        settings.setSize(640, 380);
        settings.setScale(.6f);
        settings.setOrigin(Align.center);
        settings.setVisible(false);
        settings.setPosition(640/2, 360/2, Align.center);
        add(Align.center, settings);

        if (isMobile()) {
            joystick = new Joystick(Sprites.drawable(SpriteType.JOYSTICK), Sprites.drawable(SpriteType.JOYSTICK_BACKGROUND));
            joystick.setRadius(50);
            joystick.setKnobRadius(15);
            joystick.setPosition(iconSize + 5, iconSize / 1.5f, Align.bottomLeft);
            add(Align.bottomLeft, joystick);
        }

        /* Window testSpeedWindow = new Window("Test Speed", Sprites.skin(), "small");
        testSpeedWindow.getTitleLabel().setAlignment(Align.center);
        testSpeedWindow.padTop(30);
        testSpeedWindow.setSize(220, 100);
        testSpeedWindow.setPosition(220, 100, Align.center);
        testSpeedWindow.setMovable(true);
        testSpeedWindow.setVisible(true);
        TextField testSpeedField = new TextField("", Sprites.skin(), "small");
        testSpeedField.setAlignment(Align.center);
        testSpeedField.setTextFieldFilter(new TextFieldFilter.DigitsOnlyFilter());
        testSpeedField.setTextFieldListener(new TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                if (c == '\n') {
                    try {
                        float speed = Float.parseFloat(testSpeedField.getText());
                        if (gameManager != null) {
                            gameManager.setGameSpeed(speed);
                        }
                    } catch (NumberFormatException e) {

                    }
                }
            }
        });
        testSpeedWindow.add(testSpeedField).growX().pad(5);
        add(Align.center, testSpeedWindow); */

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
            // TODO: According to some tests, sometimes the server does not receive the AES data packet, which causes connections to bug out on the client side
            AESEncryptionPacket aesPacket = new AESEncryptionPacket(secretKey, iv);
            ioStream.sendPacket(aesPacket);

            // If the server requires a password, send it.
            if (password != null && !password.isEmpty()) {
                ioStream.sendPacket(new PasswordPacket(password));
            }

            return;
        }

        if (packet instanceof PlayerPositionsPacket) {
            PlayerPositionsPacket positionsPacket = (PlayerPositionsPacket) packet;
            Player localplayer = getLocalPlayer();
            String localUUID = localplayer != null ? localplayer.getID().toString() : "";
            for (int i = 0; i < positionsPacket.uuids.length; i++) {
                if (positionsPacket.uuids[i].equals(localUUID)) {
                    continue;
                }
                Player player = (Player) getEntity(UUID.fromString(positionsPacket.uuids[i]));
                if (player != null) {
                    player.setPosition(new Vector2(positionsPacket.positions[i]));
                    // TODO: On multiplayer, the player's sprint is not accounted for their velocity, which causes visual bugs in animations
                    player.setVelocity(new Vector2(positionsPacket.velocities[i]));
                }
            }
        }

        if (packet instanceof GameObjectPacket) {
            GameObjectPacket gameObjectPacket = (GameObjectPacket) packet;
            Log.debug("Received GameObject");
            GameObject object = gameObjectPacket.getObject();
            Log.debug("Object: " + object.getClass().getSimpleName(), object);
            if (object instanceof World) {
                this.world = (World) object;
                localPlayer = null;
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
                            //enemy.playHitSound(data, DamageType.PHYSICAL);
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

        if (packet instanceof GameSpeedChangePacket) {
            GameSpeedChangePacket speedPacket = (GameSpeedChangePacket) packet;
            if (speedPacket.speed > 0) {
                this.gameSpeed = speedPacket.speed;
            }
        }

        if (packet instanceof AttackPacket) {
            AttackPacket attackPacket = (AttackPacket) packet;
            Entity entity = getEntity(attackPacket.getTowerUUID());
            if (entity instanceof TowerEntity) {
                TowerEntity tower = (TowerEntity) entity;
                tower.onAttackClient(data);
            }
            return;
        }

        if (packet instanceof EffectPacket) {
            EffectPacket effectPacket = (EffectPacket) packet;
            effectPacket.apply(this);
        }

        if (packet instanceof StatsUpdatePacket) {
            StatsUpdatePacket statsPacket = (StatsUpdatePacket) packet;
            lifeLabel.setText(String.valueOf(statsPacket.getHealth()));
            cashLabel.setText(ShopManager.costToString(statsPacket.getInventory().getCash()).substring(1));
            structureLifeLabel.setText(String.valueOf(statsPacket.getStructureHealth()));

            inventory = statsPacket.getInventory();
            emptyInventory = false;
            return;
        }

        if (packet instanceof WavePacket) {
            WavePacket wavePacket = (WavePacket) packet;

            this.waveDuration = wavePacket.getWaveLength();
            this.waveCountdown = wavePacket.getDuration();
            this.wave = wavePacket.getWaveNumber();
            this.areWavesPaused = wavePacket.isPaused();
            wavePauseButton.setChecked(!areWavesPaused);

            if (wavePacket.shouldWarn()) {
                notification(SpriteType.WARNING, "Special wave incoming! Be careful!");
                Audio.getInstance().playSFX(GameSFX.SCARY_WARNING, 1f);
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

        if (packet instanceof HealerPacket) {
            HealerPacket healerPacket = (HealerPacket) packet;
            Entity entity = getEntity(healerPacket.healerId);
            if (entity instanceof HealerEnemy) {
                HealerEnemy healer = (HealerEnemy) entity;
                ParticleEffect effect = SpreadEmitterEffect.factory()
                    .setParticle(AnimationType.SPARKLE)
                    .setDuration(1.5f)
                    .setEmissionRate(15)
                    .setScale(.2f)
                    .setParticleLife(.5f)
                    .setAnimationSpeed(1.5f)
                    .setAreaSize(HealerEnemy.EFFECT_RADIUS * 2)
                    .setFollowEntity(true)
                    .setEntity(healer)
                    .create();
                particleEffects.add(effect);
                Audio.getInstance().worldSFX(GameSFX.UPGRADE_COMPLETE, .5f, 1.5f, healer.getPosition(), this);
            }
        }

        if (packet instanceof JumpPacket) {
            JumpPacket jumpPacket = (JumpPacket) packet;
            Entity entity = getEntity(jumpPacket.getPlayerUUID());
            if (entity instanceof Player) {
                Player player = (Player) entity;
                player.jump(data, true);
            }
            return;
        }

        if (packet instanceof SetPlayerShopPacket) {
            SetPlayerShopPacket shopPacket = (SetPlayerShopPacket) packet;
            GameType[] buyableTypes = shopPacket.types;
            setShopContents.accept(buyableTypes);
        }

        if (packet instanceof DisconnectPacket) {
            // The player was disconnected, set the screen to the main menu.
            DisconnectPacket disconnectPacket = (DisconnectPacket) packet;
            Log.info("Client disconnected: " + disconnectPacket.reason);
            MainMenuScreen mainMenu = new MainMenuScreen();
            game.setScreen(new MainMenuScreen());
            // TODO: Display why the player was disconnected with a dialog

            return;
        }

        if (packet instanceof TeamUpdatePacket) {
            TeamUpdatePacket teamPacket = (TeamUpdatePacket) packet;
            if (teamPacket.teamLost) {
                notification(SpriteType.STRUCTURE, "Team " + (teamPacket.getTeamIndex()+1) + " has lost their structure!", 10);
            } else {
                notification(SpriteType.STRUCTURE, "Team " + (teamPacket.getTeamIndex()+1) + " won the game!", 10);
                if (gameManager != null) {
                    //notification(SpriteType.STRUCTURE, "Game Over! Press ';' to restart the game.", 30);
                    String currentLife = structureLifeLabel.getText().toString();
                    // if current life is greater than zero, set the level as cleared
                    try {
                        int life = Integer.parseInt(currentLife);
                        if (life > 0) {
                            Log.info("Level successfully cleared!");
                            //RetroTowerDefense.setLevelCleared(gameManager.getQuest());
                            //RetroTowerDefense.setStoredLevelValue(questFile, LevelValueKey., currentLife);
                        }
                    } catch (NumberFormatException e) {
                        // Do nothing
                    }
                }
            }
        }

        if (packet instanceof GameResetPacket) {
            GameResetPacket resetPacket = (GameResetPacket) packet;
            if (gameOverDialog != null) {
                gameOverDialog.remove();
                gameOverDialog = null;
            }
        }

        if (packet instanceof GameOverPacket) {
            GameOverPacket gameOverPacket = (GameOverPacket) packet;
            Log.info("GAME IS NOW OVER: ended on wave "+gameOverPacket.endingWave+", score: "+gameOverPacket.score+", still alive: "+gameOverPacket.stillAlive);
            if (gameOverDialog != null) {
                gameOverDialog.remove();
                gameOverDialog = null;
            }
            gameOverDialog = new Dialog(gameOverPacket.stillAlive ? "YOU WON!" : "GAME OVER", Sprites.skin(), "small") {
                @Override
                protected void result(Object object) {
                    if (gameOverDialog != null) {
                        gameOverDialog.remove();
                        gameOverDialog = null;
                    }
                    if (object == null) return;
                    if (object instanceof Boolean && (Boolean) object) {
                        // Restart the game
                        if (gameManager == null) return;
                        gameManager.resetWorld();
                        return;
                    }

                    // Delete the save file (to remove the "resume" button)
                    if (gameManager != null) {
                        ProjectTetraTD game = (ProjectTetraTD) ProjectTetraTD.getInstance(ProjectTetraTD.class);
                        game.clearLastPlayedQuest();
                        gameManager.deleteSave();
                    }

                    MainMenuScreen mainMenu = new MainMenuScreen();
                    game.setScreen(mainMenu);
                }
            };
            gameOverDialog.getTitleLabel().setAlignment(Align.center);
            gameOverDialog.pad(30, 20, 10, 20);
            LabelStyle small = Sprites.skin().get("small", LabelStyle.class);


            Image trophy = new Image(Sprites.sprite(SpriteType.TROPHY));
            gameOverDialog.getContentTable().add(trophy).size(64).pad(5);
            gameOverDialog.getContentTable().row();

            gameOverDialog.text("- You made it to wave "+gameOverPacket.endingWave+"! -", small);
            gameOverDialog.getContentTable().row();
            gameOverDialog.text("Score: "+ShopManager.costToString(gameOverPacket.score).substring(1), small);
            gameOverDialog.getContentTable().row();
            if (storedCompleted != null && gameManager != null) {
                QuestTask[] tasks = gameManager.getQuest().getTasks();
                
                boolean taskCompleted = false;
                for (Long id : storedCompleted) {
                    for (QuestTask task : tasks) {
                        if (task.id == id && !alreadyCompletedTasks.contains(task.id)) {
                            gameOverDialog.text("Task \""+task.getReadableTaskDescription()+"\" Completed!", small);
                            gameOverDialog.getContentTable().row();
                            taskCompleted = true;
                        }
                    }
                }

                if (taskCompleted) {
                    // TODO: Verify if this is the correct way to calculate the remaining tasks
                    int completedTasks = storedCompleted.size();
                    int remainingTasks = tasks.length - completedTasks;
                    if (remainingTasks > 0) {
                        gameOverDialog.text("Remaining Tasks: "+completedTasks+"/"+tasks.length, small);
                    } else {
                        gameOverDialog.text("All tasks completed!", small);
                    }
                    gameOverDialog.getContentTable().row();
                }
            }

            // If competitive game, display the team win order
            if (gameOverPacket.teamWinOrder.length > 1) {
                for (int i = 0; i < gameOverPacket.teamWinOrder.length; i++) {
                    gameOverDialog.getContentTable().row();
                    
                    String suffix = "th";
                    if (i == 0) suffix = "st";
                    if (i == 1) suffix = "nd";
                    if (i == 2) suffix = "rd";

                    gameOverDialog.text((i+1) + suffix + " Place: Team "+(gameOverPacket.teamWinOrder[i] + 1), small);
                }
            } else {
                // Solo game. Set the high score if it is higher than the current high score.
                if (gameManager != null) {
                    Long storedHighScore = ProjectTetraTD.getStoredLevelValue(gameManager.getQuest(), LevelValueKey.HIGHSCORE, Long.class);
                    Long storedFarthestWave = ProjectTetraTD.getStoredLevelValue(gameManager.getQuest(), LevelValueKey.FARTHEST_WAVE, Long.class);
                    if (storedHighScore == null || gameOverPacket.score > storedHighScore) {
                        ProjectTetraTD.setStoredLevelValue(gameManager.getQuest(), LevelValueKey.HIGHSCORE, gameOverPacket.score);
                    }
                    if (storedFarthestWave == null || gameOverPacket.endingWave > storedFarthestWave) {
                        ProjectTetraTD.setStoredLevelValue(gameManager.getQuest(), LevelValueKey.FARTHEST_WAVE, gameOverPacket.endingWave);
                    }
                }
            }

            TextButton exit = new TextButton("Quit to Title", Sprites.skin().get("small", TextButtonStyle.class));
            exit.pad(0, 10, 0, 10);
            gameOverDialog.button(exit, false);
            if (gameManager != null) {
                TextButton restart = new TextButton("Restart", Sprites.skin().get("small", TextButtonStyle.class));
                restart.pad(0, 10, 0, 10);
                gameOverDialog.button(restart, true);
            } else {
                TextButton close = new TextButton("Close", Sprites.skin().get("small", TextButtonStyle.class));
                close.pad(0, 10, 0, 10);
                gameOverDialog.button(close, null);
                gameOverDialog.key(Input.Keys.ESCAPE, null);
            }
            gameOverDialog.key(Input.Keys.ESCAPE, null);

            gameOverDialog.show(stages.get(Align.center));
            // If game over packet is received and player is still alive, they have won the game
        }

    }

    int lastScopes = -1, lastWD40 = -1, lastScraps = -1;

    public void update(float delta) {
        worldStage.act(delta);
        ioStream.update();
        float speedDelta = delta * gameSpeed;

        if (areWavesPaused) {
            waveCountdownLabel.setText("(Paused)");
        } else if (waveCountdown != -1) {

            String waveText = "Wave " + wave;
            waveCountdown -= speedDelta;
            if (waveCountdown < 0) {
                waveCountdown = 0;
            }

            float preparationTime = (waveCountdown - waveDuration) / gameSpeed;
            float displayCountdown = waveCountdown / gameSpeed;
            if (preparationTime > 0) {
                displayCountdown = preparationTime;
                if (wave == 0) {
                    waveText = "Prepare to begin!";
                } else {
                    waveText = "Prepare for " + waveText;
                }
            }

            waveCountdownLabel.setText(String.format("%.2f", displayCountdown));
            waveLabel.setText(waveText);
        }
        
        if (inventory != null && !emptyInventory) {

            int scopes = inventory.getScopes();
            int wd40 = inventory.getWd40();
            int scraps = inventory.getScraps();

            if (scopes != lastScopes || wd40 != lastWD40 || scraps != lastScraps) {
                if (scopes > lastScopes && lastScopes != -1) {
                    notification(SpriteType.SCOPE, "Scope collected!", 3);
                }
                if (wd40 > lastWD40 && lastWD40 != -1) {
                    notification(SpriteType.LUBRICANT, "Lubricant collected!", 3);
                }
                if (scraps > lastScraps && lastScraps != -1) {
                    notification(SpriteType.SCRAP, "Scrap collected!", 3);
                }

                lastScopes = scopes;
                lastWD40 = wd40;
                lastScraps = scraps;
            }

        }

        if (world != null) {
            // FIXME: When the window is frozen, new game objects are not added to the world. This causes newly added enemies to be bunched up after the window is unfrozen.
            // TODO: Implement speed properly
            world.update(gameSpeed, delta, data);
        }
        processInput(delta);

        ArrayList<ParticleEffect> toRemove = new ArrayList<ParticleEffect>();
        for (int i = 0; i < particleEffects.size(); i++) {
            ParticleEffect effect = particleEffects.get(i);
            effect.update(this, speedDelta);
            if (effect.isParticleFinished()) {
                toRemove.add(effect);
            }
        }
        for (ParticleEffect effect : toRemove) {
            particleEffects.remove(effect);
        }
        
        updateCamera();

        updateTasks();
    }

    /**
     * To be used ONLY if the player is the host.
     */

    HashSet<Long> storedCompleted = null;
    HashSet<Long> alreadyCompletedTasks = null;
    public void updateTasks() {
        if (gameManager == null) return;
        if (storedCompleted == null) {
            storedCompleted = new HashSet<Long>();
            alreadyCompletedTasks = new HashSet<Long>();
            long[] completed = ProjectTetraTD.getStoredLevelValue(gameManager.getQuest(), LevelValueKey.COMPLETED_TASKS, long[].class);
            if (completed != null) {
                for (long id : completed) {
                    storedCompleted.add(id);
                }
                for (long id : completed) {
                    alreadyCompletedTasks.add(id);
                }
            }
        }
        QuestFile quest = gameManager.getQuest();
        if (quest == null) return;
        QuestTask[] tasks = quest.getTasks();
        if (tasks == null || tasks.length == 0) return;
        HashSet<Long> calculatedCompleted = quest.calculateCompletedTasks(team, gameManager.getUpdateData());
        HashSet<Long> justCompleted = quest.getJustCompletedTasks(storedCompleted, calculatedCompleted);

        // Add newly completed tasks to the completed list
        for (Long id : justCompleted) {
            storedCompleted.add(id);
        }

        if (justCompleted.size() == 0) return;
        for (Long id : justCompleted) {
            QuestTask task = QuestTask.getTask(quest, id);
            if (task == null) continue;
            String message = task.getReadableTaskDescription();
            if (message != null && !message.isEmpty()) {
                Audio.getInstance().playSFX(GameSFX.UPGRADE_COMPLETE, 1f, 1f);
                notification(SpriteType.DIAMOND, "Task completed! "+message, 5);
            }
        }

        long[] completed = storedCompleted.stream().mapToLong(l -> l).toArray();
        ProjectTetraTD.setStoredLevelValue(quest, LevelValueKey.COMPLETED_TASKS, completed);
    }

    /**
     * Updates the camera to center on the local player.
     */
    public void updateCamera() {
        // Get the local player's position and center the camera on it.
        if (world != null) {
            Player player = getLocalPlayer();
            if (player != null) {
                OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
                float x = player.getX() + player.getBounds().getWidth() / 2;
                float y = player.getY() + player.getBounds().getHeight() / 2;
                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    int sw = Gdx.graphics.getWidth();
                    int sh = Gdx.graphics.getHeight();
                    int mx = Gdx.input.getX();
                    int my = Gdx.input.getY();
                    int tempX = sw/2 - mx;
                    int tempY = sh/2 - my;
                    float scale = (1f/Tile.SIZE)*10;
                    tempX *= scale;
                    tempY *= scale;
                    x -= tempX;
                    y += tempY;
                }
                float lerpFactor = 1/10000f;
                float lerp = 1f - (float) Math.pow(lerpFactor, Gdx.graphics.getDeltaTime());
                x = Interpolation.linear.apply(camera.position.x, x, lerp);
                y = Interpolation.linear.apply(camera.position.y, y, lerp);
                camera.position.x = x;
                camera.position.y = y;
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

    boolean hideUI = false;

    private void processInput(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SEMICOLON) && gameManager != null) {
            resetGameDialog.show(stages.get(Align.center));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            hideUI = !hideUI;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F9) && ProjectTetraTD.getInstance(ProjectTetraTD.class).getUsername().equals("Cmrboy26")) {
            gameManager.setGameSpeed(4 * (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? 2 : 1));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            boolean successful = getLocalPlayer().jump(data);
            if (successful) {
                ioStream.sendPacket(new JumpPacket());
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (!skipButton.isDisabled()) {voteSkip();}
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (inPlacementMode()) {
                exitPlacementMode();
            } else if (inMenu()) {
                closeMenu();
            } else {
                showQuitDialog();
            }
        }

        stages.actAll(delta);

        // If the player should be allowed to toggle the UI from keybinds, do so
        if (uiToggleFromKeybinds) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
                if (!inventoryButton.isDisabled()) {
                    inventoryButton.toggle();
                    shopWindow.setVisible(false);
                    inventoryWindow.setVisible(inventoryButton.isChecked());
                    Audio.getInstance().playSFX(GameSFX.CLICK, 1f);
                }
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.F) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
                if (!shopButton.isDisabled()) {
                    shopButton.toggle();
                    shopWindow.setVisible(shopButton.isChecked());
                    inventoryWindow.setVisible(false);
                    Audio.getInstance().playSFX(GameSFX.CLICK, 1f);
                }
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.U) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
                if (!upgradeButton.isDisabled()) {
                    upgradeButton.toggle();
                    inventoryWindow.setVisible(false);
                    shopWindow.setVisible(false);
                    enterUpgradeMode();
                    Audio.getInstance().playSFX(GameSFX.CLICK, 1f);
                }
            }
        }

        int mx = Gdx.input.getX();
        int my = Gdx.input.getY();
        Vector2 mousePos = viewport.unproject(new Vector2(mx, my));
        mousePos.x = (int) Math.floor(mousePos.x/Tile.SIZE) * Tile.SIZE;
        mousePos.y = (int) Math.floor(mousePos.y/Tile.SIZE) * Tile.SIZE;

        int tileX = (int) Math.floor(mousePos.x/Tile.SIZE);
        int tileY = (int) Math.floor(mousePos.y/Tile.SIZE);

        processPlayerMovement(delta);
        processMouse(tileX, tileY);
        
        if (inPlacementMode()) {
            updatePlacementMode(tileX, tileY);
            String action = placementMode == PlacementMode.UPGRADE ? "upgrade tower" : (placementMode == PlacementMode.PLACE ? "place tower" : "set gemstone");
            if (placementMode == PlacementMode.COMPONENT) {
                action = "insert component";
            }
            if (placementMode != null) {
                String text = "Click to " + action;
                if (!isMobile()) {
                    text += "\n(Press ESC to cancel)";
                }
                setTitleText(text);
            }
        } else {
            setTitleText("");
            if (Gdx.input.isKeyPressed(Input.Keys.R) && uiToggleFromKeybinds) {
                // Sell the tower at the mouse position if it's on the same team
                setTitleText("Click to sell tower\n(Release R to cancel)");
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    PurchaseItemPacket packet = new PurchaseItemPacket(PurchaseAction.SELL, null, tileX, tileY);
                    Audio.getInstance().playSFX(GameSFX.SHOOT, 1f);
                    ioStream.sendPacket(packet);
                }
            } else {
                if (title.getText().toString().equals("Click to sell tower\n(Release R to cancel)")) {
                    setTitleText("");
                }
            }
        }

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

    int infoTowerX = -1, infoTowerY = -1;
    float infoWindowX = -1, infoWindowY = -1;

    private void processMouse(int tileX, int tileY) {
        //boolean openMenu = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        //if (isMobile()) {
        boolean openMenu = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) 
                        && placementMode == PlacementMode.NONE 
                        && (!inMenu() || informationUpgradeWindow != null) 
                        && !isOverJoystick()
                        && !isOverActor(informationUpgradeWindow)
                        && componentDialog == null
                        && !Gdx.input.isKeyPressed(Input.Keys.R);
        //}
        if (openMenu) {
            TowerEntity tower = ShopManager.towerAt(world, tileX, tileY, team);
            if (informationUpgradeWindow != null && ((tileX != infoTowerX || tileY != infoTowerY) ^ !(tileX == infoTowerX && tileY == infoTowerY && !informationUpgradeWindow.isVisible()))) {
                removeInfoWindow();
            } else {
                infoTowerX = tileX;
                infoTowerY = tileY;
                if (tower == null && informationUpgradeWindow != null) {
                    removeInfoWindow();
                } else if (tower != null) {
                    // Display the tower's stats
                    if (informationUpgradeWindow != null) {
                        removeInfoWindow();
                    }
                    TowerEntity.displayRange(tower);
                    boolean materialPresent = tower.getSelectedMaterial() != null;

                    informationUpgradeWindow = new Window(tower.getClass().getSimpleName(), Sprites.skin(), "small") {
                        @Override
                        public void act(float delta) {
                            super.act(delta);
                        }
                    };
                    // TODO: add the ability to send a sorttypepacket to change the targeting style of the selected tower
                    informationUpgradeWindow.getTitleLabel().setAlignment(Align.center);
                    informationUpgradeWindow.pad(10);
                    informationUpgradeWindow.padTop(30);
                    informationUpgradeWindow.setSize(225, 250);
                    informationUpgradeWindow.setPosition(640 - 10, 180, Align.right);
                    informationUpgradeWindow.setMovable(true);
                    informationUpgradeWindow.setVisible(true);
                    informationUpgradeWindow.setResizable(true);

                    Label label = new Label(tower.getTowerDescription(), Sprites.skin(), "small");
                    label.setWrap(true);
                    label.setFontScale(.2f);
                    label.setSize(200, 200);
                    informationUpgradeWindow.add(label).grow().colspan(2).row();

                    if (materialPresent) {
                        Image materialImage = new Image(Sprites.drawable(tower.getSelectedMaterial().image));
                        materialImage.setSize(24, 24);
                        informationUpgradeWindow.add(materialImage).size(24).colspan(1).expand(0, 0);
                    }

                    int buttonHeight = 20;
                    float pad = 3;
                    float spacing = 10;

                    if (tower.canEditSortType()) {
                        SelectBoxStyle style = new SelectBoxStyle(Sprites.skin().get("small", SelectBoxStyle.class));

                        style.scrollStyle.background = new NinePatchDrawable(Sprites.skin().get("box", NinePatch.class));
                        style.scrollStyle.background.setTopHeight(spacing / 2);
                        style.scrollStyle.background.setBottomHeight(spacing / 2);
                        style.scrollStyle.background.setLeftWidth(spacing / 2);
                        style.scrollStyle.background.setRightWidth(spacing / 2);
                        
                        SelectBox<String> targetMethodSelectBox = new SelectBox<String>(style);
                        targetMethodSelectBox.setScale(.8f);
                        targetMethodSelectBox.setAlignment(Align.center);
                        targetMethodSelectBox.setItems("Strongest", "Weakest", "First", "Last", "Closest", "Random");
                        switch (tower.getPreferedSortType()) {
                            case HIGHEST_HEALTH:
                                targetMethodSelectBox.setSelected("Strongest");
                                break;
                            case LOWEST_HEALTH:
                                targetMethodSelectBox.setSelected("Weakest");
                                break;
                            case STRUCTURE_DISTANCE:
                                targetMethodSelectBox.setSelected("First");
                                break;
                            case STRUCTURE_DISTANCE_REVERSE:
                                targetMethodSelectBox.setSelected("Last");
                                break;
                            case TOWER_DISTANCE:
                                targetMethodSelectBox.setSelected("Closest");
                                break;
                            case ANY:
                                targetMethodSelectBox.setSelected("Random");
                                break;
                            default:
                                break;
                        }
                        targetMethodSelectBox.addListener(new ChangeListener() {
                            @Override
                            public void changed(ChangeEvent event, Actor actor) {
                                Audio.getInstance().playSFX(GameSFX.SELECT, 1f);
                                SortType type = SortType.HIGHEST_HEALTH;
                                switch (targetMethodSelectBox.getSelected()) {
                                    case "Strongest":
                                        type = SortType.HIGHEST_HEALTH;
                                        break;
                                    case "Weakest":
                                        type = SortType.LOWEST_HEALTH;
                                        break;
                                    case "First":
                                        type = SortType.STRUCTURE_DISTANCE;
                                        break;
                                    case "Last":
                                        type = SortType.STRUCTURE_DISTANCE_REVERSE;
                                        break;
                                    case "Closest":
                                        type = SortType.TOWER_DISTANCE;
                                        break;
                                    case "Random":
                                        type = SortType.ANY;
                                        break;
                                }
                                SortTypePacket packet = new SortTypePacket(tileX, tileY, type);
                                ioStream.sendPacket(packet);
                            } 
                        });

                        informationUpgradeWindow.add(targetMethodSelectBox).pad(pad).maxHeight(buttonHeight).colspan(1 + (materialPresent ? 0 : 1)).growX().row();
                    }

                    TextButton upgradeButton = new TextButton("Upgrade", Sprites.skin(), "small");
                    upgradeButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            if (tower.isBeingBuilt() || tower.getRemainingUpgradeTime() > 0) {
                                notification(SpriteType.WARNING, "Tower is being upgraded!", 3);
                                Audio.getInstance().playSFX(GameSFX.WARNING, 1f);
                                return;
                            }
                            Audio.getInstance().playSFX(GameSFX.SELECT, 1f);
                            removeInfoWindow();
                            upgradeDialog(tower, false);
                        }
                    });
                    informationUpgradeWindow.add(upgradeButton).bottom().pad(pad).colspan(2).maxHeight(buttonHeight).growX().row();
                    informationUpgradeWindow.row();

                    TextButton sellButton = new TextButton("Sell", Sprites.skin(), "small");
                    sellButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            if (sellButton.isChecked()) {
                                Audio.getInstance().playSFX(GameSFX.WARNING, 1f);
                                sellButton.setText("Confirm?");
                            } else {
                                Audio.getInstance().playSFX(GameSFX.SHOOT, 1f);
                                PurchaseItemPacket packet = new PurchaseItemPacket(PurchaseAction.SELL, null, tileX, tileY);
                                ioStream.sendPacket(packet);
                                removeInfoWindow();
                            }
                        }
                    });
                    informationUpgradeWindow.add(sellButton).left().bottom().pad(pad).maxHeight(buttonHeight).growX().colspan(1);

                    TextButton closeButton = new TextButton("Close", Sprites.skin(), "small");
                    closeButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            Audio.getInstance().playSFX(GameSFX.DESELECT, 1f);
                            removeInfoWindow();
                        }
                    });
                    informationUpgradeWindow.add(closeButton).right().bottom().pad(pad).maxHeight(buttonHeight).growX();

                    informationUpgradeWindow.layout();

                    
                    stages.get(Align.center).addActor(informationUpgradeWindow);
                    if (infoWindowX != -1 && infoWindowY != -1) {
                        informationUpgradeWindow.setPosition(infoWindowX, infoWindowY);
                    } else {
                        informationUpgradeWindow.setPosition(640 - 10, 180, Align.right);
                    }
                }
            }
        }
    }

    void removeInfoWindow() {
        if (informationUpgradeWindow == null) {
            return;
        }
        //infoWindowX = informationWindow.getX();
        //infoWindowY = informationWindow.getY();
        informationUpgradeWindow.setVisible(false);
        informationUpgradeWindow.remove();
        informationUpgradeWindow = null;
        TowerEntity.displayRange = false;
        TowerEntity.displayRangeTower = null;
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
        if (isMobile()) {
            vx = joystick.getInputX();
            vy = joystick.getInputY();
            sprinting = true;
        }

        if (lastVelocityX != vx || lastVelocityY != vy || sprinting != lastSprinting) {
            // Something changed, send the new input to the server.
            PlayerInputPacket inputPacket = new PlayerInputPacket(new Vector2(vx, vy), getLocalPlayer().getPosition().cpy(), lastSprinting);
            ioStream.sendPacket(inputPacket);
        }

        lastVelocityX = vx;
        lastVelocityY = vy;

        getLocalPlayer().updateInput(new Vector2(vx, vy), sprinting);
    }

    float elapsedTime = 0;
    float alphaPulsePeriod = 2f;

    @Override
    public void render(float delta) {
        update(delta);
        elapsedTime += delta;
        elapsedTime %= alphaPulsePeriod;
        if (world != null) {
            viewport.apply();
            batch.setProjectionMatrix(viewport.getCamera().combined);
            batch.begin();
            world.render(data, batch, delta, gameSpeed);

            if (inPlacementMode()) {
                Point mouseTile = getMouseTileCoordinate();
                float factor = (float) (2f * Math.PI);
                float alpha = (float) Math.sin(factor * elapsedTime / alphaPulsePeriod);
                alpha = (1 / 4f) * (alpha + 2f);
                batch.setColor(1, 1, 1, alpha);
                if (placementMode == PlacementMode.PLACE) {
                    if (entityToPlace != null) {
                        entityToPlace.setPosition((mouseTile.x + .5f) * Tile.SIZE, (mouseTile.y + .5f) * Tile.SIZE);
                        entityToPlace.render(data, batch, delta);
                    }
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

        if (!hideUI) stages.drawAll(batch);
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
    public void pause() {
        super.pause();
        saveGame();
        if (isMobile()) {
            exitGame();
            stopGame();
            Audio.getInstance().stopMusic();
            MainMenuScreen mainMenu = new MainMenuScreen();
            game.setScreen(mainMenu);
        }
    }
    @Override
    public void hide() {
        super.hide();
        saveGame();
        exitGame();
        stopGame();
        Audio.getInstance().stopMusic();
    }

    private void saveGame() {
        if (gameManager != null) {
            gameManager.save();
        }
    }

    private void stopGame() {
        if (gameManager != null) {
            gameManager.stop();
        }
    }

    private void exitGame() {
        ioStream.sendPacket(new DisconnectPacket(GamePlayer.QUIT));
        ioStream.onClose();
    }

    public @Null Player getLocalPlayer() {
        // TODO: uncommenting this code sometimes freezes the player and "desyncs" the players actual position and the camera
        //if (localPlayer != null) {
        //    return localPlayer;
        //}
        if (world == null) { return null; }
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                if (player.getName().equals(((ProjectTetraTD) game).getUsername())) {
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
        return shopButton.isChecked() 
                || inventoryButton.isChecked() 
                || settings.isVisible() 
                || (informationUpgradeWindow != null && informationUpgradeWindow.isVisible())
                || gameOverDialog != null
                ;
    }
    public void closeMenu() {
        shopButton.setChecked(false);
        inventoryButton.setChecked(false);
        shopWindow.setVisible(false);
        inventoryWindow.setVisible(false);
        /*if (informationUpgradeWindow != null) {
            informationUpgradeWindow.remove();
            TowerEntity.displayRange = false;
            TowerEntity.displayRangeTower = null;
            informationUpgradeWindow = null;
        }*/
        removeInfoWindow();
        settings.setVisible(false);
    }

    HashSet<Integer> notificationsActive = new HashSet<Integer>();

    public void notification(SpriteType icon, String message) {
        notification(icon, message, 3);
    }

    public void notification(SpriteType icon, String message, int duration) {
        // display a message at the bottom right corner
        HorizontalGroup group = new HorizontalGroup();

        int notificationPosition = 0;
        while (notificationsActive.contains(notificationPosition)) {
            notificationPosition++;
        }
        final int result = notificationPosition;
        notificationsActive.add(result);

        float targetY = 5 + 5 + 32 * result + 40;

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
            Actions.delay(duration),
            Actions.run(() -> {
                notificationsActive.remove(result);
            }),
            Actions.parallel(Actions.fadeOut(fadeOutSpeed), Actions.moveToAligned(640+5, targetY, Align.bottomLeft, fadeOutSpeed, Interpolation.swing)),
            Actions.removeActor()
        ));
    }

    private Table getTowerSection(Drawable drawable, GameType type, String name, Cost cost, String tooltipDescription) {
        Table table = new Table();
        Image towerImage = new Image(drawable);
        float fontScale = .5f;
        //  + ": $"+cost.apply(0).getCash()
        Label towerName = new Label(name, Sprites.skin(), "small");
        towerName.setFontScale(fontScale);
        int targetHeight = 32;
        int targetWidth = (int) (targetHeight / (drawable.getMinHeight() / drawable.getMinWidth()));
        float sidepad = (targetHeight - targetWidth)/2f;
        table.add(towerImage).padRight(sidepad).padLeft(sidepad).width(targetWidth).height(targetHeight).colspan(1).left();
        VerticalGroup group = new VerticalGroup();
        group.addActor(towerName);
        group.addActor(getInventoryDisplay(cost.apply(0), false, 3, true));
        table.add(group).expandX().colspan(1);
        TextButton buyButton = new TextButton("Buy", Sprites.skin(), "small");
        buyButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // transition to the place tower mode
                enterPlacementMode(type);
                Audio.getInstance().playSFX(GameSFX.SELECT, 1);
            }
        });
        table.add(buyButton).width(40).height(32).padLeft(5).padRight(5).colspan(1).right();

        TextTooltip tooltip = new TextTooltip(tooltipDescription, Sprites.skin(), "small");
        tooltip.getContainer().pad(5);
        tooltip.getContainer().setScale(.25f);
        tooltip.getActor().setFontScale(.25f);
        tooltip.setInstant(true);
        towerImage.addListener(tooltip);
        buyButton.addListener(tooltip);
        towerName.addListener(tooltip);

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

    public void enterComponentMode() {
        placementMode = PlacementMode.COMPONENT;
        shopButton.setDisabled(true);
        inventoryButton.setDisabled(true);
        inventoryButton.setChecked(false);
        upgradeButton.setDisabled(true);
        shopWindow.setVisible(false);
        inventoryWindow.setVisible(false);
    }

    Material gemstoneMaterial;

    public void enterGemstoneMode(Material material) {
        gemstoneMaterial = material;
        placementMode = PlacementMode.GEMSTONE;
        shopButton.setDisabled(true);
        inventoryButton.setDisabled(true);
        upgradeButton.setDisabled(true);
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

    boolean lastTouched = false;
    boolean touchBeganOnJoystick = false;

    public void updatePlacementMode(int tileX, int tileY) {
        boolean multiPlace = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
        boolean placementConfirm = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);

        if (isMobile()) {
            boolean hitJoystick = isOverJoystick();
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                touchBeganOnJoystick = hitJoystick;
            }
            placementConfirm = !hitJoystick && !Gdx.input.isButtonPressed(Input.Buttons.LEFT) && lastTouched && !touchBeganOnJoystick;
            lastTouched = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        }

        if (placementConfirm) {
            PurchaseItemPacket packet = null;
            if (placementMode == PlacementMode.PLACE) {
                packet = new PurchaseItemPacket(PurchaseAction.TOWER, typeToPurchase, tileX, tileY);
                boolean canPlace = true;
                TowerOption option = ShopManager.towerCatalog.get(typeToPurchase);
                TowerEntity newTower = (TowerEntity) typeToPurchase.createEntity();
                TileType below = data.getWorld().getTile(tileX, tileY, 0);
                if (!allowedToPlaceTowerAt(data, newTower, tileX, tileY)) {
                    notification(SpriteType.STRUCTURE, "Cannot place here!");
                    canPlace = false;
                } else if (ShopManager.areTilesBlocking(data, tileX, tileY)) {
                    notification(SpriteType.STRUCTURE, "Cannot place here!");
                    canPlace = false;
                } else if (ShopManager.towerAt(world, tileX, tileY) != null) {
                    notification(SpriteType.STRUCTURE, "Tower already exists here!");
                    canPlace = false;
                } else if (newTower instanceof MiningTower && !((MiningTower) newTower).validMiningTarget(below)) {
                    notification(SpriteType.STRUCTURE, "This mining tower cannot\nbe placed here!");
                    canPlace = false;
                } else if (option != null) {
                    if (inventory.getCash() < option.cost.apply(0).getCash()) {
                        notification(SpriteType.CASH, "Not enough money! ($"+ShopManager.costToString(option.cost.apply(0).getCash()).substring(1)+")");
                        canPlace = false;
                    } else if (!option.cost.canPurchase(inventory)) {
                        notification(SpriteType.CASH, "Not enough resources!");
                        canPlace = false;
                    }
                }
                if (canPlace) {
                    ioStream.sendPacket(packet);
                    Audio.getInstance().playSFX(GameSFX.random(GameSFX.PLACE1, GameSFX.PLACE2), 1f);
                } else {
                    Audio.getInstance().playSFX(GameSFX.WARNING, 1);
                }
            } else if (placementMode == PlacementMode.UPGRADE) {
                TowerEntity towerAt = ShopManager.towerAt(world, tileX, tileY, team);
                if (towerAt == null) {
                    if (!multiPlace || isMobile()) {
                        exitPlacementMode();
                    }
                    return;
                }
                if (towerAt.getRemainingUpgradeTime() >= 0) {
                    notification(SpriteType.STRUCTURE, "Tower is upgrading..." + (int) towerAt.getRemainingUpgradeTime() + "s");
                    Audio.getInstance().playSFX(GameSFX.DESELECT, 1);
                    if (!multiPlace || isMobile()) {
                        exitPlacementMode();
                    }
                    return;
                }
                if (towerAt.isBeingBuilt()) {
                    notification(SpriteType.STRUCTURE, "Tower is being built..." + ((int) towerAt.getRemainingBuildTime()) + "s");
                    Audio.getInstance().playSFX(GameSFX.DESELECT, 1);
                    if (!multiPlace || isMobile()) {
                        exitPlacementMode();
                    }
                    return;
                }
                removeInfoWindow();

                // Open the upgrade dialog
                upgradeDialog(towerAt, multiPlace);
            } else if (placementMode == PlacementMode.COMPONENT) {
                TowerEntity towerAt = ShopManager.towerAt(world, tileX, tileY, team);
                if (towerAt == null) {
                    if (!multiPlace || isMobile()) {
                        exitPlacementMode();
                    }
                    return;
                }
                int tileX2 = Entity.getTileX(towerAt.getX());
                int tileY2 = Entity.getTileY(towerAt.getY());

                int totalApplied = Math.max(towerAt.getScopesApplied(), Math.max(towerAt.getLubricantApplied(), towerAt.getScrapMetalApplied()));
                if (totalApplied >= TowerEntity.MAX_COMPONENTS) {
                    notification(SpriteType.STRUCTURE, "Cannot apply more components!");
                    Audio.getInstance().playSFX(GameSFX.DESELECT, 1);
                    if (!multiPlace || isMobile()) {
                        exitPlacementMode();
                    }
                    return;
                }
                
                if (componentAction == PurchaseAction.APPLY_LUBRICANT && (towerAt.getScopesApplied() > 0 || towerAt.getScrapMetalApplied() > 0)) {
                    notification(SpriteType.STRUCTURE, "Cannot apply lubricant\nwith other components!");
                    Audio.getInstance().playSFX(GameSFX.DESELECT, 1);
                    if (!multiPlace || isMobile()) {
                        exitPlacementMode();
                    }
                    return;
                }
                if (componentAction == PurchaseAction.APPLY_SCOPE && (towerAt.getLubricantApplied() > 0 || towerAt.getScrapMetalApplied() > 0)) {
                    notification(SpriteType.STRUCTURE, "Cannot apply scope\nwith other components!");
                    Audio.getInstance().playSFX(GameSFX.DESELECT, 1);
                    if (!multiPlace || isMobile()) {
                        exitPlacementMode();
                    }
                    return;
                }
                if (componentAction == PurchaseAction.APPLY_SCRAP_METAL && (towerAt.getLubricantApplied() > 0 || towerAt.getScopesApplied() > 0)) {
                    notification(SpriteType.STRUCTURE, "Cannot apply scrap metal\nwith other components!");
                    Audio.getInstance().playSFX(GameSFX.DESELECT, 1);
                    if (!multiPlace || isMobile()) {
                        exitPlacementMode();
                    }
                    return;
                }

                if (componentAction == PurchaseAction.APPLY_LUBRICANT && !towerAt.canApplyComponentLubricant()) {
                    notification(SpriteType.STRUCTURE, "Cannot apply lubricant on\nthis tower type!");
                    Audio.getInstance().playSFX(GameSFX.DESELECT, 1);
                    if (!multiPlace || isMobile()) {
                        exitPlacementMode();
                    }
                    return;
                }

                if (componentAction == PurchaseAction.APPLY_SCOPE && !towerAt.canApplyComponentScope()) {
                    notification(SpriteType.STRUCTURE, "Cannot apply scope on\nthis tower type!");
                    Audio.getInstance().playSFX(GameSFX.DESELECT, 1);
                    if (!multiPlace || isMobile()) {
                        exitPlacementMode();
                    }
                    return;
                }

                if (componentAction == PurchaseAction.APPLY_SCRAP_METAL && !towerAt.canApplyComponentScrapMetal()) {
                    notification(SpriteType.STRUCTURE, "Cannot apply scrap metal on\nthis tower type!");
                    Audio.getInstance().playSFX(GameSFX.DESELECT, 1);
                    if (!multiPlace || isMobile()) {
                        exitPlacementMode();
                    }
                    return;
                }

                if (componentAction == PurchaseAction.APPLY_LUBRICANT && inventory.getWd40() <= 0) {
                    notification(SpriteType.STRUCTURE, "Not enough lubricant!");
                    Audio.getInstance().playSFX(GameSFX.DESELECT, 1);
                    if (!multiPlace || isMobile()) {
                        exitPlacementMode();
                    }
                    return;
                }
                if (componentAction == PurchaseAction.APPLY_SCOPE && inventory.getScopes() <= 0) {
                    notification(SpriteType.STRUCTURE, "Not enough scopes!");
                    Audio.getInstance().playSFX(GameSFX.DESELECT, 1);
                    if (!multiPlace || isMobile()) {
                        exitPlacementMode();
                    }
                    return;
                }
                if (componentAction == PurchaseAction.APPLY_SCRAP_METAL && inventory.getScraps() <= 0) {
                    notification(SpriteType.STRUCTURE, "Not enough scrap metal!");
                    Audio.getInstance().playSFX(GameSFX.DESELECT, 1);
                    if (!multiPlace || isMobile()) {
                        exitPlacementMode();
                    }
                    return;
                }

                if (componentDialog != null) {
                    componentDialog.remove();
                    componentDialog = null;
                }

                componentDialog = new Dialog("Component", Sprites.skin(), "small") {
                    @Override
                    protected void result(Object object) {
                        if (componentDialog != null) {
                            componentDialog.remove();
                            componentDialog = null;
                        }
                        if (object.equals(false)) {
                            Audio.getInstance().playSFX(GameSFX.DESELECT, 1);
                            return;
                        }
                        PurchaseItemPacket packet = new PurchaseItemPacket(componentAction, null, tileX, tileY);
                        ioStream.sendPacket(packet);
                        // Play sound
                        Audio.getInstance().playSFX(GameSFX.UPGRADE_COMPLETE, 1f);
                        // Display completed particle
                        /*SpreadEmitterEffect effect = SpreadEmitterEffect.factory()
                            .setParticle(AnimationType.SPARKLE)
                            .setDuration(1.5f)
                            .setEmissionRate(19)
                            .setScale(.225f)
                            .setParticleLife(.8f)
                            .setAnimationSpeed(1.5f)
                            .setAreaSize(1.2f)
                            .create();*/
                        ParticleEffect effect = createUpgradeEffect(towerAt);
                        //effect.setPosition(new Vector2(towerAt.getX(), towerAt.getY()));
                        data.getScreen().addEffect(effect);
                    }
                };
                componentDialog.getTitleLabel().setAlignment(Align.center);
                componentDialog.getTitleLabel().setFontScale(.5f);
                componentDialog.pad(Tile.SIZE/2f);
                componentDialog.padTop(Tile.SIZE);
                componentDialog.setSize(Tile.SIZE*5f, Tile.SIZE*4f);
                componentDialog.setPosition((tileX2 + .5f) * Tile.SIZE, (tileY2 + 1.25f) * Tile.SIZE, Align.bottom);
                componentDialog.setMovable(false);
                componentDialog.setVisible(true);
                componentDialog.setKeepWithinStage(false);
                String componentName = componentAction == PurchaseAction.APPLY_LUBRICANT ? "Lubricant" : (componentAction == PurchaseAction.APPLY_SCOPE ? "Scope" : "Scrap");
                
                Label label = new Label("Apply "+componentName+"?", Sprites.skin(), "small");
                label.setFontScale(.45f);
                componentDialog.text(label);
                componentDialog.getContentTable().row();
                Label totalLabel = new Label((totalApplied)+"/"+TowerEntity.MAX_COMPONENTS+" -> "+(totalApplied+1)+"/"+TowerEntity.MAX_COMPONENTS, Sprites.skin(), "small");
                totalLabel.setFontScale(.45f);
                componentDialog.text(totalLabel);

                TextButton yes = new TextButton("Yes", Sprites.skin().get("small", TextButton.TextButtonStyle.class));
                yes.pad(0, 5, 0, 5);
                componentDialog.button(yes, true);
                TextButton no = new TextButton("No", Sprites.skin().get("small", TextButton.TextButtonStyle.class));
                no.pad(0, 5, 0, 5);
                componentDialog.button(no, false);
                componentDialog.key(Input.Keys.ESCAPE, false);
                worldStage.addActor(componentDialog);
                
                //dialog.setPosition((tileX + .5f) * Tile.SIZE, (tileY + 1.25f) * Tile.SIZE, Align.bottom);
                //worldStage.addActor(dialog);
            } else if (placementMode == PlacementMode.GEMSTONE) {
                TowerEntity towerAt = ShopManager.towerAt(world, tileX, tileY, team);
                if (towerAt == null) {
                    if (!multiPlace || isMobile()) {
                        exitPlacementMode();
                    }
                    return;
                }
                if (!towerAt.canApplyMaterial(gemstoneMaterial)) {
                    notification(SpriteType.STRUCTURE, "Cannot apply this material\non this structure!");
                    Audio.getInstance().playSFX(GameSFX.DESELECT, 1);
                    if (!multiPlace || isMobile()) {
                        exitPlacementMode();
                    }
                    return;
                }
                if (!Cost.material(gemstoneMaterial, 1).canPurchase(inventory)) {
                    notification(SpriteType.CASH, "Not enough resources!");
                    Audio.getInstance().playSFX(GameSFX.DESELECT, 1);
                    if (!multiPlace || isMobile()) {
                        exitPlacementMode();
                    }
                    return;
                }
                inventoryButton.setChecked(false);
                gemstoneDialog(towerAt, gemstoneMaterial);
            }
        
            if (!multiPlace || isMobile()) {
                exitPlacementMode();
            }
        }
    }

    public void upgradeDialog(TowerEntity towerAt, boolean multiplace) {
        removeInfoWindow();
        int tileX = Entity.getTileX(towerAt.getX());
        int tileY = Entity.getTileY(towerAt.getY());
        long cost = ShopManager.upgradeCatalog.get(towerAt.type).cost.apply(towerAt.getLevel()).getCash();
        if (multiplace) {
            GameScreen.this.upgrade(towerAt, tileX, tileY);
            return;
        }

        TowerEntity.displayRange = false;
        TowerEntity.displayRangeTower = null;
        Dialog dialog = new Dialog("Upgrade?", Sprites.skin(), "small") {
            @Override
            protected void result(Object object) {
                removeInfoWindow();
                if (object.equals(false)) {
                    Audio.getInstance().playSFX(GameSFX.DESELECT, 1);
                    return;
                }
                GameScreen.this.upgrade(towerAt, tileX, tileY);
            }
        };
        dialog.getTitleLabel().setAlignment(Align.center);
        dialog.getTitleLabel().setFontScale(.5f);
        dialog.pad(Tile.SIZE/2f);
        dialog.padTop(Tile.SIZE);
        dialog.setSize(Tile.SIZE*4f, Tile.SIZE*2f);
        dialog.setMovable(false);
        dialog.setVisible(true);
        dialog.setKeepWithinStage(false);

        UpgradeOption option = ShopManager.getUpgradeCatalog().get(towerAt.type);
        float time = option.levelUpTime.apply(towerAt.getLevel()+1);
        time /= gameSpeed;

        String formatedTime = new DecimalFormat("#.#").format(time);

        Label label = new Label("LVL "+(towerAt.getLevel()+1)
            +"\nTime: "+formatedTime+"s", Sprites.skin(), "small");
        label.setAlignment(Align.center);
        label.setFontScale(.45f);
        dialog.text(label);
        Table costTable = getInventoryDisplay(option.cost.apply(towerAt.getLevel()), false, 3, false);
        dialog.getContentTable().row();
        dialog.getContentTable().add(costTable).center().grow().colspan(1).row();
        dialog.button("Yes", true, Sprites.skin().get("small", TextButton.TextButtonStyle.class));
        dialog.button("No", false, Sprites.skin().get("small", TextButton.TextButtonStyle.class));
        dialog.pack();
        dialog.setPosition((tileX + .5f) * Tile.SIZE, (tileY + 1.25f) * Tile.SIZE, Align.bottom);
        worldStage.addActor(dialog);
        informationUpgradeWindow = dialog;
    }

    // Returns true if the tower can be placed at the given tile
    // Used by TutorialScreen
    public boolean allowedToPlaceTowerAt(UpdateData data, Entity tower, int tileX, int tileY) {
        return true;
    }

    private void upgrade(TowerEntity entity, int tileX, int tileY) {
        TowerOption option = ShopManager.towerCatalog.get(entity.type);
        UpgradeOption upgradeOption = ShopManager.getUpgradeCatalog().get(entity.type);
        if (ShopManager.areTilesBlocking(data, tileX, tileY)) {
            notification(SpriteType.STRUCTURE, "Cannot place here!");
            return;
        } else if (option != null) {
            if (inventory.getCash() < upgradeOption.cost.apply(entity.getLevel()).getCash()) {
                Audio.getInstance().playSFX(GameSFX.WARNING, 1);
                notification(SpriteType.CASH, "Not enough money! ($"+ShopManager.costToString(upgradeOption.cost.apply(entity.getLevel()).getCash()).substring(1)+")");
                return;
            } else if (!upgradeOption.cost.canPurchase(inventory)) {
                Audio.getInstance().playSFX(GameSFX.WARNING, 1);
                notification(SpriteType.CASH, "Not enough resources!");
                return;
            }
        }
        Audio.getInstance().playSFX(GameSFX.SELECT, 1);
        PurchaseItemPacket packet = new PurchaseItemPacket(PurchaseAction.UPGRADE, null, tileX, tileY);
        ioStream.sendPacket(packet);
        Audio.getInstance().playSFX(GameSFX.random(GameSFX.PLACE1, GameSFX.PLACE2), 1);
        removeInfoWindow();
    }
    
    public void gemstoneDialog(TowerEntity entityAt, Material material) {
        int tileX = Entity.getTileX(entityAt.getX());
        int tileY = Entity.getTileY(entityAt.getY());
        
        removeInfoWindow();
        
        Dialog dialog = new Dialog("Apply "+material.materialName+"?", Sprites.skin(), "small") {
            @Override
            protected void result(Object object) {
                removeInfoWindow();
                if (!ShopManager.canApplyMaterial(material, world, inventory, tileX, tileY, team)) {
                    notification(SpriteType.CASH, "Can't apply the gemstone!\nTry again in a second!");
                    Audio.getInstance().playSFX(GameSFX.WARNING, 1);
                    return;
                }

                if (object.equals(false)) {
                    Audio.getInstance().playSFX(GameSFX.DESELECT, 1);
                    return;
                }
                ApplyMaterialPacket packet = new ApplyMaterialPacket(tileX, tileY, material);
                ioStream.sendPacket(packet);
                // Play sound
                Audio.getInstance().playSFX(GameSFX.UPGRADE_COMPLETE, 1f);
                // Display completed particle
                SpreadEmitterEffect effect = SpreadEmitterEffect.factory()
                    .setParticle(AnimationType.SPARKLE)
                    .setDuration(1.5f)
                    .setEmissionRate(19)
                    .setScale(.225f)
                    .setParticleLife(.8f)
                    .setAnimationSpeed(1.5f)
                    .setAreaSize(1.2f)
                    .create();
                effect.setPosition(new Vector2(entityAt.getX(), entityAt.getY()));
                data.getScreen().addEffect(effect);
            }
        };

        dialog.getTitleLabel().setAlignment(Align.center);
        dialog.getTitleLabel().setFontScale(.5f);
        dialog.pad(Tile.SIZE/2f);
        dialog.padTop(Tile.SIZE);
        dialog.setSize(Tile.SIZE*4f, Tile.SIZE*2f);
        dialog.setMovable(false);
        dialog.setVisible(true);
        dialog.setKeepWithinStage(false);
        if (entityAt.getSelectedMaterial() != null) {
            Label label = new Label("Will delete\nexisting gem!", Sprites.skin(), "small");
            label.setAlignment(Align.center);
            label.setFontScale(.4f);
            dialog.getContentTable().add(label).row();
        }

        /*Label label = new Label("Apply "+material.materialName+"?", Sprites.skin(), "small");
        label.setFontScale(.4f);
        dialog.text(label);*/
        Table costTable = getInventoryDisplay(new TeamInventory(), false, 3, false);
        dialog.getContentTable().row();
        dialog.getContentTable().add(costTable).center().grow().colspan(1).row();
        TextButton yes = new TextButton("Yes", Sprites.skin().get("small", TextButton.TextButtonStyle.class));
        yes.pad(0, 5, 0, 5);
        dialog.button(yes, true);
        TextButton no = new TextButton("No", Sprites.skin().get("small", TextButton.TextButtonStyle.class));
        no.pad(0, 5, 0, 5);
        dialog.button(no, false);
        dialog.pack();
        dialog.setPosition((tileX + .5f) * Tile.SIZE, (tileY + 1.25f) * Tile.SIZE, Align.bottom);
        worldStage.addActor(dialog);

        informationUpgradeWindow = dialog;
    }

    public void setTitleText(String string) {
        title.setText(string);
    }

    public void voteSkip() {
        skipButton.setDisabled(true);
        skipButton.setChecked(true);
        ioStream.sendPacket(new SkipRequestPacket());
    }

    public boolean isMobile() {
        return ProjectTetraTD.isMobile();
    }

    public boolean isOverJoystick() {
        if (joystick == null) {
            return false;
        }
        Vector2 input = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        Vector2 unprojected = joystick.screenToLocalCoordinates(input);
        boolean hitJoystick = joystick.hit(unprojected.x, unprojected.y, true) != null;
        return hitJoystick;
    }

    public boolean overMenuIcons() {
        return isOverActor(shopButton) || isOverActor(inventoryButton) || isOverActor(upgradeButton) || isOverActor(skipButton);
    }

    public boolean isOverActor(Actor actor) {
        if (actor == null) {
            return false;
        }
        Vector2 input = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        Vector2 unprojected = actor.screenToLocalCoordinates(input);
        boolean hitActor = actor.hit(unprojected.x, unprojected.y, true) != null;
        return hitActor;
    }

    public void showQuitDialog() {
        if (quitDialog != null) {
            quitDialog.remove();
            quitDialog = null;
        } else {
            Dialog dialog = new Dialog("", Sprites.skin(), "small") {
                @Override
                protected void result(Object object) {
                    if (object == null || object.equals(false)) {
                        if (quitDialog == null) {
                            return;
                        }
                        quitDialog.remove();
                        quitDialog = null;
                        return;
                    }
                    if (gameManager != null && !(GameScreen.this instanceof TutorialScreen)) {
                        gameManager.save();
                    }
                    game.setScreen(new MainMenuScreen());
                }
            };
            dialog.text("Are you sure you want to quit?", Sprites.skin().get("small", LabelStyle.class));
            TextButton button = new TextButton("Yes", Sprites.skin().get("small", TextButton.TextButtonStyle.class));
            button.pad(0, 10, 0, 10);
            dialog.button(button, true);
            TextButton button2 = new TextButton("No", Sprites.skin().get("small", TextButton.TextButtonStyle.class));
            button2.pad(0, 10, 0, 10);
            dialog.button(button2, false);
            dialog.key(Input.Keys.ESCAPE, false);
            dialog.pad(5);
            dialog.show(stages.get(Align.center));
            quitDialog = dialog;
        }
    }

    int tempColumns = 0;
    int targetColumns = 0;

    public Table getInventoryDisplay(TeamInventory displayInventory, boolean displayAll, int columns, boolean includeTooltips) {
        Table table = new Table();
        tempColumns = 0;
        targetColumns = columns;
        if (displayAll || displayInventory.cash > 0) addInventorySection("", SpriteType.CASH, displayInventory.cash, table, includeTooltips);
        if (displayAll || displayInventory.scopes > 0) addInventorySection(" scopes", SpriteType.SCOPE, displayInventory.scopes, table, includeTooltips);
        if (displayAll || displayInventory.wd40 > 0) addInventorySection(" lubricant", SpriteType.LUBRICANT, displayInventory.wd40, table, includeTooltips);
        if (displayAll || displayInventory.scrapMetal > 0) addInventorySection(" scrap metal", SpriteType.SCRAP, displayInventory.scrapMetal, table, includeTooltips);

        /*if (displayAll || displayInventory.steel > 0) addInventorySection(" steel", SpriteType.STEEL, displayInventory.steel, table, includeTooltips);
        if (displayAll || displayInventory.titanium > 0) addInventorySection(" titanium", SpriteType.TITANIUM, displayInventory.titanium, table, includeTooltips);*/
        for (Material material : Material.values()) {
            if (displayAll || displayInventory.getMaterial(material) > 0) {
                addInventorySection(" "+material.materialName.toLowerCase(), material.image, displayInventory.getMaterial(material), table, includeTooltips);
            }
        }

        table.padTop(0);
        table.padBottom(0);
        return table;
    }

    private void addInventorySection(String name, SpriteType type, long amount, Table table, boolean includeTooltips) {

        Image image = new Image(Sprites.drawable(type));

        image.setSize(16, 16);
        table.add(image).width(16).height(16).pad(1);
        String prefix = "x";
        if (type == SpriteType.CASH) {
            prefix = "";
        }
        
        Label label = new Label(prefix+ShopManager.costToString(amount).substring(1), Sprites.skin(), "small");
        label.setFontScale(.4f);
        table.add(label).pad(1).center();
        tempColumns++;
        if (tempColumns >= targetColumns) {
            table.row();
            tempColumns = 0;
        }

        TextTooltip tooltip = new TextTooltip((prefix.isEmpty() ? "$" : prefix)+ShopManager.costToString(amount).substring(1) + name, Sprites.skin(), "small");
        tooltip.getContainer().pad(5);
        tooltip.setInstant(true);
        label.addListener(tooltip);
        image.addListener(tooltip);
    }

    private Stack getInventorySlot(SpriteType type, final Supplier<Integer> amount, Runnable onClick, String name, String usage, float size) {
        Stack stack = new Stack();
        Image image = new Image(Sprites.drawable(type));
        image.setFillParent(true);
        image.setSize(size, size);
        stack.add(image);
        TextButtonStyle style = new TextButtonStyle();
        style.font = Sprites.skin().get("small", TextButtonStyle.class).font;
        
        TextButton label = new TextButton(ShopManager.costToString(amount.get()).substring(1), style) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                setText(ShopManager.costToString(amount.get()).substring(1));
                super.draw(batch, parentAlpha);
            }
        };
        label.getLabel().setFontScale(.35f);
        label.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onClick.run();
            }
        });
        TextTooltip tooltip = new TextTooltip(name+"\n"+usage, Sprites.skin(), "small");
        tooltip.getManager().animations = false;
        tooltip.getContainer().pad(5);
        tooltip.getActor().setFontScale(.4f);
        tooltip.setInstant(true);
        label.addListener(tooltip);
        label.setFillParent(true);
        label.setSize(size, size);
        stack.add(label);
        stack.setSize(size, size);
        return stack;
    }

    public TeamInventory getTeamInventory() {
        return inventory;
    }

    public static ParticleEffect createFinishedEffect(TowerEntity tower, SpriteType type) {
        SpreadEmitterEffect effect = SpreadEmitterEffect.factory()
            .setParticle(type)
            .setDuration(1.5f)
            .setEmissionRate(19)
            .setScale(.225f)
            .setParticleLife(.8f)
            .setAnimationSpeed(1.5f)
            .setAreaSize(1.2f)
            .create();
        effect.setPosition(new Vector2(tower.getX(), tower.getY()));
        return effect;
    }

    public static ParticleEffect createUpgradeEffect(TowerEntity tower) {
        SpreadEmitterEffect effect = SpreadEmitterEffect.factory()
            .setParticle(AnimationType.SPARKLE)
            .setDuration(1.5f)
            .setEmissionRate(19)
            .setScale(.225f)
            .setParticleLife(.8f)
            .setAnimationSpeed(1.5f)
            .setAreaSize(1.2f)
            .create();
        effect.setPosition(new Vector2(tower.getX(), tower.getY()));
        return effect;
    }

}
