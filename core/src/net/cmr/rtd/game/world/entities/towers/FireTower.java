package net.cmr.rtd.game.world.entities.towers;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.entities.effects.FireEffect;

public class FireTower extends TowerEntity {

    float targetDPS = 1f;
    float range = 3;

    public FireTower() {
        super(GameType.FIRE_TOWER);
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
    
    
}
