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
    int team;

    public TowerEntity(GameType type, int team) {
        super(type);
        this.team = team;
    }
    
    public void update(float delta, UpdateData data) {
        attackDelta += delta;
        while (attackDelta >= 1f/getAttackSpeed()) {
            attackDelta -= 1f/getAttackSpeed();
            attack(data);
        }
    }


    @Override
    protected void serializeEntity(DataBuffer buffer) throws IOException {
        buffer.writeFloat(attackDelta);
        buffer.writeInt(team);
        serializeTower(buffer);
    }

    @Override
    protected void deserializeEntity(GameObject object, DataInputStream input) throws IOException {
        TowerEntity tower = (TowerEntity) object;
        tower.attackDelta = input.readFloat();
        tower.team = input.readInt();
        deserializeTower(tower, input);
    }

    protected abstract void serializeTower(DataBuffer buffer) throws IOException;
    protected abstract void deserializeTower(TowerEntity entity, DataInputStream input) throws IOException;

    public ArrayList<Entity> getEntitiesInRange(double tileRadius, UpdateData data) {
        double worldDistance = tileRadius = Tile.SIZE * tileRadius;
        ArrayList<Entity> entitiesInRange = new ArrayList<Entity>();
        for (Entity entity : data.getWorld().getEntities()) {
            if (entity instanceof EnemyEntity) {
                EnemyEntity enemy = (EnemyEntity) entity;
                if (enemy.getTeam() != team) continue;
                double distance = entity.getPosition().dst(getPosition());
                if (distance <= worldDistance) {
                    entitiesInRange.add(entity);
                }
            }
        }
        return entitiesInRange;
    }

    /**
     * @return seconds between each {@link #attack(UpdateData)} call
     */
    public float getAttackSpeed() {
        return 1;
    }

    /**
     * @see #getAttackSpeed()
     * @see #getEntitiesInRange(double, UpdateData)
     * @param data world data
     */
    public void attack(UpdateData data) {}

    public int getTeam() { return team; }

}
