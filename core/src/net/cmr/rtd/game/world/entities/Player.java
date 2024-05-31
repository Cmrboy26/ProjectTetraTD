package net.cmr.rtd.game.world.entities;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.GamePlayer;
import net.cmr.rtd.game.packets.JumpPacket;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.game.world.entities.effects.EntityEffects.EntityStat;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.screen.GameScreen;
import net.cmr.util.CMRGame;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.AnimationType;

@WorldSerializationExempt
public class Player extends Entity {
    
    private Vector2 velocity;
    private String username;
    private int health;
    private transient float jumpProgress = 0;

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
        world.moveHandleCollision(this, delta, new Vector2(velocity));
        if (jumpProgress > 0) {
            jumpProgress -= delta;
        }
    }

    float animationDelta = 0;
    GlyphLayout layout;

    final float jumpTime = .5f; 
    final float jumpHeight = .4f;

    @Override
    public void render(UpdateData data, Batch batch, float delta) {
        GameScreen screen = data.getScreen();

        float x = jumpProgress;
        float jumpY = Math.max(0, (float) -4*x*(x-jumpTime)/(jumpTime*jumpTime));
        if (screen.getLocalPlayer() != null && !screen.getLocalPlayer().getName().equals(username)) {
            // DRAW THE NAME
            batch.setColor(Color.WHITE);
            BitmapFont font = Sprites.skin().getFont("small-font");
            font.setColor(Color.WHITE);
            float scaleBefore = font.getData().scaleX;
            font.getData().setScale(scaleBefore / 2f);
            if(layout == null) {
                layout = new GlyphLayout(font, username);
            }
            font.draw(batch, username, getX() - Tile.SIZE * 1f/8f + Tile.SIZE / 2f - layout.width/2f, getY() + Tile.SIZE * 1.25f + (jumpY*(Tile.SIZE*jumpHeight)));
            font.getData().setScale(scaleBefore);
        }

        animationDelta += delta;
        updateMovementRendering(delta);
        TextureRegion sprite = Sprites.animation(getAnimationFromMovement(), movementCountdown);
        //TextureRegion sprite = /*Sprites.animation(AnimationType.TESLA_TOWER, animationDelta); //*/Sprites.sprite(Sprites.SpriteType.CMRBOY26);
        batch.draw(sprite, getX() - Tile.SIZE * 1f/8f, getY()+jumpY*(Tile.SIZE*jumpHeight), Tile.SIZE, Tile.SIZE);
        super.render(data, batch, delta);
    }

    int direction = 0; // up = 0, down = 1, left = 2, right = 3
    boolean moving = false;
    float movementCountdown;
    static final float renderSlowdownFactor = 3;

    private void updateMovementRendering(float delta) {
        if (moving) {
            movementCountdown += delta * (velocity.len() / Tile.SIZE) / renderSlowdownFactor;
        }
        int lastDirection = direction;
        boolean lastMoving = moving;
        if (velocity.len() > 0) {
            if (Math.abs(velocity.x) > Math.abs(velocity.y)) {
                if (velocity.x > 0) {
                    direction = 3;
                } else {
                    direction = 2;
                }
            } else {
                if (velocity.y > 0) {
                    direction = 0;
                } else {
                    direction = 1;
                }
            }
        }
        moving = velocity.len() > 0;

        if (lastDirection != direction || lastMoving != moving) {
            movementCountdown = 0;
        }
    }

    public boolean jump(UpdateData data) {
        if (jumpProgress > 0) return false;
        if (jumpProgress <= 0) {
            jumpProgress = jumpTime;
        }
        if (data.isServer()) {
            GameManager manager = data.getManager();
            GamePlayer player = manager.getPlayer(this);
            if (player != null) {
                JumpPacket packet = new JumpPacket(getID());
                manager.sendPacketToAll(packet);
            }
        }
        return true;
    }

    private AnimationType getAnimationFromMovement() {
        if (direction == 0) return AnimationType.PLAYER_UP;
        if (direction == 1) return AnimationType.PLAYER_DOWN;
        if (direction == 2) return AnimationType.PLAYER_LEFT;
        if (direction == 3) return AnimationType.PLAYER_RIGHT;
        return AnimationType.PLAYER_DOWN;
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
