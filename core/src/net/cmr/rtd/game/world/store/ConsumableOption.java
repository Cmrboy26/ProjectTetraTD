package net.cmr.rtd.game.world.store;

import com.badlogic.gdx.math.Vector2;

import net.cmr.rtd.game.GamePlayer;
import net.cmr.rtd.game.world.Entity;
import net.cmr.rtd.game.world.GameObject.GameType;
import net.cmr.rtd.game.world.entities.splashes.SlownessAOE;

public class ConsumableOption extends StoreOption {
    
    public final GameType type;

    public ConsumableOption(int order, GameType type, String name, Cost cost) {
        super(order, name, cost);
        this.type = type;
    }

    @Override
    public Entity createEntity(GamePlayer purchaser, Vector2 placePosition) {
        return new SlownessAOE(purchaser.getPlayer().getPosition(), placePosition, 5, purchaser.getTeam());
    }
    
}
