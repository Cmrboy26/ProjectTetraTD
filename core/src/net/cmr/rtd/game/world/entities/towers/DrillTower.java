package net.cmr.rtd.game.world.entities.towers;

import java.io.DataInputStream;
import java.io.IOException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.DataBuffer;
import com.badlogic.gdx.utils.Null;

import net.cmr.rtd.game.storage.TeamInventory;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.MiningTower;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.game.world.tile.Tile.TileType;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.AnimationType;
import net.cmr.util.Sprites.SpriteType;

public class DrillTower extends MiningTower {

    float timeUntilNextItem = 0;

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

        animationDelta += delta;

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

    @Override
    public float getMiningTime() {
        return Math.max(60 - getLevel() * 5, 5);
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
    
}
