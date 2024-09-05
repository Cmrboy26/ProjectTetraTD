package net.cmr.rtd.game.world.store;

import com.badlogic.gdx.math.Vector2;

import net.cmr.rtd.game.GamePlayer;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.entities.Player;

public abstract class StoreOption {
    
    public final int order;
    public final String name;
    public final Cost cost;

    public StoreOption(int order, String name, Cost cost) {
        this.order = order;
        this.name = name;
        this.cost = cost;
    }

    public abstract Entity createEntity(GamePlayer purchaser, Vector2 placePosition);

}
