package net.cmr.rtd.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Sprites;

public class LoadingScreen extends AbstractScreenEX {

    public LoadingScreen() {
        super(INITIALIZE_ALL);
    }

    @Override
    public void show() {
        super.show();

        Label loadingLabel = new Label("Loading...", Sprites.skin());
        loadingLabel.setAlignment(Align.center);
        loadingLabel.setPosition(Gdx.graphics.getWidth() / 2 - loadingLabel.getWidth() / 2, Gdx.graphics.getHeight() / 2 - loadingLabel.getHeight() / 2, Align.center);
        add(Align.center, loadingLabel);
        
    }
    
}
