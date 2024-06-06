package net.cmr.rtd.game.world.store;

import net.cmr.rtd.game.storage.TeamInventory;
import net.cmr.rtd.game.world.GameObject.GameType;
import net.cmr.util.Sprites.AnimationType;
import net.cmr.util.Sprites.SpriteType;

public class TowerOption {

    public final int order;
    public final GameType type;
    public final Cost cost;
    public final String name;
    public final String description;
    public final SpriteType sprite;
    public final AnimationType animation;

    public TowerOption(int order, GameType type, SpriteType sprite, Cost cost, String name, String description) {
        this.order = order;
        this.type = type;
        this.sprite = sprite;
        this.animation = null;
        this.cost = cost;
        this.name = name;
        this.description = description;
    }

    public TowerOption(int order, GameType type, AnimationType animation, Cost cost, String name, String description) {
        this.order = order;
        this.type = type;
        this.sprite = null;
        this.animation = animation;
        this.cost = cost;
        this.name = name;
        this.description = description;
    }

}
