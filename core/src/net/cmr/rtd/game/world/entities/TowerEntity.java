package net.cmr.rtd.game.world.entities;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.tile.Tile;

public abstract class TowerEntity extends Entity {

    float attackDelta = 0;

    public TowerEntity(GameType type) {
        super(type);
    }
    
    public void update(float delta, UpdateData data) {
        attackDelta += delta;
        if (attackDelta >= 1) {
            attackDelta = 0;
            attack(data);
        }
    }


    @Override
    protected void serializeEntity(DataBuffer buffer) throws IOException {
        buffer.writeFloat(attackDelta);
        serializeTower(buffer);
    }

    @Override
    protected void deserializeEntity(GameObject object, DataInputStream input) throws IOException {
        TowerEntity tower = (TowerEntity) object;
        tower.attackDelta = input.readFloat();
        deserializeTower(tower, input);
    }

    protected abstract void serializeTower(DataBuffer buffer) throws IOException;
    protected abstract void deserializeTower(TowerEntity entity, DataInputStream input) throws IOException;

    public ArrayList<Entity> getEntitiesInRange(double tileRadius, UpdateData data) {
        double worldDistance = tileRadius = Tile.SIZE * tileRadius;
        ArrayList<Entity> entitiesInRange = new ArrayList<Entity>();
        for (Entity entity : data.getWorld().getEntities()) {
            if (entity instanceof TowerEntity) {
                double distance = entity.getPosition().dst(getPosition());
                if (distance <= worldDistance) {
                    entitiesInRange.add(entity);
                }
            }
        }
        return entitiesInRange;
    }

    public float getAttackSpeed() {
        return 1;
    }
    public void attack(UpdateData data) {}

}
