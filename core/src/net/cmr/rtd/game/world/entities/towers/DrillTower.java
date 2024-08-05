package net.cmr.rtd.game.world.entities.towers;

import java.io.DataInputStream;
import java.io.IOException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.storage.TeamInventory;
import net.cmr.rtd.game.storage.TeamInventory.Material;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.MiningTower;
import net.cmr.rtd.game.world.entities.TowerDescription;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.game.world.tile.Tile.TileType;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.AnimationType;
import net.cmr.util.Sprites.SpriteType;
import net.cmr.util.StringUtils;

public class DrillTower extends MiningTower {

    float timeUntilNextItem = 0;
    TileType under = null;

    public DrillTower() {
        super(GameType.DRILL_TOWER, 0);
    }

    public DrillTower(int team) {
        super(GameType.DRILL_TOWER, team);
    }

    float animationDelta;

    @Override
    public void render(UpdateData data, Batch batch, float delta) {
        preRender(data, batch, delta);

        if (getRemainingUpgradeTime() <= 0) {
            animationDelta += delta;
        }

        Color color = new Color(Color.WHITE);
        color.a = batch.getColor().a;
        batch.setColor(color);
        TextureRegion sprite = Sprites.sprite(SpriteType.DRILL_TOWER_ONE);
        batch.draw(sprite, getX() - Tile.SIZE / 2, getY() - Tile.SIZE / 2, Tile.SIZE, Tile.SIZE);
        TextureRegion drill = Sprites.animation(AnimationType.DRILL, animationDelta);
        batch.draw(drill, getX() - Tile.SIZE / 2, getY() - Tile.SIZE / 2, Tile.SIZE, Tile.SIZE);
        if (color.a != 1 && getTeam() == data.getScreen().team) {
            SpriteType type = null;
            String text = null;
            TileType actualUnder = getTileBelow(data);
            if (actualUnder == TileType.IRON_VEIN) {
                type = SpriteType.STEEL;
                text = "Steel";
            } else if (actualUnder == TileType.TITANIUM_VEIN) {
                type = SpriteType.TITANIUM;
                text = "Titanium";
            }
            if (type != null) {
                GlyphLayout layout = new GlyphLayout(Sprites.getInstance().smallFont(), text);
                batch.setColor(1, 1, 1, 1);
                Sprites.getInstance().smallFont().draw(batch, layout, getX() - (layout.width / 2), getY() + Tile.SIZE);
                batch.draw(Sprites.sprite(type), getX() - Tile.SIZE / 2, getY() - Tile.SIZE / 2, Tile.SIZE, Tile.SIZE);
            }
        }
        batch.setColor(Color.WHITE);

        
        postRender(data, batch, delta);
        super.render(data, batch, delta);
    }

    @Override
    public void update(float delta, UpdateData data) {
        under = getTileBelow(data);
        super.update(delta, data);
    }

    @Override
    protected void serializeTower(DataBuffer buffer) throws IOException {
        buffer.writeFloat(timeUntilNextItem);
    }

    @Override
    protected void deserializeTower(TowerEntity entity, DataInputStream input) throws IOException {
        timeUntilNextItem = input.readFloat();
    }

    @Override
    public String getDescription() {
        return "A general-purpose mining drill that can be used to extract resources from the ground, which can be used to build and upgrade special towers.";
    }

    public float getLubricantSpeedBoost() {
        return Math.max(1, 1 + this.lubricantApplied * .1f);
    }

    @Override
    public float getMiningTime() {
        return (float) Math.max(30 - Math.sqrt((getLevel() - 1) * 14), 5) * (under == TileType.TITANIUM_VEIN ? 1.25f : 1);
    }

    @Override
    public void onMiningComplete(UpdateData data) {
        TeamInventory inventory = getTeamInventory(data);
        TileType type = getTileBelow(data);
        Material material = under == TileType.TITANIUM_VEIN ? Material.TITANIUM : Material.STEEL;
        if (type == TileType.IRON_VEIN) {
            inventory.steel++;
        } else if (type == TileType.TITANIUM_VEIN) {
            inventory.titanium++;
        }
        updateInventoryOnClients(data);
        
    }

    @Override
    public boolean validMiningTarget(TileType type) {
        return type == TileType.TITANIUM_VEIN || type == TileType.IRON_VEIN;
    }

    public Table getTowerDescription() {
        TowerDescription description = TowerDescription.getMinimalDescription();

        Table miningTable = new Table();
        Table miningTimeTable = new Table();
        miningTimeTable.add(new Image(Sprites.sprite(SpriteType.MINING_SPEED_ICON))).colspan(1).left();
        miningTimeTable.add(description.descriptionLabel("Mining Time: " + StringUtils.truncateFloatingPoint(getMiningTime(), 2) + "s")).colspan(1).padLeft(3).left().growX().row();
        Table collectsTable = new Table();
        SpriteType collectType = under == TileType.TITANIUM_VEIN ? SpriteType.TITANIUM : SpriteType.STEEL;
        String collects = under == TileType.TITANIUM_VEIN ? "Titanium" : "Steel";
        collectsTable.add(new Image(Sprites.sprite(collectType))).colspan(1).left();
        collectsTable.add(description.descriptionLabel("Collects: " + collects)).colspan(1).padLeft(3).left().growX().row();

        miningTable.add(miningTimeTable).growX();
        miningTable.add(collectsTable).growX();
        description.addCustomSection(miningTable);

        return description.create(this);
    }
    
}
