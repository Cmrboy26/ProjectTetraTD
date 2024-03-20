package net.cmr.rtd.game.world.store;

import net.cmr.rtd.game.world.GameObject.GameType;
import net.cmr.util.Sprites.AnimationType;
import net.cmr.util.Sprites.SpriteType;

public class TowerOption {

    public final GameType type;
    public final long cost;
    public final String name;
    public final String description;
    public final SpriteType sprite;
    public final AnimationType animation;

    public TowerOption(GameType type, SpriteType sprite, long cost, String name, String description) {
        this.type = type;
        this.sprite = sprite;
        this.animation = null;
        this.cost = cost;
        this.name = name;
        this.description = description;
    }

    public TowerOption(GameType type, AnimationType animation, long cost, String name, String description) {
        this.type = type;
        this.sprite = null;
        this.animation = animation;
        this.cost = cost;
        this.name = name;
        this.description = description;
    }

}
