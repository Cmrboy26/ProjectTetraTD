package net.cmr.rtd.game.world.entities.towers;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.storage.TeamInventory;
import net.cmr.rtd.game.storage.TeamInventory.Material;
import net.cmr.rtd.game.storage.TeamInventory.MaterialType;
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

public class GemstoneExtractor extends MiningTower {

    float timeUntilNextItem = 0;

    public GemstoneExtractor() {
        super(GameType.GEMSTONE_EXTRACTOR, 0);
    }

    public GemstoneExtractor(int team) {
        super(GameType.GEMSTONE_EXTRACTOR, team);
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
        TextureRegion sprite = Sprites.sprite(SpriteType.GEMSTONE_EXTRACTOR_ONE);
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
        return "A specialized tower that extracts gemstones from the ground, which can be used to specialize a tower's stats.";
    }

    @Override
    public float getMiningTime() {
        return (float) Math.max(60 - Math.sqrt((getLevel() - 1) * 160), 5);
        //return 1f;
    }

    @Override
    public void onMiningComplete(UpdateData data) {
        TeamInventory inventory = getTeamInventory(data);
        TileType type = getTileBelow(data);
        Material[] gemList = Material.getMaterialType(MaterialType.GEMSTONE);
        if (type == TileType.GEMSTONE_VEIN) {
            Random random = new Random();
            int selection = random.nextInt(gemList.length);
            int amount = 1;
            Material gem = gemList[selection];
            inventory.addMaterial(gem, amount);
            displayCollectedEffect(data, gem.image);
        }
        updateInventoryOnClients(data);
    }

    @Override
    public boolean validMiningTarget(TileType type) {
        return type == TileType.GEMSTONE_VEIN;
    }

    public Table getTowerDescription() {
        TowerDescription description = TowerDescription.getMinimalDescription();

        Table miningTable = new Table();
        Table miningTimeTable = new Table();
        miningTimeTable.add(new Image(Sprites.sprite(SpriteType.MINING_SPEED_ICON))).colspan(1).left();
        miningTimeTable.add(description.descriptionLabel("Mining Time: " + StringUtils.truncateFloatingPoint(getMiningTime(), 2) + "s")).colspan(1).padLeft(3).left().growX().row();
        miningTable.add(miningTimeTable).growX();
        description.addCustomSection(miningTable);

        return description.create(this);
    }
    
}
