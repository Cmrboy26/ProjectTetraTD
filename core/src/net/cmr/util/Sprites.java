package net.cmr.util;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Manages sprites, animations, and provides utility methods for them.
 */
public class Sprites implements Disposable {

    private Skin skin;
    private TextureAtlas skinAtlas, spriteAtlas;
    private HashMap<String, Sprite> sprites;
    private HashMap<String, Animation<TextureRegion>> animations;

    public enum SpriteType {
        CMRBOY26("cmrboy26"),
        FLOOR("wallSprites20"),
        WALL("wallSprites2"),
        HEART("heart"),
        CASH("cash"),
        STRUCTURE_LIFE("structureLife"),
        STRUCTURE("structure"),
        DARKENED("darkened"),
        PROJECTILE("area"),
        AREA("area"),
        BORDER_DEFAULT("borderDefault"),
        BORDER_SELECTED("borderSelected"),
        BORDER_HOVER("borderHover"),
        BORDER_DOWN("borderDown"),
        BORDER_DISABLED("borderDisabled"),
        SHOP_ICON("shopIcon"),
        INVENTORY_ICON("inventoryIcon"),
        FROZEN("frozen"),
        ICE_TOWER("iceTower"),
        UPGRADE_FRONT("upgradeFront"),
        UPGRADE_BACK("upgradeBack"),
        WARNING("warning"),
        UPGRADE_PROGRESS("upgradeProgress"),
        UPGRADE_PROGRESS_BACKGROUND("upgradeProgressBackground"),
        ;

        private String spriteName;
        SpriteType(String spriteName) {
            this.spriteName = spriteName;
        }
        public String getSpriteName() {
            return spriteName;
        }
    }

    static final float enemySpeed = .125f;

    public enum AnimationType {
        BASIC_ONE_DOWN("basic1/down", PlayMode.LOOP, enemySpeed),
        BASIC_ONE_UP("basic1/up", PlayMode.LOOP, enemySpeed),
        BASIC_ONE_LEFT("basic1/left", PlayMode.LOOP, enemySpeed),
        BASIC_ONE_RIGHT("basic1/right", PlayMode.LOOP, enemySpeed),

        BASIC_TWO_DOWN("basic2/down", PlayMode.LOOP, enemySpeed),
        BASIC_TWO_UP("basic2/up", PlayMode.LOOP, enemySpeed),
        BASIC_TWO_LEFT("basic2/left", PlayMode.LOOP, enemySpeed),
        BASIC_TWO_RIGHT("basic2/right", PlayMode.LOOP, enemySpeed),

        BASIC_THREE_DOWN("basic3/down", PlayMode.LOOP, enemySpeed),
        BASIC_THREE_UP("basic3/up", PlayMode.LOOP, enemySpeed),
        BASIC_THREE_LEFT("basic3/left", PlayMode.LOOP, enemySpeed),
        BASIC_THREE_RIGHT("basic3/right", PlayMode.LOOP, enemySpeed),

        BASIC_FOUR_DOWN("basic4/down", PlayMode.LOOP, enemySpeed),
        BASIC_FOUR_UP("basic4/up", PlayMode.LOOP, enemySpeed),
        BASIC_FOUR_LEFT("basic4/left", PlayMode.LOOP, enemySpeed),
        BASIC_FOUR_RIGHT("basic4/right", PlayMode.LOOP, enemySpeed),

        HEALER_ONE_DOWN("healer1/down", PlayMode.LOOP, enemySpeed),
        HEALER_ONE_UP("healer1/up", PlayMode.LOOP, enemySpeed),
        HEALER_ONE_LEFT("healer1/left", PlayMode.LOOP, enemySpeed),
        HEALER_ONE_RIGHT("healer1/right", PlayMode.LOOP, enemySpeed),

        PLAYER_DOWN("player/down", PlayMode.LOOP, enemySpeed),
        PLAYER_UP("player/up", PlayMode.LOOP, enemySpeed),
        PLAYER_LEFT("player/left", PlayMode.LOOP, enemySpeed),
        PLAYER_RIGHT("player/right", PlayMode.LOOP, enemySpeed),

        TESLA_TOWER("teslaTower", PlayMode.LOOP, .1f),
        FIRE_TOWER("fireTower", PlayMode.LOOP, .25f),
        SHOOTER_TOWER_1("shooterTower1", PlayMode.NORMAL, .1f),
        SHOOTER_TOWER_2("shooterTower2", PlayMode.NORMAL, .1f),
        SHOOTER_TOWER_3("shooterTower3", PlayMode.NORMAL, .1f),

        FIRE("fire", PlayMode.LOOP, .5f),
        SPARKLE("effects/sparkle", PlayMode.NORMAL, .075f),
        ;

        private String animationName;
        private PlayMode mode;
        private float speed;
        AnimationType(String animationName, PlayMode mode, float speed) {
            this.animationName = animationName;
            this.mode = mode;
            this.speed = speed;
        }
        public String getAnimationName() {
            return animationName;
        }
        public PlayMode getPlayMode() {
            return mode;
        }
        public float getSpeed() {
            return speed;
        }
    }

    private Sprites() {
        if (Gdx.files == null) {
            return;
        }

        this.skinAtlas = new TextureAtlas(Gdx.files.internal("skin/uiskin.atlas"));
        this.skinAtlas.getTextures().forEach(s -> {
            s.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        });
        this.skin = new Skin(Gdx.files.internal("skin/uiskin.json"), skinAtlas);
        this.skin.getFont("small-font").getData().setScale(.5f, .5f);
        this.skin.getFont("small-font").getData().setScale(.5f);
        this.sprites = new HashMap<String, Sprite>();
        this.spriteAtlas = new TextureAtlas(Gdx.files.internal("atlas/sprites.atlas"));
        for(TextureAtlas.AtlasRegion region : spriteAtlas.getRegions()) {
            Log.info("Loading sprite: " + region.name);
            sprites.put(region.name, spriteAtlas.createSprite(region.name));
        }
        Log.info("Sprites initialized and loaded: " + sprites.size());
        this.animations = new HashMap<String, Animation<TextureRegion>>();
        for (AnimationType type : AnimationType.values()) {
            PlayMode mode = type.getPlayMode();
            Array<AtlasRegion> regions = spriteAtlas.findRegions(type.getAnimationName());
            if (regions.size == 0) {
                throw new NullPointerException("No regions found for animation: " + type.getAnimationName());
            }
            Animation<TextureRegion> animation = new Animation<TextureRegion>(type.getSpeed(), regions, mode);
            Log.info("Loading animation: " + type.getAnimationName());
            animations.put(type.getAnimationName(), animation);
        }
        Log.info("Animations initialized and loaded: " + animations.size());
    }
    
    public static Skin skin() {
        return getInstance().getSkin();
    }
    public Skin getSkin() {
        return skin;
    }
    public static Sprite sprite(String name) {
        return getInstance().getSprite(name);
    }
    public Sprite getSprite(String name) {
        return sprites.get(name);
    }
    public static Sprite sprite(SpriteType type) {
        return getInstance().getSprite(type);
    }
    public Sprite getSprite(SpriteType type) {
        return sprites.get(type.getSpriteName());
    }
    public Drawable getDrawable(SpriteType type) {
        return new TextureRegionDrawable(getSprite(type));
    }
    public static Drawable drawable(SpriteType type) {
        return getInstance().getDrawable(type);
    }
    public static Drawable drawable(AnimationType type) {
        return new TextureRegionDrawable(getInstance().getAnimation(type, 0));
    }
    public static Drawable drawable(AnimationType type, float delta) {
        return new TextureRegionDrawable(getInstance().getAnimation(type, delta));
    }

    public static Animation<TextureRegion> animation(String type) {
        return getInstance().getAnimation(type);
    }
    public Animation<TextureRegion> getAnimation(String type) {
        return animations.get(type);
    }
    public static Animation<TextureRegion> animation(AnimationType type) {
        return getInstance().getAnimation(type);
    }
    public Animation<TextureRegion> getAnimation(AnimationType type) {
        return animations.get(type.getAnimationName());
    }
    public static TextureRegion animation(String type, float stateTime) {
        return getInstance().getAnimation(type, stateTime);
    }
    public TextureRegion getAnimation(String type, float stateTime) {
        return animations.get(type).getKeyFrame(stateTime);
    }
    public static TextureRegion animation(AnimationType type, float stateTime) {
        return getInstance().getAnimation(type, stateTime);
    }
    public TextureRegion getAnimation(AnimationType type, float stateTime) {
        return animations.get(type.getAnimationName()).getKeyFrame(stateTime);
    }

    public BitmapFont smallFont() {
        return skin.getFont("small-font");
    }
    public BitmapFont font() {
        return skin.getFont("default-font");
    }


    public static void disposeManager() {
        getInstance().dispose();
    }

    @Override
    public void dispose() {
        skinAtlas.dispose();
        skin.dispose();
        for (Sprite sprite : sprites.values()) {
            sprite.getTexture().dispose();
        }
        spriteAtlas.dispose();
    }

    // Singleton

    public static Sprites instance;
    public static Sprites getInstance() {
        if (instance == null) {
            instance = new Sprites();
        }
        return instance;
    }

    public static void initializeSpriteManager() {
        getInstance();
    }

}
