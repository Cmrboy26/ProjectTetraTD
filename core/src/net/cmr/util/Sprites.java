package net.cmr.util;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

/**
 * Manages sprites, animations, and provides utility methods for them.
 */
public class Sprites implements Disposable {

    private Skin skin;
    private TextureAtlas skinAtlas, spriteAtlas;
    private HashMap<String, Sprite> sprites;

    public enum SpriteType {
        CMRBOY26("cmrboy26"),
        FLOOR("wallSprites20"),
        WALL("wallSprites2"),
        HEART("heart"),
        CASH("cash"),
        STRUCTURE_LIFE("structureLife"),
        ;

        private String spriteName;
        SpriteType(String spriteName) {
            this.spriteName = spriteName;
        }
        public String getSpriteName() {
            return spriteName;
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
    public TextureRegionDrawable getDrawable(SpriteType type) {
        return new TextureRegionDrawable(getSprite(type));
    }
    public static TextureRegionDrawable drawable(SpriteType type) {
        return getInstance().getDrawable(type);
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
