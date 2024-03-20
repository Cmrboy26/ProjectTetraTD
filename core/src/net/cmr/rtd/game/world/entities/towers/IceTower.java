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

    boolean attacking = false;
    float animationDelta = 0;
    final float persistence = 1;
    float range = 2;

    public IceTower() {
        super(GameType.ICE_TOWER, 0);
    }

    public IceTower(int team) {
        super(GameType.ICE_TOWER, team);
    }

    @Override
    public void update(float delta, UpdateData data) {
        super.update(delta, data);
    }

    @Override
    public void attack(UpdateData data) {
        super.attack(data);
        ArrayList<EnemyEntity> entitiesInRange = getEnemiesInRange(range, data);
        attacking = false;
        for (Entity entity : entitiesInRange) {
            attacking = true;
            if (entity instanceof EnemyEntity) {
                if (data.isClient()) {

                }
                EnemyEntity enemy = (EnemyEntity) entity;
                new SlownessEffect(enemy.getEffects(), getAttackSpeed() + persistence, 1);
                // TODO: add a visual effect to show the slow effect
            }
        }
    }
    
    @Override
    public float getAttackSpeed() {
        return .5f;
    }

    @Override
    public float getDisplayRange() {
        return range;
    }

    @Override
    protected void serializeTower(DataBuffer buffer) throws IOException {
        
    }

    @Override
    protected void deserializeTower(TowerEntity entity, DataInputStream input) throws IOException {

    }

    @Override
    public void render(Batch batch, float delta) {
        if (attacking) {
            animationDelta += delta;
        } else {
            animationDelta = 0;
        }

        Color color = new Color(Color.BLUE);
        color.a = batch.getColor().a;
        batch.setColor(color);
        TextureRegion sprite = Sprites.animation(AnimationType.TESLA_TOWER, animationDelta); //Sprites.sprite(Sprites.SpriteType.CMRBOY26)
        batch.draw(sprite, getX() - Tile.SIZE / 2, getY() - Tile.SIZE / 2, Tile.SIZE, Tile.SIZE);
        batch.setColor(Color.WHITE);
        super.render(batch, delta);
    }

}
