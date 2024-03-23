package net.cmr.rtd.game.world.entities;

import java.awt.Point;
import java.io.DataInputStream;
import java.io.IOException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.Pathfind;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.entities.effects.EntityEffects.EntityStat;
import net.cmr.rtd.game.world.tile.TeamTileData;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.game.world.tile.Tile.TileType;
import net.cmr.rtd.game.world.tile.TileData;
import net.cmr.util.Sprites;

public class BasicEnemy extends EnemyEntity {
    
    float speed;
    int maxHealth;
    String displayType;
    Vector2 velocity;

    Point lastTile;
    Point targetTile;
    boolean approachingEnd;

    // How much the enemy will damage the structure when it reaches the end
    public static final int DAMAGE = 1;

    public BasicEnemy() {
        super(0, GameType.BASIC_ENEMY);
    }

    public BasicEnemy(int team, String displayType, int maxHealth, float speed) {
        super(team, GameType.BASIC_ENEMY);
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.displayType = displayType;
        this.speed = speed;
    }

    @Override
    public void update(float delta, UpdateData data) {
        super.update(delta, data);

        if (targetTile == null) {
            findNextTile(data);
            return;
        }

        // Move towards the target tile
        float speed = getSpeed() * Tile.SIZE;
        velocity = Pathfind.directPathfind(this, targetTile.x, targetTile.y);
        float dx = velocity.x * speed * delta;
        float dy = velocity.y * speed * delta;
        translate(dx, dy);

        // If the distance to the center of the target tile is less than the speed, set targetTile to null and set lastTile to the targetTile
        // In short, if the enemy has reached the target tile, move to the next one
        float distance = new Vector2(targetTile.x * Tile.SIZE + Tile.SIZE / 2 - getX(), targetTile.y * Tile.SIZE + Tile.SIZE / 2 - getY()).len();
        if (distance < speed * delta) {
            targetTile = null;

            if (approachingEnd) {
                int tileX = getTileX(this);
                int tileY = getTileY(this);
                attackStructure(tileX, tileY, data, DAMAGE);
                removeFromWorld();
            }
        }
    }

    private void findNextTile(UpdateData data) {
        int tileX = getTileX(this);
        int tileY = getTileY(this);
        if (tileX == 10 && tileY == 5) {
            System.out.println("10, 5");
        }
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
        buffer.writeFloat(speed);
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
        enemy.speed = input.readFloat();
    }

    @Override
    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    public float getTargetSpeed() {
        return speed * getEffects().getStatMultiplier(EntityStat.SPEED);
    }

    @Override
    public float getRenderOffset() {
        return -Tile.SIZE / 2;
    }

    float alphaDecay = Float.MAX_VALUE;
    float animationDelta = 0;

    @Override
    public void render(Batch batch, float delta) {
        // Draw the enemy

        if (approachingEnd) {
            if (alphaDecay == Float.MAX_VALUE) {
                alphaDecay = 5f;
            }
            alphaDecay -= delta * 8f * speed;
            if (alphaDecay < 0) {
                alphaDecay = 0;
            }
        }
        animationDelta += delta * getSpeed();

        Color color = new Color(getEffects().getDiscoloration());
        color.a = Math.min(1, alphaDecay);
        batch.setColor(color);
        String directionString = "/";
        // Add "up", "down", "left", or "right" to the displayType to get the correct sprite
        float threshold = .05f;
        if (velocity == null) {
            velocity = new Vector2();
        }
        if (velocity.x > threshold) {
            directionString = "/right";
        } else if (velocity.x < -threshold) {
            directionString = "/left";
        } else if (velocity.y > threshold) {
            directionString = "/up";
        } else if (velocity.y < -threshold) {
            directionString = "/down";
        } else {
            directionString = "/down";
        }
        batch.draw(Sprites.animation(displayType+directionString, animationDelta), getX() - Tile.SIZE / 2, getY() - Tile.SIZE / 2, Tile.SIZE, Tile.SIZE);
        batch.setColor(Color.WHITE);
        super.render(batch, delta);
    }

    @Override
    public void onDamage(int damage, UpdateData data, DamageType type) {
        playHitSound(data, type);
    }


    @Override
    public void onDeath(UpdateData data) {
        super.onDeath(data);
        //playHitSound(data, DamageType.PHYSICAL);
        //int money = (int) Math.floor(Math.pow(maxHealth, 1.5d) / 10d);
        int money = (int) Math.floor(maxHealth * .4f - 1f);
        moneyOnDeath(this, data, money);
    }

    @Override
    public Vector2 getVelocity() {
        if (velocity == null) {
            velocity = new Vector2();
        }
        return new Vector2(velocity).scl(getSpeed());
    }

}
