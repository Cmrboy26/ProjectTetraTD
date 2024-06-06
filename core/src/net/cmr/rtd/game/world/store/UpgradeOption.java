package net.cmr.rtd.game.world.store;

import java.util.function.Function;

import net.cmr.rtd.game.world.GameObject.GameType;

public class UpgradeOption {
    
    public final Cost cost; // takes the current level and returns the cost of the next level
    public final Function<Integer, Float> levelUpTime; // takes the current level and returns the time it takes to upgrade to the next level
    public final GameType type;

    public UpgradeOption(GameType type, Cost cost, Function<Integer, Float> levelUpTime) {
        this.type = type;
        this.cost = cost;
        this.levelUpTime = levelUpTime;
    }

}
