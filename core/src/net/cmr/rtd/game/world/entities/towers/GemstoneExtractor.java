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

        if (getRemainingUpgradeTime() > 0) {
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
        return "A general-purpose mining drill that can be used to extract resources from the ground, which can be used to build and upgrade special towers.";
    }

    @Override
    public float getMiningTime() {
        return (float) Math.max(40 - Math.sqrt((getLevel() - 1) * 15), 5);
    }

    @Override
    public void onMiningComplete(UpdateData data) {
        TeamInventory inventory = getTeamInventory(data);
        TileType type = getTileBelow(data);
        if (type == TileType.GEMSTONE_VEIN) {
            Random random = new Random();
            int selection = random.nextInt(6);
            int amount = 1;
            switch (selection) {
                case 0:
                    inventory.diamonds+=amount;
                    break;
                case 1:
                    inventory.cryonite+=amount;
                    break;
                case 2:
                    inventory.thorium+=amount;
                    break;
                case 3:
                    inventory.ruby+=amount;
                    break;
                case 4:
                    inventory.quartz+=amount;
                    break;
                case 5:
                    inventory.topaz+=amount;
                    break;
                default:
                    break;
            }
        }
        updateInventoryOnClients(data);
    }

    @Override
    public boolean validMiningTarget(TileType type) {
        return type == TileType.GEMSTONE_VEIN;
    }
    
}
