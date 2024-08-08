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

import net.cmr.rtd.ProjectTetraTD;
import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.achievements.AchievementManager;
import net.cmr.rtd.game.achievements.custom.TutorialCompleteAchievement;
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

        int size = 30;
        switch (state) {
            case INTRO:
                tableText("Welcome to " + ProjectTetraTD.GAME_NAME + "!");
                tableText("This is an interactive tutorial to help you get started.");
                if (!isMobile()) {
                    tableText("Press 'Escape' at any time to exit the tutorial.");
                }
                tableText("Select the 'Continue' button to begin.");
                promptContinueButton();
                break;
            case CONTROLS:
                tableText("To move around in the world, use the");
                if (isMobile()) {
                    tableText("joystick in the bottom left corner of the screen.");
                } else {
                    tableText("WASD keys on your keyboard.");
                }
                break;
            case STRUCTURE:
                tableText("See this structure? Your goal is to");
                tableText("defend it from enemies for as long as possible.");
                tableText("When the structure's life (displayed in the left top)");
                tableText("reaches 0, you will lose the game.");
                promptContinueButton();
                break;
            case SPAWN_POINT:
                tableText("Enemies will spawn from this point and");
                tableText("follow the path towards your structure.");
                tableText("To protect your structure, you must");
                tableText("construct towers to defeat the enemies.");
                promptContinueButton();
                break;
            case SHOW_MONEY:
                tableText("You can purchase towers using money, which is");
                tableText("displayed in the top left corner.");
                tableText("To construct towers, touch the 'Shop' button");
                tableText("at the bottom of the screen.");
                Image image = new Image(Sprites.sprite(SpriteType.SHOP_ICON));
                Image money = new Image(Sprites.sprite(SpriteType.CASH));
                tutorialTable.add(money).size(size).colspan(1);
                tutorialTable.add(image).size(size).colspan(1).row();
                break;
            case BUY_TOWER:
                tableText("Purchase the 'Shooter Tower', the most basic tower.");
                Image shooterTower = new Image(Sprites.animation(AnimationType.SHOOTER_TOWER_1, 0));
                tutorialTable.add(shooterTower).size(size).colspan(2).row();
                break;
            case PLACE_TOWER:
                tableText("Place the tower on the map by touching a tile.");
                tableText("Please place the tower on the highlighted tile.");
                break;
            case UNPAUSE_WAVE:
                tableText("Excellent! The tower will be finished ");
                tableText("constructing in just a moment.");
                tableText("Once it is finished, touch the resume button");
                tableText("(right bottom) to summon a wave of enemies.");
                Image resume = new Image(Sprites.sprite(SpriteType.RESUME));
                tutorialTable.add(resume).size(size).colspan(2).row();
                break;
            case FINISH_WAVE:
                tableText("Each kill will reward you with");
                tableText("money to construct more defenses.");
                tableText("Please wait for the wave to finish.");
                tableText("(You can see how long a wave will last");
                tableText("in the right top corner.)");
                break;
            case UPGRADE_TOWER:
                tableText("Congratulations! You have survived your first wave!");
                tableText("With this extra money, you can upgrade your tower.");
                tableText("Touch the 'Upgrade' button at the bottom of the screen.");
                Image upgrade = new Image(Sprites.sprite(SpriteType.UPGRADE));
                tutorialTable.add(upgrade).size(size).colspan(2).row();
                break;
            case SELECT_TOWER_FOR_UPGRADE:
                tableText("Touch the tower we just constructed");
                tableText("and wait for it to finish upgrading.");
                break;
            case UPGRADE_SUCCESS:
                tableText("Great job! Your tower has been upgraded.");
                tableText("Upgraded towers are much more powerful.");
                break;
            case INVENTORY_INTRO_1:
                tableText("Your inventory is a key part of the game.");
                tableText("It holds a variety of items, which can be used to");
                tableText("add abilities to towers, upgrade stats,");
                tableText("or construct unique towers.");
                Image inventory = new Image(Sprites.sprite(SpriteType.INVENTORY_ICON));
                tutorialTable.add(inventory).size(size).colspan(2).row();
                break;
            case INVENTORY_INTRO_2: {
                tableText("Components are items that can boost", 3);
                tableText("the general stats of towers.", 3);
                tableText("Only one type of component can be applied to a tower.", 3);
                tableText("They are obtained by killing enemies.", 3);
                Image lubricant = new Image(Sprites.sprite(SpriteType.LUBRICANT));
                tutorialTable.add(lubricant).size(size).colspan(1);
                Image scrapMetal = new Image(Sprites.sprite(SpriteType.SCRAP));
                tutorialTable.add(scrapMetal).size(size).colspan(1);
                Image scope = new Image(Sprites.sprite(SpriteType.SCOPE));
                tutorialTable.add(scope).size(size).colspan(1).row();
                break;
            }
            case INVENTORY_OPEN:
                tableText("Let's apply a component to your tower.");
                tableText("Touch the 'Inventory' button at the bottom of the screen.");
                break;
            case INVENTORY_SELECT: {
                tableText("Touch the left top item in the inventory.");
                tableText("This is lubricant, one of the 3 types of components.");
                Image wd40 = new Image(Sprites.sprite(SpriteType.LUBRICANT));
                tutorialTable.add(wd40).size(size).colspan(2).row();
                break;
            }
            case COMPONENT_APPLY:
                tableText("Touch the tower we constructed earlier");
                tableText("to apply the component to it.");
                break;
            case INVENTORY_INTRO_3:
                tableText("Great job! Your tower has been lubricated,", 3);
                tableText("increasing the tower's attack speed.", 3);
                tableText("Scopes and scrap metal, the other two components,", 3);
                tableText("increase the tower's range and damage, respectively.", 3);
                Image lubricant = new Image(Sprites.sprite(SpriteType.LUBRICANT));
                tutorialTable.add(lubricant).size(size).colspan(1);
                Image scope = new Image(Sprites.sprite(SpriteType.SCOPE));
                tutorialTable.add(scope).size(size).colspan(1);
                Image scrapMetal = new Image(Sprites.sprite(SpriteType.SCRAP));
                tutorialTable.add(scrapMetal).size(size).colspan(1).row();
                break;
            case INVENTORY_INTRO_4: {
                tableText("The inventory also holds resource items, like");
                tableText("steel and titanium. Obtained by constructing production");
                tableText("towers, resources are used to construct unique towers.");
                Image steel = new Image(Sprites.sprite(SpriteType.STEEL));
                Image titanium = new Image(Sprites.sprite(SpriteType.TITANIUM));
                tutorialTable.add(steel).size(size).colspan(1);
                tutorialTable.add(titanium).size(size).colspan(1).row();
                Image ironVein = new Image(Sprites.sprite("ironVein"));
                Image titaniumVein = new Image(Sprites.sprite("titaniumVein"));
                tutorialTable.add(ironVein).size(size).colspan(1);
                tutorialTable.add(titaniumVein).size(size).colspan(1).row();
                break;
            }
            case INVENTORY_INTRO_5: {
                tableText("Lastly, the inventory holds gemstones.", 3);
                tableText("They are drilled from gemstone deposits, like this one.", 3);
                tableText("Only one gemstone can be applied to a tower at a time.", 3);
                Image topaz = new Image(Sprites.sprite(SpriteType.TOPAZ));
                Image quartz = new Image(Sprites.sprite(SpriteType.QUARTZ));
                Image ruby = new Image(Sprites.sprite(SpriteType.RUBY));
                tutorialTable.add(topaz).size(size).colspan(1);
                tutorialTable.add(quartz).size(size).colspan(1);
                tutorialTable.add(ruby).size(size).colspan(1).row();
                break;
            }
            case INVENTORY_INTRO_6:
                tableText("Gemstones can provide unique abilities to towers,", 3);
                tableText("such as stunning enemies, dealing piercing damage,", 3);
                tableText("or trading damage for speed boosts.", 3);
                tableText("Experiement to find the unique benefits of each gemstone!", 3);
                Image cryonite = new Image(Sprites.sprite(SpriteType.CRYONITE));
                Image diamond = new Image(Sprites.sprite(SpriteType.DIAMOND));
                Image thorium = new Image(Sprites.sprite(SpriteType.THORIUM));
                tutorialTable.add(cryonite).size(size).colspan(1);
                tutorialTable.add(diamond).size(size).colspan(1);
                tutorialTable.add(thorium).size(size).colspan(1).row();
                break;
            case SELL_INTRO:
                tableText("Finally, to view a tower's stats, you can touch it.");
                break;
            case SELL:
                tableText("Here, you can view what components and gemstones");
                tableText("are applied to the tower along with the tower's stats.");
                tableText("You can also modify the tower's targeting method");
                tableText("or sell the tower for a refund here.");
                tableText("Please sell your tower by touching 'Sell'.");
                tableText("(Components and gemstones will be lost on sell.)");
                break;
            case FINAL_INFO: {
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
                tableText("You are now ready to play " + ProjectTetraTD.GAME_NAME + "!");
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
                    return pressedW || pressedA || pressedS || pressedD;
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
                //focusTileX = steelDepositX;
                //focusTileY = steelDepositY;
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
                gameManager.updateTeamStats(0);
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
    private void tableText(String text, int colspan) {
        tutorialTable.add(text, "small").colspan(colspan).row();
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
            onTutorialFinished();
        }
    }

    public void onTutorialFinished() {
        game.setScreen(new MainMenuScreen());
        AchievementManager.getInstance().setAchievementValue(TutorialCompleteAchievement.class, true);
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
