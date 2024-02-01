package net.cmr.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

import net.cmr.util.Sprites.SpriteType;

public class IntroScreen extends AbstractScreenEX {

    final CMRGame game;
    final AbstractScreenEX nextScreen;
    float elapsedTime = 0;
    Label label;

    public IntroScreen(AbstractScreenEX nextScreen) {
        super(Align.center);
		this.game = CMRGame.getInstance();
        this.nextScreen = nextScreen;
        label = new Label("Cmrboy26", Sprites.getInstance().skin());
        label.setPosition(360-label.getWidth()/2, 360/2-label.getHeight()/2);
        label.setOrigin(Align.center);
        label.setFontScale(1.3f);
        add(Align.center, label);
    }

    @Override
    public void show() {
        
    }

    @Override
    public void render(float delta) {   
        elapsedTime += delta;

        // plug in elapsed time into f(x)=-x^3+x^2+x
        float x = elapsedTime/2f;
        float alpha = -x*x*x + x*x + x;
        alpha = Math.min(1, Math.max(0, alpha));


		game.batch().begin();
        game.batch().setColor(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, alpha);
        label.setColor(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, alpha);
        actDrawAll(delta);
		game.batch().setProjectionMatrix(stages.get(Align.center).getCamera().combined);
        game.batch().begin();
        game.batch().draw(Sprites.sprite(SpriteType.CMRBOY26), 130, 130, 100, 100);
		game.batch().end();

        if(elapsedTime>=3.3f || CMRGame.SKIP_INTRO) {
            if (nextScreen == null) {
                game.setScreen(null);
                return;
            }
            game.setScreen(nextScreen.fadeIn(1, Interpolation.linear));
            return;
        }
    }
    
}
