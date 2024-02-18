package net.cmr.rtd.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.cmr.rtd.RetroTowerDefense;
import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.GameManager.GameManagerDetails;
import net.cmr.rtd.game.GameSave;
import net.cmr.rtd.game.stream.LocalGameStream;
import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Sprites;

public class MainMenuScreen extends AbstractScreenEX {
    
    LocalGameStream clientsideStream, serversideStream;
    GameManager manager;

    public MainMenuScreen() {
        super(INITIALIZE_ALL);

		Table table = new Table();
		table.setFillParent(true);

		Label label = new Label("Retro Tower Defense", Sprites.skin(), "default");
		label.setAlignment(Align.center);
		table.add(label).padTop(20.0f).padBottom(20.0f);

		String labelType = "toggle-small";

		table.row();
		TextButton textButton = new TextButton("Play", Sprites.skin(), labelType);
		textButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				GameManagerDetails details = new GameManagerDetails();
				GameSave save = new GameSave("testSave");
				RetroTowerDefense.getInstance(RetroTowerDefense.class).joinSingleplayerGame(details, save);
				//RetroTowerDefense.getInstance(RetroTowerDefense.class).joinOnlineGame("localhost", 11265);
			}
		});
		table.add(textButton).padLeft(100.0f).padRight(100.0f).space(10.0f).fillX();
		table.row();

		TextButton editor = new TextButton("Editor", Sprites.skin(), labelType);
		editor.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				FileHandle handle = Gdx.files.external("editorWorld.dat");
				fadeToScreen(new EditorScreen(handle), .5f, Interpolation.linear, false);
			}
		});
		table.add(editor).padLeft(100.0f).padRight(100.0f).space(10.0f).fillX();
		table.row();

		textButton = new TextButton("Settings", Sprites.skin(), labelType);
		textButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				
			}
		});
		table.add(textButton).padLeft(100.0f).padRight(100.0f).space(10.0f).fillX();
		table.row();

		textButton = new TextButton("Exit", Sprites.skin(), labelType);
		textButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Gdx.app.exit();
			}
		});
		table.add(textButton).padLeft(100.0f).padRight(100.0f).space(10.0f).fillX();
		add(Align.center, table);
    }

    @Override
    public void render(float delta) {
        game.batch().setColor(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, 1);
        super.render(delta);
    }
    
    @Override
    public void hide() {
        super.hide();
    }

}
