package net.cmr.rtd.screen;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import net.cmr.util.AbstractScreenEX;

public class ScreenViewportTest extends AbstractScreenEX {
    
    ScreenViewport viewport;
    Stage uiStage;

    public ScreenViewportTest() {
        super();
        viewport = new ScreenViewport();
        uiStage = new Stage(viewport);

        initializeTable(Align.topLeft);
        initializeTable(Align.top);
        initializeTable(Align.topRight);
        initializeTable(Align.left);
        initializeTable(Align.center);
        initializeTable(Align.right);
        initializeTable(Align.bottomLeft);
        initializeTable(Align.bottom);
        initializeTable(Align.bottomRight);
    }

    private void initializeTable(int align) {
        Table table = new Table();
        table.setFillParent(true);
        table.setDebug(true);
        table.align(align);
        uiStage.addActor(table);

        table.add("Align: " + Align.toString(align));
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        uiStage.act(delta);
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        super.dispose();
        uiStage.dispose();
    }

}
