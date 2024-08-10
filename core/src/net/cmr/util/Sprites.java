package net.cmr.util;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
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
        PROJECTILE("projectile"),
        AREA("area"),
        BORDER_DEFAULT("borderDefault"),
        BORDER_SELECTED("borderSelected"),
        BORDER_HOVER("borderHover"),
        BORDER_DOWN("borderDown"),
        BORDER_DISABLED("borderDisabled"),
        SHOP_ICON("shopIcon"),
        INVENTORY_ICON("inventoryIcon"),
        FROZEN("frozen"),
        UPGRADE_FRONT("upgradeFront"),
        UPGRADE_BACK("upgradeBack"),
        WARNING("warning"),
        UPGRADE_PROGRESS("upgradeProgress"),
        UPGRADE_PROGRESS_BACKGROUND("upgradeProgressBackground"),
        UPGRADE("upgrade"),
        ICON("icon"),
        TROPHY("trophy"),
        LUBRICANT("lubricant"),
        PAUSE("pause"),
        RESUME("resume"),
        SKIP("skip"),
        RESTART("restart"),
        SETTINGS("settings"),
        JOYSTICK("joystick"),
        JOYSTICK_BACKGROUND("joystickBackground"),
        BARNOLD("ripbarnold"),
        BARNOLD2("barnold2"),
        SCOPE("scope"),
        SCRAP("scrap"),
        TITANIUM("titanium"),
        STEEL("steel"),
        DIAMOND("diamond"),
        CRYONITE("cryonite"),
        THORIUM("thorium"),
        RUBY("ruby"),
        QUARTZ("quartz"),
        TOPAZ("topaz"),
        SMALL_CASH("smallCash"),
        SPEED_1("speed1"),
        SPEED_2("speed2"),
        TITLE("title"),
        PASTE_ICON("pasteIcon"),
        COPY_ICON("copyIcon"),
        DAMAGE_ICON("damage"),
        ATTACK_SPEED_ICON("attack_speed"),
        CRITICAL_ICON("critical"),
        CRITICAL_CHANCE_ICON("critical_chance"),
        LEVEL_ICON("level"),
        DPS_ICON("dps"),
        DESCRIPTION_ICON("description"),
        RANGE_ICON("range"),
        MINING_SPEED_ICON("mining_speed"),

        DRILL_TOWER_ONE("drill/drillTower1"),
        GEMSTONE_EXTRACTOR_ONE("drill/gemstoneExtractor1"),

        ICE_TOWER_1("iceTower1"),
        ICE_TOWER_2("iceTower2"),
        ICE_TOWER_3("iceTower3"),
        ICE_TOWER_4("iceTower4"),
        ICE_TOWER_5("iceTower5"),
        ICE_TOWER_6("iceTower6"),
        
        //WORLD_LEVEL("worldLevel"),
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
        TARGET("target", PlayMode.LOOP, .1f),
        
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

        BASIC_FIVE_DOWN("basic5/down", PlayMode.LOOP, enemySpeed),
        BASIC_FIVE_UP("basic5/up", PlayMode.LOOP, enemySpeed),
        BASIC_FIVE_LEFT("basic5/left", PlayMode.LOOP, enemySpeed),
        BASIC_FIVE_RIGHT("basic5/right", PlayMode.LOOP, enemySpeed),

        HEALER_ONE_DOWN("healer1/down", PlayMode.LOOP, enemySpeed),
        HEALER_ONE_UP("healer1/up", PlayMode.LOOP, enemySpeed),
        HEALER_ONE_LEFT("healer1/left", PlayMode.LOOP, enemySpeed),
        HEALER_ONE_RIGHT("healer1/right", PlayMode.LOOP, enemySpeed),

        PLAYER_DOWN("player/down", PlayMode.LOOP, enemySpeed),
        PLAYER_UP("player/up", PlayMode.LOOP, enemySpeed),
        PLAYER_LEFT("player/left", PlayMode.LOOP, enemySpeed),
        PLAYER_RIGHT("player/right", PlayMode.LOOP, enemySpeed),

        ACCESSORY_TOPHAT_DOWN("accessories/tophat/AccDown", PlayMode.LOOP, enemySpeed),
        ACCESSORY_TOPHAT_UP("accessories/tophat/AccUp", PlayMode.LOOP, enemySpeed),
        ACCESSORY_TOPHAT_LEFT("accessories/tophat/AccLeft", PlayMode.LOOP, enemySpeed),
        ACCESSORY_TOPHAT_RIGHT("accessories/tophat/AccRight", PlayMode.LOOP, enemySpeed),

        TESLA_TOWER("teslaTower", PlayMode.LOOP, .1f),
        FIRE_TOWER("fireTower", PlayMode.LOOP, .25f),
        SHOOTER_TOWER_1("shooterTower1", PlayMode.NORMAL, .1f),
        SHOOTER_TOWER_2("shooterTower2", PlayMode.NORMAL, .1f),
        SHOOTER_TOWER_3("shooterTower3", PlayMode.NORMAL, .1f),
        SHOOTER_TOWER_4("shooterTower4", PlayMode.NORMAL, .1f),
        SHOOTER_TOWER_5("shooterTower5", PlayMode.NORMAL, .1f),
        SHOOTER_TOWER_6("shooterTower6", PlayMode.NORMAL, .1f),
        SHOOTER_TOWER_7("shooterTower7", PlayMode.NORMAL, .1f),

        DRILL("drill/drill", PlayMode.LOOP_PINGPONG, .1f),

        FIRE("fire", PlayMode.LOOP, .5f),
        SPARKLE("effects/sparkle", PlayMode.NORMAL, .075f),

        WORLD_LEVEL_ANIMATED("worldLevelAnimated", PlayMode.LOOP, .2f),
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
        font().setUseIntegerPositions(false);
        smallFont().setUseIntegerPositions(false);
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
        //grayscaleShader.dispose();
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

    /*private static String vertexShader = "attribute vec4 a_position;\n" +
            "attribute vec4 a_color;\n" +
            "attribute vec2 a_texCoord0;\n" +
            "\n" +
            "uniform mat4 u_projTrans;\n" +
            "\n" +
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoords;\n" +
            "\n" +
            "void main() {\n" +
            "    v_color = a_color;\n" +
            "    v_texCoords = a_texCoord0;\n" +
            "    gl_Position = u_projTrans * a_position;\n" +
            "}";

    private static String fragmentShader = "#ifdef GL_ES\n" +
            "    precision mediump float;\n" +
            "#endif\n" +
            "\n" +
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture;\n" +
            "\n" +
            "void main() {\n" +
            "  vec4 c = v_color * texture2D(u_texture, v_texCoords);\n" +
            "  float grey = (c.r + c.g + c.b) / 3.0;\n" +
            "  gl_FragColor = vec4(grey, grey, grey, c.a);\n" +
            "}";

    public static ShaderProgram grayscaleShader = new ShaderProgram(vertexShader,
            fragmentShader);


    public static void enableGrayscale(Batch batch) {
        //batch.setShader(grayscaleShader);
    }

    public static void disableGrayscale(Batch batch) {
        //batch.setShader(null);
    }*/

}
