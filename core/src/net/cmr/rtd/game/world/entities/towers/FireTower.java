package net.cmr.rtd.game.world.entities.towers;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.entities.effects.FireEffect;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.AnimationType;

public class FireTower extends TowerEntity {

    boolean attacking = false;
    float animationDelta = 0;
    float targetDPS = 1f;
    float range = 3;

    public FireTower(int team) {
        super(GameType.FIRE_TOWER, team);
    }

    @Override
    public void attack(UpdateData data) {
        super.attack(data);
        ArrayList<Entity> entitiesInRange = getEntitiesInRange(range, data);
        int damageIncrement = (int) Math.ceil(targetDPS * getAttackSpeed());
        for (Entity entity : entitiesInRange) {
            if (entity instanceof EnemyEntity) {
                EnemyEntity enemy = (EnemyEntity) entity;
                enemy.damage(damageIncrement);

                // 1 in 4 chance to apply fire effect
                int random = new Random().nextInt(4);
                if (random == 0) {
                    new FireEffect(enemy.getEffects(), 4, 1);
                }
            }
        }
    }

    @Override
    protected void serializeTower(DataBuffer buffer) throws IOException {

    }

    @Override
    protected void deserializeTower(TowerEntity entity, DataInputStream input) throws IOException {

    }

    @Override
    public float getAttackSpeed() {
        return 1;
    }

    @Override
    public void render(Batch batch, float delta) {
        if (attacking) {
            animationDelta += delta;
        } else {
            animationDelta = 0;
        }

        batch.setColor(Color.RED);
        TextureRegion sprite = Sprites.animation(AnimationType.TESLA_TOWER, animationDelta); //Sprites.sprite(Sprites.SpriteType.CMRBOY26)
        batch.draw(sprite, getX() - Tile.SIZE / 2, getY() - Tile.SIZE / 2, Tile.SIZE, Tile.SIZE);
        batch.setColor(Color.WHITE);
        super.render(batch, delta);
    }
    
    
}
