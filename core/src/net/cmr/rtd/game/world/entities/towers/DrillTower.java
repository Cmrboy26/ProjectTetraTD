package net.cmr.rtd.game.world.entities.towers;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.storage.TeamInventory;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.MiningTower;
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
        return (float) Math.max(15 - Math.sqrt((getLevel() - 1) * 10), 5) * (under == TileType.TITANIUM_VEIN ? 1.25f : 1);
    }

    @Override
    public void onMiningComplete(UpdateData data) {
        TeamInventory inventory = getTeamInventory(data);
        TileType type = getTileBelow(data);
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

    public String getTowerDescription() {
        StringBuilder builder = new StringBuilder();

        appendLine(builder, "Level " + getLevel());
        appendLine(builder, "Mining Time: " + StringUtils.truncateFloatingPoint(getMiningTime(), 2) + "s");
        appendLine(builder, "Collects: " + (under == TileType.TITANIUM_VEIN ? "Titanium" : "Steel"));
        appendLine(builder, "Description: \n- " + getDescription());

        return builder.toString();
    }
    
}
