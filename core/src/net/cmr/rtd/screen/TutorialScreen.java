package net.cmr.rtd.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.cmr.rtd.RetroTowerDefense;
import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.stream.GameStream;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject.GameType;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.entities.towers.ShooterTower;
import net.cmr.rtd.game.world.tile.StructureTileData;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.util.Point;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.AnimationType;
import net.cmr.util.Sprites.SpriteType;

public class TutorialScreen extends GameScreen {

    public enum TutorialState {
        INTRO,
        CONTROLS,
        STRUCTURE,
        SPAWN_POINT,
        SHOW_MONEY,
        BUY_TOWER,
        PLACE_TOWER,
        UNPAUSE_WAVE,
        FINISH_WAVE,
        UPGRADE_TOWER,
        SELECT_TOWER_FOR_UPGRADE,
        UPGRADE_SUCCESS,
        INVENTORY_INTRO_1,
        INVENTORY_INTRO_2,
        INVENTORY_OPEN,
        INVENTORY_SELECT,
        COMPONENT_APPLY,
        INVENTORY_INTRO_3,
        INVENTORY_INTRO_4,
        INVENTORY_INTRO_5,
        INVENTORY_INTRO_6,
        SELL_INTRO,
        SELL,
        FINAL_INFO,
        FINISHED
    }

    TutorialState state;
    Table tutorialTable;
    Window tutorialWindow;
    TextButton continueButton;

    public TutorialScreen(GameStream ioStream, GameManager gameManager) {
        super(ioStream, gameManager, "", 0);
        state = TutorialState.INTRO;
        gameManager.setGameSpeed(1.5f);

        tutorialTable = new Table(Sprites.skin());
        tutorialWindow = new Window("Tutorial", Sprites.skin(), "small");
        tutorialWindow.getTitleLabel().setAlignment(Align.center);
        tutorialWindow.pad(10);
        tutorialWindow.padTop(30);
        tutorialWindow.add(tutorialTable).row();
        add(Align.center, tutorialWindow);

        continueButton = new TextButton("Continue", Sprites.skin(), "small");
        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                continueTutorial();
            }
        });
        continueButton.pad(0, 20, 0, 20);
        tutorialWindow.add(continueButton).padTop(10).row();

        setTableContent();
    }

    public void setTableContent() {
        tutorialTable.clear();
        continueButton.setVisible(false);

        switch (state) {
            case INTRO:
                tableText("Welcome to " + RetroTowerDefense.GAME_NAME + "!");
                tableText("This is an interactive tutorial to help you get started.");
                if (!isMobile()) {
                    tableText("Press 'Escape' at any time to exit the tutorial.");
                }
                tableText("Select the 'Continue' button to begin.");
                promptContinueButton();
                break;
            case CONTROLS:
                tableText("To move around in this world, use the");
                if (isMobile()) {
                    tableText("joystick in the bottom left corner of the screen.");
                } else {
                    tableText("WASD keys on your keyboard.");
                }
                break;
            case STRUCTURE:
                tableText("See this structure? Your goal is to");
                tableText("defend it from the incoming enemies.");
                tableText("The structure's life is displayed in");
                tableText("the top left corner. Once life reaches");
                tableText("0, the game is over.");
                promptContinueButton();
                break;
            case SPAWN_POINT:
                tableText("Enemies will spawn from this point.");
                tableText("They will follow the path towards your structure.");
                tableText("To protect your structure, you must place");
                tableText("towers along the path to defeat the enemies.");
                promptContinueButton();
                break;
            case SHOW_MONEY:
                tableText("You can purchase towers using money, which is");
                tableText("displayed in the top left corner.");
                tableText("To construct towers, select the 'Shop' button");
                tableText("at the bottom of the screen.");
                break;
            case BUY_TOWER:
                tableText("Purchase the 'Shooter Tower', the most basic tower.");
                break;
            case PLACE_TOWER:
                tableText("Place the tower on the map by selecting a tile.");
                tableText("Please place the tower on the targeted tile.");
                break;
            case UNPAUSE_WAVE:
                tableText("Excellent! The tower will be finished ");
                tableText("constructing in just a moment.");
                tableText("Once it is finished, select the resume button in the");
                tableText("right bottom corner to allow enemies to spawn.");
                break;
            case FINISH_WAVE:
                tableText("The enemies will begin to spawn!");
                tableText("Each kill will reward you with money");
                tableText("to construct more towers.");
                tableText("Survive this wave!");
                break;
            case UPGRADE_TOWER:
                tableText("Congratulations! You have survived your first wave!");
                tableText("With this extra money, you can upgrade your tower.");
                tableText("Select the 'Upgrade' button at the bottom of the screen.");
                break;
            case SELECT_TOWER_FOR_UPGRADE:
                tableText("Select the tower you wish to upgrade");
                tableText("and wait for it to finish upgrading.");
                break;
            case UPGRADE_SUCCESS:
                tableText("Great job! Your tower has been upgraded.");
                tableText("Upgraded towers are much more powerful.");
                break;
            case INVENTORY_INTRO_1:
                tableText("You have an inventory that stores");
                tableText("a variety of items, which can be used to");
                tableText("add abilities to towers, upgrade stats,");
                tableText("or even construct unique towers.");
                tableText("Let's explain what each item does.");
                break;
            case INVENTORY_INTRO_2:
                tableText("Components are items that can boost the");
                tableText("general stats of towers. Only one component");
                tableText("TYPE can be applied to a tower at a time, and");
                tableText("a max of "+ShooterTower.MAX_COMPONENTS+" components can be applied.");
                tableText("They are obtained by killing enemies.");
                break;
            case INVENTORY_OPEN:
                tableText("Let's apply a component to your tower!");
                tableText("Select the 'Inventory' button at the bottom of the screen.");
                break;
            case INVENTORY_SELECT: {
                tableText("Select the left top item in the inventory.");
                tableText("This is lubricant, one of the 3 types of components.");
                Image wd40 = new Image(Sprites.sprite(SpriteType.LUBRICANT));
                int size = 30;
                tutorialTable.add(wd40).size(size).colspan(2).row();
                break;
            }
            case COMPONENT_APPLY:
                tableText("Select the tower you wish to upgrade");
                tableText("and apply the component to it.");
                break;
            case INVENTORY_INTRO_3:
                tableText("Great job! Your tower has been lubricated,");
                tableText("which increases the tower's attack speed.");
                tableText("Scopes and scrap metal, the other two components,");
                tableText("increase the tower's range and damage, respectively.");
                break;
            case INVENTORY_INTRO_4: {
                tableText("The inventory also holds resource items, like");
                tableText("steel and titanium, which can be obtained by");
                tableText("creating mining towers on resource tiles.");
                tableText("Resources are only used to construct unique towers.");
                int size = 30;
                Image ironVein = new Image(Sprites.sprite("ironVein"));
                Image titaniumVein = new Image(Sprites.sprite("titaniumVein"));
                tableText("Iron vein (produces steel): ", false);
                tutorialTable.add(ironVein).size(size).row();
                tableText("Titanium vein:", false);
                tutorialTable.add(titaniumVein).size(size).row();
                break;
            }
            case INVENTORY_INTRO_5: {
                tableText("Lastly, the inventory holds gemstones.");
                tableText("They are drilled from gemstone deposits.");
                tableText("They can be applied to towers similar to components,");
                tableText("but only one can be applied at a time.");
                int size = 30;
                Image gemstoneVein = new Image(Sprites.sprite("gemstoneVein"));
                tutorialTable.add(gemstoneVein).size(size).colspan(2).row();
                break;
            }
            case INVENTORY_INTRO_6:
                tableText("Gemstones can provide unique abilities to towers,");
                tableText("such as stunning enemies, dealing piercing damage,");
                tableText("or trading damage for massive speed boosts.");
                tableText("Experiement to find the unique benefits of each gemstone!");
                break;
            case SELL_INTRO:
                tableText("Finally, to view a tower's stats, you can select it,");
                tableText("and a menu will appear on the right side of the screen.");
                break;
            case SELL:
                tableText("Here, you can see what components and gemstones");
                tableText("are applied to the tower, as well as the tower's stats.");
                tableText("In addition to these stats, you can also sell the tower.");
                tableText("Selling towers will refund all of the base cost, but");
                tableText("BE WARNED! You will lose ALL components and gemstones.");
                tableText("Please sell your tower.");
                break;
            case FINAL_INFO: {
                int size = 30;
                Image speedIcon = new Image(Sprites.sprite(SpriteType.SPEED_1));
                speedIcon.setSize(size, size);
                Image restartIcon = new Image(Sprites.sprite(SpriteType.RESTART));
                restartIcon.setSize(size, size);
                Image skipIcon = new Image(Sprites.sprite(SpriteType.SKIP));
                skipIcon.setSize(size, size);

                tableText("Here are some final tips to get you started:");
                tutorialTable.add(speedIcon).size(size).right().colspan(1);
                tableText("<- Used to change the speed of the game", true, 1);
                tutorialTable.add(restartIcon).size(size).right().colspan(1);
                tableText("<- Used to restart the game at any time", true, 1);
                tutorialTable.add(skipIcon).size(size).right().colspan(1);
                tableText("<- Skips the waiting time between waves", true, 1);
                tableText("- Unique enemies will spawn as you progress; some");
                tableText("  will be immune to cold or fire, some will heal their");
                tableText("  allies, and much more. Be prepared!");
                tableText("");
                break;
            }
            case FINISHED:
                tableText("Congratulations! You have completed the tutorial.");
                tableText("You are now ready to play " + RetroTowerDefense.GAME_NAME + "!");
                tableText("Select the 'Continue' button to return to the main menu.");
                break;
        }

        tutorialWindow.pack();
        tutorialWindow.setOrigin(Align.top);
        tutorialWindow.setScale(.75f);
        tutorialWindow.setPosition(640 / 2, 360 - 10, Align.top);
    }

    // CONTROLS
    boolean pressedW, pressedA, pressedS, pressedD;
    // PLACE TOWER
    int shooterTowerPositionX = 11, shooterTowerPositionY = 5;
    // INVENTORY INTRO 4
    int steelDepositX = 2, steelDepositY = 6;
    // INVENTORY INTRO 5
    int gemstoneDepositX = 5, gemstoneDepositY = 2;

    public boolean isTaskComplete() {
        boolean disableShop = state.ordinal() >= TutorialState.UNPAUSE_WAVE.ordinal();
        boolean disableResume = state.ordinal() >= TutorialState.FINISH_WAVE.ordinal();
        boolean disableUpgrade = state.ordinal() >= TutorialState.UPGRADE_SUCCESS.ordinal();
        boolean disableSidebar = state.ordinal() < TutorialState.INVENTORY_INTRO_6.ordinal();
        boolean disableInventory = state.ordinal() >= TutorialState.INVENTORY_INTRO_3.ordinal();

        shopButton.setDisabled(disableShop);
        wavePauseButton.setDisabled(disableResume);
        upgradeButton.setDisabled(disableUpgrade);
        inventoryButton.setDisabled(disableInventory);
        if (disableSidebar && TowerEntity.displayRangeTower != null) {
            removeInfoWindow();
        }

        switch (state) {
            case INTRO:
                return true;
            case CONTROLS:
                pressedW |= Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.W);
                pressedA |= Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.A);
                pressedS |= Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.S);
                pressedD |= Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.D);

                if (isMobile()) {
                    return joystick.getInputX() != 0 || joystick.getInputY() != 0;
                } else {
                    return pressedW && pressedA && pressedS && pressedD;
                }
            case STRUCTURE:
                return true;
            case SPAWN_POINT:
                return true;
            case SHOW_MONEY:
                if (shopWindow.isVisible()) {
                    continueTutorial();
                }
                return false;
            case BUY_TOWER:
                Entity toPlace = entityToPlace;
                if (toPlace instanceof ShooterTower) {
                    continueTutorial();
                } else if (toPlace != null) {
                    exitPlacementMode();
                    notification(SpriteType.SHOP_ICON, "Please purchase the Shooter Tower.");
                }
                return false;
            case PLACE_TOWER:
                // If there is a shooter tower in the world, the task is complete
                boolean hasShooterTower = false;
                for (Entity entity : gameManager.getWorld().getEntities()) {
                    if (entity instanceof ShooterTower) {
                        hasShooterTower = true;
                        break;
                    }
                }
                if (hasShooterTower) {
                    continueTutorial();
                    exitPlacementMode();
                    shopWindow.setVisible(false);
                    return false;
                } else {
                    enterPlacementMode(GameType.SHOOTER_TOWER);
                }
                return false;
            case UNPAUSE_WAVE:
                if (!gameManager.areWavesPaused()) {
                    continueTutorial();
                }
                return false;
            case FINISH_WAVE:
                int currentWave = gameManager.getWorld().getWave();
                if (currentWave >= 2) {
                    gameManager.pauseWaves();
                    return true;
                }
                return false;
            case UPGRADE_TOWER:
                if (placementMode == PlacementMode.UPGRADE) {
                    continueTutorial();
                }
                return false;
            case SELECT_TOWER_FOR_UPGRADE:
                for (Entity entity : gameManager.getWorld().getEntities()) {
                    if (entity instanceof ShooterTower) {
                        ShooterTower shooterTower = (ShooterTower) entity;
                        if (shooterTower.getLevel() == 2) {
                            continueTutorial();
                        }
                    }
                }
                return false;
            case UPGRADE_SUCCESS:
                return true;
            case INVENTORY_INTRO_1:
            case INVENTORY_INTRO_2:
                return true;
            case INVENTORY_OPEN:
                if (inventoryWindow.isVisible()) {
                    continueTutorial();
                }
                return false;
            case INVENTORY_SELECT:
                if (placementMode == PlacementMode.COMPONENT) {
                    continueTutorial();
                }
                return false;
            case COMPONENT_APPLY:
                for (Entity entity : gameManager.getWorld().getEntities()) {
                    if (entity instanceof ShooterTower) {
                        ShooterTower shooterTower = (ShooterTower) entity;
                        if (shooterTower.getComponentsApplied() > 0) {
                            continueTutorial();
                        }
                    }
                }
                return false;
            case INVENTORY_INTRO_3:
                return true;
            case INVENTORY_INTRO_4:
                focusTileX = steelDepositX;
                focusTileY = steelDepositY;
                return true;
            case INVENTORY_INTRO_5:
                focusTileX = gemstoneDepositX;
                focusTileY = gemstoneDepositY;
                return true;
            case INVENTORY_INTRO_6:
                return true;
            case SELL_INTRO:
                focusTileX = -1;
                focusTileY = -1;
                if (informationUpgradeWindow != null && TowerEntity.displayRangeTower != null) {
                    continueTutorial();
                }
                return false;
            case SELL:
                if (informationUpgradeWindow != null && TowerEntity.displayRangeTower != null) {
                    tutorialWindow.setOrigin(Align.left);
                    tutorialWindow.setPosition(10, 360 / 2, Align.left);
                } else {
                    tutorialWindow.setOrigin(Align.top);
                    tutorialWindow.setPosition(640 / 2, 360 - 10, Align.top);
                }

                boolean shooterTowerExists = false;
                for (Entity entity : gameManager.getWorld().getEntities()) {
                    if (entity instanceof ShooterTower) {
                        shooterTowerExists = true;
                        break;
                    }
                }
                if (!shooterTowerExists) {
                    continueTutorial();
                }
                return false;
            case FINAL_INFO:
                return true;
            case FINISHED:
                return true;
        }
        return false;
    }

    @Override
    public boolean allowedToPlaceTowerAt(UpdateData data, Entity tower, int tileX, int tileY) {
        if (state == TutorialState.PLACE_TOWER) {
            return tileX == shooterTowerPositionX && tileY == shooterTowerPositionY;
        }
        return super.allowedToPlaceTowerAt(data, tower, tileX, tileY);
    }

    public void onNewTaskAssigned() {
        switch (state) {
            case CONTROLS:
                if (joystick != null) {
                    joystick.setVisible(true);
                }
                break;
            case STRUCTURE:
                structureLifeLabel.setVisible(true);
                structureLife.setVisible(true);
                Point structurePos = gameManager.getTeam(0).getStructurePosition();
                focusTileX = structurePos.x;
                focusTileY = structurePos.y;
                break;
            case SPAWN_POINT:
                Point spawnPoint = gameManager.getTeam(0).getStartTilePosition();
                focusTileX = spawnPoint.x;
                focusTileY = spawnPoint.y;
                break;
            case SHOW_MONEY:
                cashLabel.setVisible(true);
                cash.setVisible(true);
                focusTileX = -1;
                focusTileY = -1;
                shopButton.setVisible(true);
                break;
            case BUY_TOWER:
                break;
            case PLACE_TOWER:
                focusTileX = shooterTowerPositionX;
                focusTileY = shooterTowerPositionY;
                break;
            case UNPAUSE_WAVE:
                wavePauseButton.setVisible(true);
                waveLabel.setVisible(true);
                waveCountdownLabel.setVisible(true);
                focusTileX = -1;
                focusTileY = -1;
                break;
            case FINISH_WAVE:
                wavePauseButton.setDisabled(true);
                break;
            case UPGRADE_TOWER:
                upgradeButton.setVisible(true);
                break;
            case SELECT_TOWER_FOR_UPGRADE:
                break;
            case UPGRADE_SUCCESS:
                break;
            case INVENTORY_INTRO_1:
            case INVENTORY_INTRO_2:
                break;
            case INVENTORY_OPEN:
                inventoryButton.setVisible(true);
                break;
            case INVENTORY_SELECT:
                gameManager.getTeam(0).getInventory().addWd40();
                break;
            case FINAL_INFO:
                skipButton.setVisible(true);
                speedButton.setVisible(true);
                restartButton.setVisible(true);
                restartButton.setDisabled(true);
                speedButton.setDisabled(true);
                break;
            default:
                break;
        }
    }

    private void tableText(String text) {
        tutorialTable.add(text, "small").colspan(2).row();
    }
    private void tableText(String text, boolean row) {
        tutorialTable.add(text, "small").colspan(1 + (row ? 1 : 0));
        if (row) {
            tutorialTable.row();
        }
    }
    private void tableText(String text, boolean row, int colspan) {
        tutorialTable.add(text, "small").colspan(colspan);
        if (row) {
            tutorialTable.row();
        }
    }

    @Override
    public void show() {
        super.show();
        settingsButton.setVisible(false);
        skipButton.setVisible(false);
        wavePauseButton.setVisible(false);
        restartButton.setVisible(false);
        speedButton.setVisible(false);

        upgradeButton.setVisible(false);
        shopButton.setVisible(false);
        inventoryButton.setVisible(false);

        waveCountdownLabel.setVisible(false);
        waveLabel.setVisible(false);

        structureLifeLabel.setVisible(false);
        structureLife.setVisible(false);
        cashLabel.setVisible(false);
        cash.setVisible(false);

        if (joystick != null) {
            joystick.setVisible(false);
        }

        uiToggleFromKeybinds = false;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (isTaskComplete()) {
            promptContinueButton();
        }
        StructureTileData structureTile = gameManager.getTeam(0).structure;
        structureTile.health = 50;
    }

    float targetElapsedTime = 0;

    @Override
    public void render(float delta) {
        super.render(delta);
        targetElapsedTime += delta;

        if (focusTileX != -1 && focusTileY != -1) {
            viewport.apply();
            batch.setProjectionMatrix(viewport.getCamera().combined);
            batch.begin();
            AnimationType animationType = AnimationType.TARGET;
            float size = Tile.SIZE * 1.5f;
            float centerX = focusTileX * Tile.SIZE + Tile.SIZE / 2f;
            float centerY = focusTileY * Tile.SIZE + Tile.SIZE / 2f;
            float leftBottomX = centerX - size / 2f;
            float leftBottomY = centerY - size / 2f;
            batch.draw(Sprites.animation(animationType, targetElapsedTime), leftBottomX, leftBottomY, size, size);
            batch.end();
        }
    }

    public void promptContinueButton() {
        if (!continueButton.isVisible()) {
            continueButton.setVisible(true);
        }
    }

    public void continueTutorial() {
        int next = state.ordinal() + 1;
        if (next < TutorialState.values().length) {
            state = TutorialState.values()[next];
            onNewTaskAssigned();
            setTableContent();
        } else {
            game.setScreen(new MainMenuScreen());
        }
    }

    int focusTileX = -1, focusTileY = -1;

    @Override
    public void updateCamera() {
        if (focusTileX != -1 && focusTileY != -1) {
            focusCameraOnTile(focusTileX, focusTileY);
        } else {
            super.updateCamera();
        }
    }

    public void focusCameraOnTile(int tx, int ty) {
        OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
        float x = (tx * Tile.SIZE) + Tile.SIZE / 2f;
        float y = (ty * Tile.SIZE) + Tile.SIZE / 2f;

        float lerpFactor = 1/10000f;
        float lerp = 1f - (float) Math.pow(lerpFactor, Gdx.graphics.getDeltaTime());
        x = Interpolation.linear.apply(camera.position.x, x, lerp);
        y = Interpolation.linear.apply(camera.position.y, y, lerp);
        camera.position.x = x;
        camera.position.y = y;
    }
    
}
