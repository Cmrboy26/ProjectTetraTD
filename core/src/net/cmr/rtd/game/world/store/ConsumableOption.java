package net.cmr.rtd.game.world.store;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import net.cmr.rtd.game.GamePlayer;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.GameObject.GameType;
import net.cmr.rtd.game.world.entities.TowerEntity;
import net.cmr.rtd.game.world.entities.splashes.BlindnessAOE;
import net.cmr.rtd.game.world.entities.splashes.SplashAOE;
import net.cmr.util.Sprites;
import net.cmr.util.Audio.GameSFX;
import net.cmr.util.Sprites.SpriteType;

public class ConsumableOption extends StoreOption {
    
    public final GameType type;
    public final SpriteType icon;
    public final int duration;

    public ConsumableOption(int order, GameType type, SpriteType icon, String name, Cost cost, String description, int duration) {
        super(order, name, description, cost);
        this.icon = icon;
        this.type = type;
        this.duration = duration;
    }

    @Override
    public Entity createEntity(GamePlayer purchaser, Vector2 placePosition) {
        Class<? extends GameObject> clazz = type.getGameObjectClass();  
        if (SplashAOE.class.isAssignableFrom(clazz)) {
            try {
                return (SplashAOE) clazz.getConstructor(Vector2.class, Vector2.class, int.class, int.class)
                        .newInstance(purchaser.getPlayer().getPosition(), placePosition, duration, purchaser.getTeam());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
        //return new BlindnessAOE(purchaser.getPlayer().getPosition(), placePosition, 5, purchaser.getTeam());
    }

    @Override
    public GameSFX getPurchaseSFX() {
        return GameSFX.SCARY_WARNING;
    }

    @Override
    public Drawable getIcon() {
        return Sprites.drawable(icon);
    }
    
}
