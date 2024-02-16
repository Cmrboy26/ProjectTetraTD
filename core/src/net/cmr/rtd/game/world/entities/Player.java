package net.cmr.rtd.game.world.entities;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.util.Sprites;

@WorldSerializationExempt
public class Player extends Entity {
    
    String username;
    private Vector2 velocity;

    public Player() {
        super(GameType.PLAYER);
        this.velocity = new Vector2();
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
    }

    @Override
    public void update(float delta, UpdateData data) {
        World world = data.getWorld();
        if (world == null) return;
        // Collision detection and response
        world.moveHandleCollision(this, delta, velocity);
    }

    @Override
    public void render(Batch batch, float delta) {
        batch.draw(Sprites.sprite(Sprites.SpriteType.CMRBOY26), getX() - Tile.SIZE * 1f/8f, getY(), Tile.SIZE, Tile.SIZE);
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
        player.setID(UUID.nameUUIDFromBytes(player.username.getBytes()));
    }

    public String getName() { return username; }
    public void setVelocity(Vector2 velocity) { this.velocity = velocity; }
    public Vector2 getVelocity() { return velocity; }

    

}
