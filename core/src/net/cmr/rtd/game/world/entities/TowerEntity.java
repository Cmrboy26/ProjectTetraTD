package net.cmr.rtd.game.world.entities;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.DataBuffer;
import com.badlogic.gdx.utils.Null;

import net.cmr.rtd.game.packets.AttackPacket;
import net.cmr.rtd.game.storage.TeamInventory;
import net.cmr.rtd.game.storage.TeamInventory.Material;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.particles.SpreadEmitterEffect;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.screen.GameScreen;
import net.cmr.util.Audio;
import net.cmr.util.Audio.GameSFX;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.AnimationType;
import net.cmr.util.Sprites.SpriteType;
import net.cmr.util.StringUtils;

public abstract class TowerEntity extends Entity {

    public static boolean displayRange = false;
    public static TowerEntity displayRangeTower;

    float attackDelta = 0;
    int team;

    float levelUpDelta = -1; // -1 means not currently upgrading, 0 and lower means done upgrading, positive means time left 
    float upgradeTime = 1;
    float placementDelta = -1;
    int level = 1;

    // TODO: Add other tower upgrades
    // NOTE: The player must select only ONE type of upgrade for the tower and must stick with that path.
    static final int VERSION = 2;
    public static final int MAX_COMPONENTS = 4;
    protected int scrapsApplied = 0;
    protected int lubricantApplied = 0;
    protected int scopesApplied = 0;
    SortType preferedSortType = SortType.STRUCTURE_DISTANCE;
    @Null Material selectedMaterial = null;

    public TowerEntity(GameType type, int team) {
        super(type);
        this.team = team;
        this.level = 1;
        this.levelUpDelta = -1;
        this.upgradeTime = 1;
    }
    
    public void update(float delta, UpdateData data) {
        if (levelUpDelta != -1) {
            levelUpDelta -= delta;
            if (levelUpDelta <= 0) {
                levelUpDelta = -1;
                upgradeTime = 1;
                level++;
            }
        } else if (placementDelta != -1) {
            placementDelta -= delta;
            if (placementDelta <= 0) {
                placementDelta = -1;
            }
        } else if (data.isServer()) { 
            // CANNOT attack while upgrading
            attackDelta += delta;
            if (attackDelta >= getAttackSpeed()) {
                boolean attacked = attack(data);
                if (attacked) {
                    AttackPacket packet = new AttackPacket(this.getID());
                    data.getManager().sendPacketToAll(packet);
                    attackDelta = 0;
                }
            }
        }
    }

    @Override
    public float getRenderOffset() {
        return - Tile.SIZE / 2;
    }

    @Override
    protected void serializeEntity(DataBuffer buffer) throws IOException {
        buffer.writeInt(VERSION);
        buffer.writeFloat(attackDelta);
        buffer.writeInt(team);
        buffer.writeInt(level);
        buffer.writeFloat(levelUpDelta);
        buffer.writeFloat(upgradeTime);
        buffer.writeFloat(placementDelta);
        buffer.writeInt(scrapsApplied);
        buffer.writeInt(lubricantApplied);
        buffer.writeInt(scopesApplied);
        buffer.writeInt(preferedSortType.getID());
        buffer.writeInt(selectedMaterial == null ? -1 : selectedMaterial.id);
        serializeTower(buffer);
    }

    @Override
    protected void deserializeEntity(GameObject object, DataInputStream input) throws IOException {
        TowerEntity tower = (TowerEntity) object;
        int version = input.readInt();
        tower.attackDelta = input.readFloat();
        tower.team = input.readInt();
        tower.level = input.readInt();
        tower.levelUpDelta = input.readFloat();
        tower.upgradeTime = input.readFloat();
        tower.placementDelta = input.readFloat();
        tower.scrapsApplied = input.readInt();
        tower.lubricantApplied = input.readInt();
        tower.scopesApplied = input.readInt();
        if (version >= 1) {
            tower.preferedSortType = SortType.fromID(input.readInt());
        }
        if (version >= 2) {
            tower.selectedMaterial = Material.getMaterial(input.readInt());
        }
        deserializeTower(tower, input);
    }

    public void preRender(UpdateData data, Batch batch, float delta) {
        if (getRemainingUpgradeTime() != -1f) {
            batch.draw(Sprites.sprite(SpriteType.UPGRADE_BACK), getX() - Tile.SIZE / 2, getY() - Tile.SIZE / 2, Tile.SIZE, Tile.SIZE * 2);
        }
        Color batchColor = batch.getColor();
        if (data.getScreen().team != team && batchColor.a == 1) {
            batchColor.a *= GameScreen.OPPONENT_TEAM_ALPHA;
        }
        batch.setColor(batchColor);
    }

    float lastProgress = -1;
    boolean building = false, lastBuilding = false;
    public void postRender(UpdateData data, Batch batch, float delta) {
        batch.setColor(Color.WHITE); 
        float progress = 1 - getRemainingUpgradeTime() / getUpgradeTime();
        float buildProgress = 1 - getRemainingBuildTime() / 3;

        if (buildProgress < 1 && buildProgress > 0) {
            progress = Math.max(buildProgress, 0.05f);
            building = true;
        } else {
            building = false;
        }
        if (getRemainingUpgradeTime() != -1f || placementDelta != -1f) {
            batch.draw(Sprites.sprite(SpriteType.UPGRADE_FRONT), getX() - Tile.SIZE / 2, getY() - Tile.SIZE / 2, Tile.SIZE, Tile.SIZE * 2);
            if (progress < 1 && progress > 0) {
                progress = Math.max(progress, 0.05f);
                progress = Interpolation.sineOut.apply(progress);
                batch.setColor(1, 1, 1, 1);
                GameScreen.upgradeProgressBackground.draw(batch, getX() - Tile.SIZE / 2.5f, getY() - Tile.SIZE / 2.5f, Tile.SIZE * .8f, Tile.SIZE / 8);
                batch.setColor(1, 1, 1, .7f);
                GameScreen.upgradeProgress.draw(batch, getX() - Tile.SIZE / 2.5f, getY() - Tile.SIZE / 2.5f, Tile.SIZE * progress * .8f, Tile.SIZE / 8);
                batch.setColor(1, 1, 1, 1);
            }
        }
        boolean upgradeComplete = getRemainingUpgradeTime() == -1 && lastProgress != -1;
        boolean buildingComplete = lastBuilding != building && !building;
        if (upgradeComplete || buildingComplete) {
            // Play sound
            Audio.getInstance().playSFX(GameSFX.UPGRADE_COMPLETE, .5f);
            // Display completed particle
            SpreadEmitterEffect effect = SpreadEmitterEffect.factory()
                .setParticle(AnimationType.SPARKLE)
                .setDuration(1.5f)
                .setEmissionRate(15)
                .setScale(.2f)
                .setParticleLife(.5f)
                .setAnimationSpeed(1.5f)
                .setAreaSize(1.2f)
                .create();
            effect.setPosition(new Vector2(getX(), getY()));
            data.getScreen().addEffect(effect);
        }
        lastProgress = getRemainingUpgradeTime();
        lastBuilding = building;

        // draw a circle of radius getDisplayRange() centered at getPosition()
        if (displayRange && (displayRangeTower == null || displayRangeTower == this)) {
            SpriteType type = SpriteType.AREA;
            // set the color to a transparent yellow
            batch.setColor(new Color(Color.YELLOW).sub(0,0,0,.8f));
            batch.draw(Sprites.sprite(type), getPosition().x - getRange() * Tile.SIZE, getPosition().y - getRange() * Tile.SIZE, getRange() * 2 * Tile.SIZE, getRange() * 2 * Tile.SIZE);
            batch.setColor(Color.WHITE);
        }
    }

    protected abstract void serializeTower(DataBuffer buffer) throws IOException;
    protected abstract void deserializeTower(TowerEntity entity, DataInputStream input) throws IOException;

    private static HashMap<Integer, SortType> sortTypeMap;

    public enum SortType {
        /**
         * Returns a list in any order (fastest).
         */
        ANY(0) {
            @Override
            public void sort(ArrayList<EnemyEntity> entities, TowerEntity tower, UpdateData data) {
                // Do nothing
            }
        },
        /**
         * Highest health to lowest health
         */
        HIGHEST_HEALTH(1) {
            @Override
            public void sort(ArrayList<EnemyEntity> entities, TowerEntity tower, UpdateData data) {
                entities.sort((a, b) -> (int) (b.getHealth() - a.getHealth()));
            }
        },
        /**
         * Lowest health to highest health
         */
        LOWEST_HEALTH(2) {
            @Override
            public void sort(ArrayList<EnemyEntity> entities, TowerEntity tower, UpdateData data) {
                entities.sort((a, b) -> (int) (a.getHealth() - b.getHealth()));
            }
        },
        /**
         * Closest to farthest to the shooting tower
         */
        TOWER_DISTANCE(3) {
            @Override
            public void sort(ArrayList<EnemyEntity> entities, TowerEntity tower, UpdateData data) {
                entities.sort((a, b) -> {
                    int adist = (int) a.getPosition().dst(tower.getPosition());
                    int bdist = (int) b.getPosition().dst(tower.getPosition());
                    /*if (adist > bdist) {
                        return 1;
                    }
                    if (adist == bdist) {
                        return 0;
                    }
                    return -1;*/
                    return adist - bdist;
                    //(int) (a.getPosition().dst(tower.getPosition()) - b.getPosition().dst(tower.getPosition()))
                });
            }
        },
        /**
         * Farthest to closest to the shooting tower
         */
        TOWER_DISTANCE_REVERSE(4) {
            @Override
            public void sort(ArrayList<EnemyEntity> entities, TowerEntity tower, UpdateData data) {
                entities.sort((a, b) -> {
                    int adist = (int) a.getPosition().dst(tower.getPosition());
                    int bdist = (int) b.getPosition().dst(tower.getPosition());
                    /*if (adist > bdist) {
                        return 1;
                    }
                    if (adist == bdist) {
                        return 0;
                    }
                    return -1;*/
                    return bdist - adist;
                    //(int) (a.getPosition().dst(tower.getPosition()) - b.getPosition().dst(tower.getPosition()))
                });
                //entities.sort((a, b) -> (int) (b.getPosition().dst(tower.getPosition()) - a.getPosition().dst(tower.getPosition())));
            }
        },
        /**
         * Closest to the end of the path/structure
         */
        STRUCTURE_DISTANCE(5) {
            @Override
            public void sort(ArrayList<EnemyEntity> entities, TowerEntity tower, UpdateData data) {
                // AKA: Highest distance traveled first, lowest distance traveled last
                entities.sort((a, b) -> {
                    int adist = (int) a.getDistanceTraveled();
                    int bdist = (int) b.getDistanceTraveled();
                    return bdist - adist;
                });
                //entities.sort((a, b) -> (int) (b.getDistanceTraveled() - a.getDistanceTraveled()));
            }
        },
        /**
         * Farthest to the end of the path/structure
         */
        STRUCTURE_DISTANCE_REVERSE(6) {
            @Override
            public void sort(ArrayList<EnemyEntity> entities, TowerEntity tower, UpdateData data) {
                // AKA: Lowest distance traveled first, highest distance traveled last
                entities.sort((a, b) -> {
                    int adist = (int) a.getDistanceTraveled();
                    int bdist = (int) b.getDistanceTraveled();
                    return adist - bdist;
                });
                //entities.sort((a, b) -> (int) (a.getDistanceTraveled() - b.getDistanceTraveled()));
            }
        };

        static {
            sortTypeMap = new HashMap<>();
            for (SortType type : values()) {
                sortTypeMap.put(type.getID(), type);
            }
            sortTypeMap.put(-1, null);
        }

        int id;
        SortType(int id) {
            this.id = id;
        }
        public int getID() {
            return id;
        }
        public static SortType fromID(int id) {
            return sortTypeMap.get(id);
        }

        public static String getName(SortType type) {
            switch (type) {
                case ANY: return "Random";
                case HIGHEST_HEALTH: return "Strongest";
                case LOWEST_HEALTH: return "Weakest";
                case TOWER_DISTANCE: return "Closest";
                case TOWER_DISTANCE_REVERSE: return "Farthest";
                case STRUCTURE_DISTANCE: return "First";
                case STRUCTURE_DISTANCE_REVERSE: return "Last";
                default: return "None";
            }
        }

        @Override
        public String toString() {
            return getName(this);
        }


        public abstract void sort(ArrayList<EnemyEntity> entities, TowerEntity tower, UpdateData data);
    }

    public ArrayList<EnemyEntity> getEnemiesInRange(double tileRadius, UpdateData data) {
        return getEnemiesInRange(tileRadius, data, SortType.ANY);
    }

    public ArrayList<EnemyEntity> getEnemiesInRange(double tileRadius, UpdateData data, SortType sortType) {
        Objects.requireNonNull(sortType, "sortType cannot be null");
        ArrayList<EnemyEntity> entitiesInRange = getEnemiesInRange(team, getPosition(), tileRadius, data);
        /*double worldDistance = tileRadius = Tile.SIZE * tileRadius;
        ArrayList<EnemyEntity> entitiesInRange = new ArrayList<EnemyEntity>();
        double threshold = Tile.SIZE / 16;
        for (Entity entity : data.getWorld().getEntities()) {
            if (entity instanceof EnemyEntity) {
                EnemyEntity enemy = (EnemyEntity) entity;
                if (enemy.getTeam() != team) continue;
                //double distance = Math.abs(Math.max(entity.getX() - enemy.getX(), entity.getY() - enemy.getY()));
                double distance = entity.getPosition().dst(getPosition());
                if (distance <= worldDistance + threshold) {
                    entitiesInRange.add(enemy);
                }
            }
        }*/
        sortType.sort(entitiesInRange, this, data);
        return entitiesInRange;
    }

    public static ArrayList<EnemyEntity> getEnemiesInRange(int team, Vector2 position, double tileRadius, UpdateData data) {
        double worldDistance = tileRadius = Tile.SIZE * tileRadius;
        ArrayList<EnemyEntity> entitiesInRange = new ArrayList<EnemyEntity>();
        double threshold = Tile.SIZE / 16;
        for (Entity entity : data.getWorld().getEntities()) {
            if (entity instanceof EnemyEntity) {
                EnemyEntity enemy = (EnemyEntity) entity;
                if (enemy.getTeam() != team) continue;
                //double distance = Math.abs(Math.max(entity.getX() - enemy.getX(), entity.getY() - enemy.getY()));
                double distance = entity.getPosition().dst(position);
                if (distance <= worldDistance + threshold) {
                    entitiesInRange.add(enemy);
                }
            }
        }
        return entitiesInRange;
    }

    /**
     * @return seconds between each {@link #attack(UpdateData)} call
     */
    public float getAttackSpeed() {
        return 1;
    }

    public abstract float getDamage(boolean rollCritical);
    public abstract float getRange();
    public abstract String getDescription();
    public float getCritChance() {
        return Material.getCritChance(getSelectedMaterial());
    }
    public float getCritDamagePercent() {
        return Material.getCritDamagePercent(getSelectedMaterial());
    }

    public SortType getPreferedSortType() {
        return preferedSortType;
    }
    public void setPreferedSortType(SortType type) {
        preferedSortType = type;
    }

    public float getUpgradeTime() {
        return upgradeTime;
    }

    public float getRemainingUpgradeTime() {
        return levelUpDelta;
    }

    public int getLevel() {
        return level;
    }

    /**
     * @param upgradeTime time in seconds to upgrade the tower
     * @return if the action was successful
     */
    public boolean levelUp(float upgradeTime) {
        if (levelUpDelta != -1) {
            return false;
        }
        this.upgradeTime = upgradeTime;
        levelUpDelta = upgradeTime;
        return true;
    }

    /**
     * Sets the time to initially build the tower (when placed down on the map).
     * @param delta
     */
    public void setBuildDelta(float delta) {
        placementDelta = delta;
    }

    public float getRemainingBuildTime() {
        return placementDelta;
    }

    /**
     * @return whether the tower has finished the initial build (when placed down on the map)
     */
    public boolean isBeingBuilt() {
        return placementDelta > 0;
    }

    /**
     * @see #getAttackSpeed()
     * @see #getEnemiesInRange(double, UpdateData)
     * @param data world data
     * @apiNote This method is called every {@link #getAttackSpeed()} seconds (if the tower is not upgrading and {@link #attack(UpdateData)} returns true all the time)
     * @return if any action occured, which will reset the attack delta if true
     */
    public boolean attack(UpdateData data) { return false; }

    /**
     * @apiNote This method should only be called on the client side when the client receives an {@link AttackPacket}
     * @param data world data
     */
    public void onAttackClient(UpdateData data) { }

    public int getTeam() { return team; }

    public static void displayRangeAll() {
        displayRange = true;
        displayRangeTower = null;
    }
    public static void displayRange(TowerEntity target) {
        displayRange = true;
        displayRangeTower = target;
    }
    public static void hideRange() {
        displayRange = false;
        displayRangeTower = null;
    }

    // Upgrade Helper Methods

    /**
     * Every x levels, increase by y
     */
    public float calculateIncrementedValue(int levelsPerIncrement, float incrementAmount) {
        return calculateIncrementedValue(levelsPerIncrement, incrementAmount, 0);
    }

    /**
     * Every x levels, increase by y
     */
    public float calculateIncrementedValue(int levelsPerIncrement, float incrementAmount, float initial) {
        return (float) (initial + Math.floor(getLevel() / levelsPerIncrement) * incrementAmount);
    }

    public AnimationType getTowerAnimationLevelDependent(AnimationType[] levels, int[] levelThresholds) {
        for (int i = 0; i < levelThresholds.length; i++) {
            if (getLevel() <= levelThresholds[i]) {
                return levels[i];
            }
        }
        return levels[levels.length - 1];
    }

    public boolean applyLubricant(TeamInventory inventory) {
        if (!canApplyComponentLubricant()) {
            return false;
        }
        if (lubricantApplied >= MAX_COMPONENTS) {
            return false;
        }
        if (scopesApplied != 0 || scrapsApplied != 0) {
            return false;
        }
        if (inventory.getWd40() <= 0) {
            return false;
        }
        lubricantApplied++;
        inventory.removeWd40();
        return true;
    }

    public boolean applyScope(TeamInventory inventory) {
        if (!canApplyComponentScope()) {
            return false;
        }
        if (scopesApplied >= MAX_COMPONENTS) {
            return false;
        }
        if (lubricantApplied != 0 || scrapsApplied != 0) {
            return false;
        }
        if (inventory.getScopes() <= 0) {
            return false;
        }
        scopesApplied++;
        inventory.removeScope();
        return true;
    }

    public boolean applyScrapMetal(TeamInventory inventory) {
        if (!canApplyComponentScrapMetal()) {
            return false;
        }
        if (scrapsApplied >= MAX_COMPONENTS) {
            return false;
        }
        if (lubricantApplied != 0 || scopesApplied != 0) {
            return false;
        }
        if (inventory.getScraps() <= 0) {
            return false;
        }
        scrapsApplied++;
        inventory.removeScrapMetal();
        return true;
    }

    public int getScrapMetalApplied() {
        return scrapsApplied;
    }
    public int getLubricantApplied() {
        return lubricantApplied;
    }
    public int getScopesApplied() {
        return scopesApplied;
    }
    public int getComponentsApplied() {
        return scrapsApplied + lubricantApplied + scopesApplied;
    }

    public int getScrapMetalDamageBoostPercent() {
        return (int) (((getScrapMetalDamageBoost() - 1) * 100));
    }
    public int getLubricantSpeedBoostPercent() {
        return (int) ((getLubricantSpeedBoost() - 1) * 100);
    }
    public int getScopeRangeBoostPercent() {
        return (int) ((getScopeRangeBoost() - 1) * 100);
    }

    public float getScrapMetalDamageBoost() {
        return Math.max(1, 1 + scrapsApplied * .05f);
    }
    public float getLubricantSpeedBoost() {
        return Math.max(1, 1 + lubricantApplied * .1f);
    }
    public float getScopeRangeBoost() {
        return Math.max(1, 1 + scopesApplied * .20f);
    }

    public boolean canApplyComponentScrapMetal() { return true; }
    public boolean canApplyComponentLubricant() { return true; }
    public boolean canApplyComponentScope() { return true; }

    public boolean canEditSortType() {
        return true;
    }

    public boolean applyMaterial(Material material) {
        if (canApplyMaterial(material)) {
            selectedMaterial = material;
            return true;
        }
        return false;
    }

    public boolean canApplyMaterial(Material material) {
        if (getValidMaterials() == null) {
            return false;
        }
        for (Material mat : getValidMaterials()) {
            if (mat == material) {
                return true;
            }
        }
        return false;
    }

    public @Null Material getSelectedMaterial() {
        return selectedMaterial;
    }

    public @Null Material[] getValidMaterials() {
        return null;
        //return new Material[] {Material.CRYONITE, Material.DIAMONDS, Material.QUARTZ, Material.RUBY, Material.THORIUM, Material.TOPAZ};
    }

    private Label descriptionLabel(String text) {
        return descriptionLabel(text, true);
    }

    private Label descriptionLabel(String text, boolean wrap) {
        Label label = new Label(text, Sprites.skin(), "small");
        label.setWrap(wrap);
        label.setFontScale(.25f);
        return label;
    }

    public Table getTowerDescription() {
        Table table = new Table(Sprites.skin());
        table.setSize(200, 200);
        //Label label = new Label("", Sprites.skin(), "small");
        //label.setWrap(true);
        //label.setFontScale(.2f);
        //label.setSize(200, 200);
        //table.add(label).grow();

        table.add(new Image(Sprites.sprite(SpriteType.LEVEL_ICON))).colspan(1).left();
        table.add(descriptionLabel("Level: "+getLevel())).colspan(1).padLeft(3).left().growX().row();
        
        Table generalStatsTable = new Table();
        Table rangeTable = new Table();
        rangeTable.add(new Image(Sprites.sprite(SpriteType.RANGE_ICON))).colspan(1).left();
        String range = StringUtils.truncateFloatingPoint(getRange(), 2);
        rangeTable.add(descriptionLabel("Range: "+range)).colspan(1).padLeft(3).left().growX().row();

        Table dpsTable = new Table();
        dpsTable.add(new Image(Sprites.sprite(SpriteType.DPS_ICON))).colspan(1).left();
        dpsTable.add(descriptionLabel("DPS: "+StringUtils.truncateFloatingPoint(getDamage(false)/getAttackSpeed(), 2))).colspan(1).padLeft(3).left().growX().row();

        generalStatsTable.add(dpsTable).growX();
        generalStatsTable.add(rangeTable).growX();
        table.add(generalStatsTable).colspan(2).growX().row();

        Table dpsComponentsTable = new Table();
        Table damageTable = new Table();
        damageTable.add(new Image(Sprites.sprite(SpriteType.DAMAGE_ICON))).colspan(1).left();
        String damage = StringUtils.truncateFloatingPoint(getDamage(false), 2);
        damageTable.add(descriptionLabel("Damage: "+damage)).colspan(1).padLeft(3).left().growX().row();

        Table speedTable = new Table();
        speedTable.add(new Image(Sprites.sprite(SpriteType.ATTACK_SPEED_ICON))).colspan(1).left();
        String speed = StringUtils.truncateFloatingPoint(1f/getAttackSpeed(), 2);
        speedTable.add(descriptionLabel("Speed: "+speed)).colspan(1).padLeft(3).left().growX().row();

        dpsComponentsTable.add(damageTable).growX();
        dpsComponentsTable.add(speedTable).growX();
        table.add(dpsComponentsTable).colspan(2).growX().row();

        if (getCritDamagePercent() != 1) {
            Table critTable = new Table();
            Table critDamageTable = new Table();
            critDamageTable.add(new Image(Sprites.sprite(SpriteType.CRITICAL_ICON))).colspan(1).left();
            String critDamage = StringUtils.truncateFloatingPoint(getDamage(false) * getCritDamagePercent(), 2);
            String multiplierText = "Crit Multiplier: x"+StringUtils.truncateFloatingPoint(getCritDamagePercent(), 2);
            critDamageTable.add(descriptionLabel(multiplierText)).colspan(1).padLeft(3).left().growX().row();
            Table critChanceTable = new Table();
            critChanceTable.add(new Image(Sprites.sprite(SpriteType.CRITICAL_CHANCE_ICON))).colspan(1).left();
            critChanceTable.add(descriptionLabel("Crit Chance: "+StringUtils.truncateFloatingPoint(getCritChance()*100, 2)+"%")).colspan(1).padLeft(3).left().growX().row();

            critTable.add(critDamageTable).growX();
            critTable.add(critChanceTable).growX();
            table.add(critTable).colspan(2).growX().row();
        }

        table.add(new Image(Sprites.sprite(SpriteType.DESCRIPTION_ICON))).colspan(1).left();
        table.add(descriptionLabel(getDescription())).colspan(1).padLeft(3).left().growX().row();
        
        SpriteType component = null;
        SpriteType benefitIcon = null;
        String componentInfo = "";
        componentInfo = getComponentsApplied()+"/"+TowerEntity.MAX_COMPONENTS+" ";
        if (getLubricantApplied() > 0) {
            component = SpriteType.LUBRICANT;
            componentInfo += "Lubricant (+"+getLubricantSpeedBoostPercent()+"%";
            benefitIcon = SpriteType.ATTACK_SPEED_ICON;
        } else if (getScopesApplied() > 0) {
            component = SpriteType.SCOPE;
            componentInfo += "Scope (+"+getScopeRangeBoostPercent()+"%";
            benefitIcon = SpriteType.RANGE_ICON;
        } else if (getScrapMetalApplied() > 0) {
            component = SpriteType.SCRAP;
            componentInfo += "Scrap Metal (+"+getScrapMetalDamageBoostPercent()+"%";
            benefitIcon = SpriteType.DAMAGE_ICON;
        }

        if (component != null) {
            table.add(new Image(Sprites.sprite(component))).size(11, 11).colspan(1).left();
            Table text = new Table();
            text.add(descriptionLabel(componentInfo, false)).colspan(1).padLeft(3).fillX().left();
            text.add(new Image(Sprites.sprite(benefitIcon))).size(9, 9).colspan(1).padLeft(3).left();
            text.add(descriptionLabel(")", false)).colspan(1).padLeft(1).left().growX().row();
            table.add(text).colspan(1).left().growX().row();
        }

        Material selectedMaterial = getSelectedMaterial();\\
        if (selectedMaterial != null) {
            SpriteType gemSprite = selectedMaterial.image;
            String desc = selectedMaterial.description;
            String name = selectedMaterial.materialName;
            table.add(new Image(Sprites.sprite(gemSprite))).size(11, 11).colspan(1).left();
            String gemContent = name+": "+desc;
            table.add(descriptionLabel(gemContent)).colspan(1).padLeft(3).left().growX().row();
        }

        /*StringBuilder builder = new StringBuilder();

        appendLine(builder, "Level " + getLevel());
        appendLine(builder, "Range: " + StringUtils.truncateFloatingPoint(getRange(), 2) + " tiles");
        appendLine(builder, "DPS: ~" + StringUtils.truncateFloatingPoint(getDamage(false)/getAttackSpeed(), 2));
        appendLine(builder, "- Damage: " + StringUtils.truncateFloatingPoint(getDamage(false), 2));
        appendLine(builder, "- Speed: " + StringUtils.truncateFloatingPoint(1f/getAttackSpeed(), 2));
        appendLine(builder, "Description: \n- " + getDescription());
        appendLine(builder, "");

        String componentPath = "None";
        
        String benefits = "None";
        if (getLubricantApplied() > 0) {
            componentPath = "Lubricant";
            benefits = "+"+getLubricantSpeedBoostPercent()+"% Attack Speed";
        } else if (getScopesApplied() > 0) {
            componentPath = "Scopes";
            benefits = "+"+getScopeRangeBoostPercent()+"% Range";
        } else if (getScrapMetalApplied() > 0) {
            componentPath = "Scrap Metal";
            benefits = "+"+getScrapMetalDamageBoostPercent()+"% Damage";
        }

        String amount = getComponentsApplied() + "/" + MAX_COMPONENTS;
        amount = " ("+amount+")";
        if (componentPath.equals("None")) {
            amount = "";
        }
        appendLine(builder, "Component: "+componentPath+amount);
        if (getComponentsApplied() > 0) {
            appendLine(builder, "- Bonuses: "+benefits);
        }

        appendLine(builder, "");
        if (getSelectedMaterial() == null) {
            //appendLine(builder, "Gem: None");
        } else {
            appendLine(builder, "Gem: "+getSelectedMaterial().materialName);
            appendLine(builder, "- "+getSelectedMaterial().description);
        }*/

        
        //label.setText(builder.toString());
        return table;
    }

    protected void appendLine(StringBuilder builder, String info) {
        builder.append(info).append("\n");
    }

}
