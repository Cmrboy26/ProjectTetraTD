package net.cmr.rtd.game.world.entities.towers;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.entities.effects.SlownessEffect;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.AnimationType;

public class IceTower extends TowerEntity {

    public IceTower() {
        super(GameType.ICE_TOWER);
    }

    @Override
    public void update(float delta, UpdateData data) {
        super.update(delta, data);
    }

    @Override
    public void attack(UpdateData data) {
        super.attack(data);
        ArrayList<Entity> entitiesInRange = getEntitiesInRange(3, data);
        for (Entity entity : entitiesInRange) {
            if (entity instanceof EnemyEntity) {
                EnemyEntity enemy = (EnemyEntity) entity;
                new SlownessEffect(enemy.getEffects(), 2, 1);
                // TODO: add a visual effect to show the slow effect
            }
        }
    }
    
    @Override
    public float getAttackSpeed() {
        return .5f;
    }

    @Override
    protected void serializeTower(DataBuffer buffer) throws IOException {
        
    }

    @Override
    protected void deserializeTower(TowerEntity entity, DataInputStream input) throws IOException {

    }

    float animationDelta = 0;

    @Override
    public void render(Batch batch, float delta) {
        animationDelta += delta;
        batch.setColor(Color.BLUE);
        TextureRegion sprite = Sprites.animation(AnimationType.TESLA_TOWER, animationDelta); //Sprites.sprite(Sprites.SpriteType.CMRBOY26)
        batch.draw(sprite, getX() - Tile.SIZE * 1f/8f, getY(), Tile.SIZE, Tile.SIZE);
        batch.setColor(Color.WHITE);
        super.render(batch, delta);
    }

}
