package net.cmr.rtd.game.world.entities;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.GamePlayer;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.game.world.entities.effects.EntityEffects.EntityStat;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.util.CMRGame;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.AnimationType;

@WorldSerializationExempt
public class Player extends Entity {
    
    private Vector2 velocity;
    private String username;
    private int health;

    public Player() {
        super(GameType.PLAYER);
        this.velocity = new Vector2();
        this.health = getMaxHealth();
    }

    public Player(final String username) {
        this();
        this.username = username;
        this.setID(UUID.nameUUIDFromBytes(username.getBytes()));
    }

    @Override
    public void create() {
        super.create();
    }

    @Override
    public void remove() {
        super.remove();
    }

    @Override
    protected void serializeEntity(DataBuffer buffer) throws IOException {
        buffer.writeUTF(username);
        buffer.writeInt(health);
    }

    @Override
    public void update(float delta, UpdateData data) {
        World world = data.getWorld();
        if (world == null) return;
        // Collision detection and response
        world.moveHandleCollision(this, delta, velocity);
    }

    float animationDelta = 0;

    @Override
    public void render(Batch batch, float delta) {
        animationDelta += delta;
        TextureRegion sprite = Sprites.animation(AnimationType.TESLA_TOWER, animationDelta); //Sprites.sprite(Sprites.SpriteType.CMRBOY26)
        batch.draw(sprite, getX() - Tile.SIZE * 1f/8f, getY(), Tile.SIZE, Tile.SIZE);
        super.render(batch, delta);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), Tile.SIZE * (3f/4f), Tile.SIZE / 2f);
    }

    @Override
    protected void deserializeEntity(GameObject object, DataInputStream input) throws IOException {
        Player player = (Player) object;
        player.username = input.readUTF();
        player.health = input.readInt();
        player.setID(UUID.nameUUIDFromBytes(player.username.getBytes()));
    }

    public String getName() { return username; }
    public void setVelocity(Vector2 velocity) { this.velocity = velocity; }
    public Vector2 getVelocity() { return velocity; }
    public int getHealth() { return health; }

    public void damage(int amount, UpdateData data) {
        health -= amount;
        if (health < 0) health = 0;
        if (data.isServer()) {
            GameManager manager = data.getManager();
            GamePlayer player = manager.getPlayer(this);
            if (player != null) {
                manager.sendStatsUpdatePacket(this);
            }
        }
    }

    public int getMaxHealth() { 
        return 10;
    }

    public float getSpeed() {
        return 4 * getEffects().getStatMultiplier(EntityStat.SPEED);
    }

    public float getSprintMultiplier() {
        return 1.5f;
    }

    public void updateInput(Vector2 input, boolean sprinting) {

        float deadZone = CMRGame.DEADZONE;

        if (input.len() < deadZone) {
            input.set(0, 0);
            velocity.set(0, 0);
            return;
        }

        float velocityX = Math.abs(input.x);
        float velocityY = Math.abs(input.y);

        float angle = (float) Math.atan2(input.y, input.x);
        velocityX = (float) Math.cos(angle);
        velocityY = (float) Math.sin(angle);

        input.set(velocityX, velocityY);

        if (sprinting) {
            input.scl(getSprintMultiplier());
        }

        velocity.set(input).scl(getSpeed() * Tile.SIZE);
    }

    

}
