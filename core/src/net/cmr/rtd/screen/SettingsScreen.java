package net.cmr.rtd.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.cmr.rtd.RetroTowerDefense;
import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Settings;
import net.cmr.util.Sprites;

public class SettingsScreen extends AbstractScreenEX {

    public SettingsScreen() {
        super(INITIALIZE_ALL);

		Table table = new Table();
		table.setFillParent(true);

		Label label = new Label("Settings", Sprites.skin(), "default");
		label.setAlignment(Align.center);
		table.add(label).top().padTop(20.0f).padBottom(20.0f);

		String labelType = "small";

		table.row();

        Table settingsTable = new Table();
        table.add(settingsTable).expand().fill();

        // Settings Begin
        
        // Volume Sliders
        settingsTable.add(new Label("Master Volume", Sprites.skin(), labelType)).left().padRight(10.0f).colspan(1);
        settingsTable.add(new Label("Music Volume", Sprites.skin(), labelType)).left().padRight(10.0f).colspan(1);
        settingsTable.add(new Label("SFX Volume", Sprites.skin(), labelType)).left().padRight(10.0f).colspan(1);

        settingsTable.row();

        Slider masterVolume = new Slider(0.0f, 1.0f, 0.01f, false, Sprites.skin());
        masterVolume.setValue(Settings.getPreferences().getFloat(Settings.MASTER_VOLUME));
        settingsTable.add(masterVolume).expandX().fillX().colspan(1).pad(10);

        Slider musicVolume = new Slider(0.0f, 1.0f, 0.01f, false, Sprites.skin());
        musicVolume.setValue(Settings.getPreferences().getFloat(Settings.MUSIC_VOLUME));
        settingsTable.add(musicVolume).expandX().fillX().colspan(1).pad(10);

        Slider sfxVolume = new Slider(0.0f, 1.0f, 0.01f, false, Sprites.skin());
        sfxVolume.setValue(Settings.getPreferences().getFloat(Settings.SFX_VOLUME));
        settingsTable.add(sfxVolume).expandX().fillX().colspan(1).pad(10).row();

        // Username
        settingsTable.add(new Label("Username", Sprites.skin(), labelType)).left().padRight(10.0f).colspan(1);
        settingsTable.add(new Label("Show FPS", Sprites.skin(), labelType)).left().padRight(10.0f).colspan(1);
        settingsTable.add(new Label("Show Placement Grid", Sprites.skin(), labelType)).left().padRight(10.0f).colspan(1);

        settingsTable.row();
        
        TextField username = new TextField("Username", Sprites.skin(), "small");
        username.setMessageText("Username");
        username.setText(Settings.getPreferences().getString(Settings.USERNAME));
        username.setAlignment(Align.center);
        username.setMaxLength(16);
        settingsTable.add(username).expandX().fillX().colspan(3).pad(10).colspan(1);

        CheckBox showFPS = new CheckBox("", Sprites.skin());
        showFPS.setChecked(Settings.getPreferences().getBoolean(Settings.SHOW_FPS));
        settingsTable.add(showFPS).expandX().fillX().colspan(1).pad(10);

        CheckBox showPlacementGrid = new CheckBox("", Sprites.skin());
        showPlacementGrid.setChecked(Settings.getPreferences().getBoolean(Settings.SHOW_PLACEMENT_GRID));
        settingsTable.add(showPlacementGrid).expandX().fillX().colspan(1).pad(10).row();

        // Settings End

        table.row();
        
        Table horizontalGroup = new Table();

        TextButton apply = new TextButton("Apply", Sprites.skin(), labelType);
        apply.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Save settings
                Settings.getPreferences().putFloat(Settings.MASTER_VOLUME, masterVolume.getValue());
                Settings.getPreferences().putFloat(Settings.MUSIC_VOLUME, musicVolume.getValue());
                Settings.getPreferences().putFloat(Settings.SFX_VOLUME, sfxVolume.getValue());
                Settings.getPreferences().putString(Settings.USERNAME, username.getText());
                Settings.getPreferences().putBoolean(Settings.SHOW_FPS, showFPS.isChecked());
                Settings.getPreferences().putBoolean(Settings.SHOW_PLACEMENT_GRID, showPlacementGrid.isChecked());

                Settings.getPreferences().flush();
                Settings.applySettings();
            }
        });
        horizontalGroup.add(apply).width(100).padLeft(20f).padRight(20);

        TextButton backButton = new TextButton("Back", Sprites.skin(), labelType);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                RetroTowerDefense game = RetroTowerDefense.getInstance(RetroTowerDefense.class);
                game.setScreen(new MainMenuScreen());
            }
        });
        horizontalGroup.add(backButton).width(100).padLeft(20f).padRight(20);

        table.add(horizontalGroup).bottom().pad(5f).width(100).expandX().row();

		add(Align.center, table);
    }

    @Override
    public void render(float delta) {
        game.batch().setColor(Color.WHITE);
        super.render(delta);
    }
    
    @Override
    public void hide() {
        super.hide();
    }
}
