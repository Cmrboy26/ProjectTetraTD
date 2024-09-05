package net.cmr.rtd.game.world.store;

import com.badlogic.gdx.math.Vector2;

import net.cmr.rtd.game.GamePlayer;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.GameObject.GameType;
import net.cmr.rtd.game.world.entities.Player;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.util.Sprites.AnimationType;
import net.cmr.util.Sprites.SpriteType;

public class TowerOption extends StoreOption {

    public final GameType type;
    public final String description;
    public final SpriteType sprite;
    public final AnimationType animation;

    public TowerOption(int order, GameType type, SpriteType sprite, Cost cost, String name, String description) {
        super(order, name, cost);
        this.type = type;
        this.sprite = sprite;
        this.animation = null;
        this.description = description;
    }

    public TowerOption(int order, GameType type, AnimationType animation, Cost cost, String name, String description) {
        super(order, name, cost);
        this.type = type;
        this.sprite = null;
        this.animation = animation;
        this.description = description;
    }

    @Override
    public Entity createEntity(GamePlayer purchaser, Vector2 placePosition) {
        Class<? extends GameObject> clazz = type.getGameObjectClass();  
        if (TowerEntity.class.isAssignableFrom(clazz)) {
            try {
                return (TowerEntity) clazz.getConstructor(int.class).newInstance(purchaser.getTeam());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
