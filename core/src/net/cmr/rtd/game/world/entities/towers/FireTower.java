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
import net.cmr.rtd.game.world.entities.Projectile;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.entities.effects.FireEffect;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.AnimationType;

public class FireTower extends TowerEntity {

    boolean attacking = false;
    float animationDelta = 0;
    float targetDPS = 1f;
    float range = 2;

    public FireTower() {
        super(GameType.FIRE_TOWER, 0);
    }

    public FireTower(int team) {
        super(GameType.FIRE_TOWER, team);
    }

    @Override
    public void attack(UpdateData data) {
        super.attack(data);
        ArrayList<EnemyEntity> entitiesInRange = getEnemiesInRange(range, data);
        int damageIncrement = (int) Math.ceil(targetDPS * getAttackSpeed());
        boolean launchedFireball = false;
        for (Entity entity : entitiesInRange) {
            if (entity instanceof EnemyEntity) {
                //System.out.println("ATTACKING ENTITY "+data.isServer());
                EnemyEntity enemy = (EnemyEntity) entity;
                enemy.damage(damageIncrement);
                new FireEffect(enemy.getEffects(), 1, 10);
                if (!launchedFireball) {
                    Projectile fireball = new Projectile(enemy, 3, 1, 1);
                    fireball.setPosition(getPosition());
                    //System.out.println("LAUNCHED FIREBALL");
                    //data.getWorld().addEntity(fireball);
                    launchedFireball = true;
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
        return 3f;
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

    @Override
    public float getDisplayRange() {
        return range;
    }
    
    
}
