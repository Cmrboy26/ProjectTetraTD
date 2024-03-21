package net.cmr.rtd.game.world.entities.towers;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.rtd.game.world.entities.Projectile;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.AnimationType;
import net.cmr.util.Sprites.SpriteType;

public class ShooterTower extends TowerEntity {

    public ShooterTower() {
        super(GameType.SHOOTER_TOWER, 0);
    }

    public ShooterTower(int team) {
        super(GameType.SHOOTER_TOWER, team);
    }

    @Override
    public void attack(UpdateData data) {
        super.attack(data);
        float damage = getDisplayDamage();
        float range = getDisplayRange();
        ArrayList<EnemyEntity> entitiesInRange = getEnemiesInRange(range, data, getPreferedSortType());
        attacking = false;
        for (Entity entity : entitiesInRange) {
            if (entity instanceof EnemyEntity) {
                EnemyEntity enemy = (EnemyEntity) entity;
                Projectile arrow = new Projectile(enemy, SpriteType.PROJECTILE, new Vector2(getPosition()), .5f, (int) damage, getArrowAirTime(), 0, 1);
                data.getWorld().addEntity(arrow);
                animationDelta = 0;
                attacking = true;
                return;
            }
        }
    }

    public float getArrowAirTime() {
        return Math.max(.25f - ((getLevel() - 1) * .005f), 0);
    }

    @Override
    public float getAttackSpeed() {
        return Math.max(1 - ((getLevel() - 1) * .05f), 0.1f);
    }

    @Override
    protected void serializeTower(DataBuffer buffer) throws IOException {

    }

    @Override
    protected void deserializeTower(TowerEntity entity, DataInputStream input) throws IOException {

    }

    @Override
    public float getDisplayDamage() {
        float damage = calculateIncrementedValue(1, 1, 1);
        damage *= damage * .5f;
        return (float) Math.ceil(damage);
    }

    @Override
    public float getDisplayRange() {
        return calculateIncrementedValue(3, .25f, 2);
    }

    @Override
    public String getDescription() {
        return "Shoots arrows at enemies.";
    }

    float animationDelta = 0;
    boolean attacking = false;

    @Override
    public void render(Batch batch, float delta) {
        if (attacking) {
            animationDelta += delta;
        } else {
            animationDelta = 0;
        }

        Color color = new Color(Color.WHITE);
        color.a = batch.getColor().a;
        batch.setColor(color);
        TextureRegion sprite = Sprites.animation(AnimationType.SHOOTER_TOWER, animationDelta); //Sprites.sprite(Sprites.SpriteType.CMRBOY26)
        batch.draw(sprite, getX() - Tile.SIZE / 2, getY() - Tile.SIZE / 2, Tile.SIZE, Tile.SIZE);
        batch.setColor(Color.WHITE);

        super.render(batch, delta);
    }
    
}
