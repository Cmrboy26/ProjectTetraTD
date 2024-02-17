package net.cmr.rtd.game.world.entities;

import java.awt.Point;
import java.io.DataInputStream;
import java.io.IOException;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.Pathfind;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.tile.TeamTileData;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.game.world.tile.Tile.TileType;
import net.cmr.rtd.game.world.tile.TileData;
import net.cmr.util.Sprites;

public class BasicEnemy extends EnemyEntity {
    
    int maxHealth;
    String displayType;

    Point lastTile;
    Point targetTile;
    boolean approachingEnd;

    public BasicEnemy() {
        super(0, GameType.BASIC_ENEMY);
    }

    public BasicEnemy(int team, String displayType, int maxHealth) {
        super(team, GameType.BASIC_ENEMY);
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.displayType = displayType;
    }

    @Override
    public void update(float delta, UpdateData data) {
        
        if (targetTile == null) {
            findNextTile(data);
            return;
        }

        // Move towards the target tile
        float speed = getSpeed() * Tile.SIZE * 2;
        Vector2 direction = Pathfind.directPathfind(this, targetTile.x, targetTile.y);
        float dx = direction.x * speed * delta;
        float dy = direction.y * speed * delta;
        translate(dx, dy);

        // If the distance to the center of the target tile is less than the speed, set targetTile to null and set lastTile to the targetTile
        // In short, if the enemy has reached the target tile, move to the next one
        float distance = new Vector2(targetTile.x * Tile.SIZE + Tile.SIZE / 2 - getX(), targetTile.y * Tile.SIZE + Tile.SIZE / 2 - getY()).len();
        if (distance < speed * delta) {
            targetTile = null;

            if (approachingEnd) {
                // TODO: Damage the structure and update the health bar on the client side
                removeFromWorld();
            }
        }
    }

    private void findNextTile(UpdateData data) {
        int tileX = getTileX(this);
        int tileY = getTileY(this);
        int lastTileX = lastTile == null ? tileX : lastTile.x;
        int lastTileY = lastTile == null ? tileY : lastTile.y;
        int dX = tileX - lastTileX;
        int dY = tileY - lastTileY;

        TileType left = data.getWorld().getTile(tileX - 1, tileY, 1);
        TileType right = data.getWorld().getTile(tileX + 1, tileY, 1);
        TileType up = data.getWorld().getTile(tileX, tileY + 1, 1);
        TileType down = data.getWorld().getTile(tileX, tileY - 1, 1);

        TileData leftData = data.getWorld().getTileData(tileX - 1, tileY, 1);
        TileData rightData = data.getWorld().getTileData(tileX + 1, tileY, 1);
        TileData upData = data.getWorld().getTileData(tileX, tileY + 1, 1);
        TileData downData = data.getWorld().getTileData(tileX, tileY - 1, 1);

        boolean leftBlocked = lastTile != null && lastTile.x == tileX - 1 && lastTile.y == tileY;
        boolean rightBlocked = lastTile != null && lastTile.x == tileX + 1 && lastTile.y == tileY;
        boolean upBlocked = lastTile != null && lastTile.x == tileX && lastTile.y == tileY + 1;
        boolean downBlocked = lastTile != null && lastTile.x == tileX && lastTile.y == tileY - 1;

        if (leftData instanceof TeamTileData && ((TeamTileData) leftData).team != team) {
            leftBlocked = true;
        }
        if (rightData instanceof TeamTileData && ((TeamTileData) rightData).team != team) {
            rightBlocked = true;
        }
        if (upData instanceof TeamTileData && ((TeamTileData) upData).team != team) {
            upBlocked = true;
        }
        if (downData instanceof TeamTileData && ((TeamTileData) downData).team != team) {
            downBlocked = true;
        }

        if ((dX < 0 && left == TileType.PATH || left == TileType.END) && !leftBlocked) {
            targetTile = new Point(tileX - 1, tileY);
            approachingEnd = left == TileType.END;
        } else if ((dX > 0 && right == TileType.PATH || right == TileType.END) && !rightBlocked) {
            targetTile = new Point(tileX + 1, tileY);
            approachingEnd = right == TileType.END;
        } else if ((dY > 0 && up == TileType.PATH || up == TileType.END) && !upBlocked) {
            targetTile = new Point(tileX, tileY + 1);
            approachingEnd = up == TileType.END;
        } else if ((dY < 0 && down == TileType.PATH || down == TileType.END) && !downBlocked) {
            targetTile = new Point(tileX, tileY - 1);
            approachingEnd = down == TileType.END;
        } else {
            if ((left == TileType.PATH || left == TileType.END) && !leftBlocked) {
                targetTile = new Point(tileX - 1, tileY);
                approachingEnd = left == TileType.END;
            } else if ((right == TileType.PATH || right == TileType.END) && !rightBlocked) {
                targetTile = new Point(tileX + 1, tileY);
                approachingEnd = right == TileType.END;
            } else if ((up == TileType.PATH || up == TileType.END) && !upBlocked) {
                targetTile = new Point(tileX, tileY + 1);
                approachingEnd = up == TileType.END;
            } else if ((down == TileType.PATH || down == TileType.END) && !downBlocked) {
                targetTile = new Point(tileX, tileY - 1);
                approachingEnd = down == TileType.END;
            }
        }
        lastTile = new Point(tileX, tileY);

    }

    @Override
    protected void serializeEnemy(DataBuffer buffer) throws IOException {
        buffer.writeUTF(displayType);
        if (targetTile == null) {
            buffer.writeInt(-1);
        } else {
            buffer.writeInt(targetTile.x);
            buffer.writeInt(targetTile.y);
        }
        if (lastTile == null) {
            buffer.writeInt(-1);
        } else {
            buffer.writeInt(lastTile.x);
            buffer.writeInt(lastTile.y);
        }
        buffer.writeBoolean(approachingEnd);
    }

    @Override
    protected void deserializeEnemy(GameObject object, DataInputStream input) throws IOException {
        BasicEnemy enemy = (BasicEnemy) object;
        enemy.displayType = input.readUTF();
        int targetTileX = input.readInt();
        if (targetTileX == -1) {
            enemy.targetTile = null;
        } else {
            enemy.targetTile = new Point(targetTileX, input.readInt());
        }
        int lastTileX = input.readInt();
        if (lastTileX == -1) {
            enemy.lastTile = null;
        } else {
            enemy.lastTile = new Point(lastTileX, input.readInt());
        }
        enemy.approachingEnd = input.readBoolean();
    }

    @Override
    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    public float getSpeed() {
        return 1;
    }

    @Override
    public void render(Batch batch, float delta) {
        // Draw the enemy
        batch.draw(Sprites.sprite(displayType), getX() - Tile.SIZE / 2, getY() - Tile.SIZE / 2, Tile.SIZE, Tile.SIZE);
        super.render(batch, delta);
    }

}
