package net.cmr.rtd.game.world.entities;

import java.io.DataInputStream;
import java.io.IOException;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.game.world.tile.StructureTileData;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.game.world.tile.TileData;

public abstract class EnemyEntity extends Entity {

    public int team;
    public int health;

    protected EnemyEntity(int team, GameType type) {
        super(type);
        this.team = team;
    }

    @Override
    public void update(float delta, UpdateData data) {
        
    }

    public Rectangle getBounds() {
        return new Rectangle(getX() - Tile.SIZE / 2, getY() - Tile.SIZE / 2, Tile.SIZE, Tile.SIZE);
    }

    protected abstract void serializeEnemy(DataBuffer buffer) throws IOException;
    protected abstract void deserializeEnemy(GameObject object, DataInputStream input) throws IOException;

    public abstract int getMaxHealth();
    public abstract float getSpeed();

    public int getHealth() {
        return health;
    }
    public void setHealth(int health) {
        this.health = health;
    }
    public void damage(int damage) {
        health -= damage;
    }
    public void heal(int heal) {
        health += heal;
    }
    public int getTeam() {
        return team;
    }

    public void attackStructure(int tileX, int tileY, UpdateData data, int damage) {
        // Get the endTileData from the tile at the given coordinates
        // Call the damage method on the endTileData
        World world = data.getWorld();
        TileData tileData = world.getTileData(tileX, tileY, 1);
        if (tileData instanceof StructureTileData) {
            StructureTileData endTileData = (StructureTileData) tileData;
            endTileData.damage(damage);
        }
    }

    @Override
    protected final void serializeEntity(DataBuffer buffer) throws IOException {
        buffer.writeInt(team);
        buffer.writeInt(health);
        serializeEnemy(buffer);
    }

    @Override
    protected final void deserializeEntity(GameObject object, DataInputStream input) throws IOException {
        EnemyEntity entity = (EnemyEntity) object;
        entity.team = input.readInt();
        entity.health = input.readInt();
        deserializeEnemy(object, input);
    }

}
