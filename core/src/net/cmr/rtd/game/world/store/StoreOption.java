package net.cmr.rtd.game.world.store;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import net.cmr.rtd.game.GamePlayer;
import net.cmr.rtd.game.world.Entity;
import net.cmr.util.Audio.GameSFX;

public abstract class StoreOption {
    
    public final int order;
    public final String name, description;
    public final Cost cost;

    public StoreOption(int order, String name, String description, Cost cost) {
        this.order = order;
        this.name = name;
        this.description = description;
        this.cost = cost;
    }

    public abstract Entity createEntity(GamePlayer purchaser, Vector2 placePosition);
    public abstract Drawable getIcon();
    public GameSFX getPurchaseSFX() {
        return GameSFX.random(GameSFX.PLACE1, GameSFX.PLACE2);
    }

}
