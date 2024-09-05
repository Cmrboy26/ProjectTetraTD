package net.cmr.rtd.game.world.entities.splashes;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.function.Consumer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.DataBuffer;

import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.UpdateData;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.SpriteType;

public abstract class SplashAOE extends Entity {

    final static int VERSION = 0;
    final static float THROW_TIME = 0.5f;
    final static float SPLASH_RANGE = 2.5f;

    private Vector2 throwOffset;
    private int lingerDuration;
    private int team;

    private float timeAlive = 0;

    /**
     * Empty constructor for serialization.
     */
    public SplashAOE(GameType splashType) {
        super(splashType);
    }

    /**
     * Creates a new splash AOE entity.
     * @param throwPosition The position the AOE was thrown from (AKA the player's position, used for rendering).
     * @param targetPosition The position the AOE was thrown at (the center of the AOE).
     * @param lingerDuration The duration (in seconds) the AOE should linger for. 0 means it is instant.
     */
    public SplashAOE(GameType splashType, Vector2 throwPosition, Vector2 targetPosition, int lingerDuration, int team) {
        super(splashType);
        this.throwOffset = targetPosition.cpy().sub(throwPosition);
        this.lingerDuration = lingerDuration;
        setPosition(targetPosition);
    }

    @Override
    protected void serializeEntity(DataBuffer buffer) throws IOException {
        buffer.writeInt(VERSION);
        buffer.writeFloat(throwOffset.x);
        buffer.writeFloat(throwOffset.y);
        buffer.writeInt(lingerDuration);
        buffer.writeInt(team);
    }

    @Override
    protected void deserializeEntity(GameObject object, DataInputStream input) throws IOException {
        SplashAOE splash = (SplashAOE) object;
        int version = input.readInt();
        splash.throwOffset = new Vector2(input.readFloat(), input.readFloat());
        splash.lingerDuration = input.readInt();
        splash.team = input.readInt();
    }

    @Override
    public void update(float delta, UpdateData data) {
        super.update(delta, data);
        timeAlive += delta;
        if (timeAlive < THROW_TIME) {
            return;
        }

        float totalLingeringTime = lingerDuration + THROW_TIME;
        boolean isInstant = lingerDuration == 0;

        // Apply AOE to targets in range
        for (Entity entity : data.getWorld().getEntities()) {
            // TODO: The splash AOE itself gets inflicted with the debuff. This is not intended.
            float distance = entity.getPosition().dst(getPosition());
            if (distance <= SPLASH_RANGE) {
                if (targetsPlayers() && entity.type == GameType.PLAYER) {
                    applyEffect(data, entity);
                } else if (targetsEnemies() && entity.type != GameType.PLAYER) {
                    applyEffect(data, entity);
                }
            }
        }

        if (isInstant || timeAlive > totalLingeringTime) {
            removeFromWorld();
        }
    }

    @Override
    public void render(UpdateData data, Batch batch, float delta) {
        super.render(data, batch, delta);

        if (throwOffset == null) {
            return;
        }

        float splashProgress = Math.min(timeAlive / THROW_TIME, 1);
        if (splashProgress < 1) {
            // Have the splash's y position go up and down as it is thrown
            Vector2 renderPosition = getPosition().cpy().sub(throwOffset.cpy().scl(1 - splashProgress));
            renderPosition.add(0, Tile.SIZE / 2f);
            batch.setColor(getColor());
            batch.draw(Sprites.sprite(SpriteType.BARNOLD), renderPosition.x, renderPosition.y, Tile.SIZE / 2f, Tile.SIZE / 2f);
            batch.setColor(Color.WHITE);
        } else {
            float lingerProgress = Math.min((timeAlive - THROW_TIME) / lingerDuration, 1);
            //if (lingerProgress < 1) {
                batch.setColor(getColor());
                batch.draw(Sprites.sprite(SpriteType.AREA), getX() - (SPLASH_RANGE * Tile.SIZE), getY() - (SPLASH_RANGE * Tile.SIZE), SPLASH_RANGE * 2 * Tile.SIZE, SPLASH_RANGE * 2 * Tile.SIZE);
                batch.setColor(Color.WHITE);
            //}
        }
    }

    public abstract boolean targetsPlayers();
    public abstract boolean targetsEnemies();
    public abstract void applyEffect(UpdateData data, Entity entity);
    public Color getColor() {
        return Color.WHITE;
    }
    
}
