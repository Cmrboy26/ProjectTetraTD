package net.cmr.rtd.screen;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import net.cmr.util.Sprites;
import net.cmr.util.Sprites.AnimationType;

public class AnimatedImage extends Image {

    float elapsedTime = 0;
    AnimationType type;

    public AnimatedImage(AnimationType animation) {
        this(animation, 0);
    }

    public AnimatedImage(AnimationType type, float time) {
        super(Sprites.animation(type, time));
        this.type = type;
        elapsedTime = time;

    }
    
    @Override
    public void act(float delta) {
        elapsedTime += delta;
        Drawable updatedDrawable = new TextureRegionDrawable(Sprites.animation(type, elapsedTime));
        setDrawable(updatedDrawable);
    }
    
}
