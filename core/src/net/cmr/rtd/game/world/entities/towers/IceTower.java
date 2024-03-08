package net.cmr.rtd.game.world.entities.towers;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.EnemyEntity;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.entities.effects.SlownessEffect;

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

}
