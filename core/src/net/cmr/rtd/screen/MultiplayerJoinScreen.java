package net.cmr.rtd.screen;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.cmr.rtd.ProjectTetraTD;
import net.cmr.rtd.game.GameConnector;
import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Audio.GameMusic;
import net.cmr.util.Settings;
import net.cmr.util.Sprites;

public class MultiplayerJoinScreen extends AbstractScreenEX {
    
    public MultiplayerJoinScreen() {
        super(INITIALIZE_ALL);
    }

    @Override
    public void show() {
        super.show();
        Table table = new Table();
        table.setFillParent(true);
        add(Align.center, table);

        Label title = new Label("Join Multiplayer Game", Sprites.skin(), "default");
        title.setAlignment(Align.center);
        table.add(title).padTop(20.0f).padBottom(20.0f).colspan(2).expandX().growX().row();

        TextField ipField = new TextField(Settings.getPreferences().getString(Settings.JOIN_IP), Sprites.skin(), "small");
        ipField.setMessageText("localhost");
        ipField.setAlignment(Align.center);
        Label label = new Label("IP Address:", Sprites.skin(), "small");
        label.setAlignment(Align.center);
        table.add(label).expandX().padRight(10.0f).colspan(1);
        table.add(ipField).expandX().fillX().pad(10).colspan(1).row();

        TextField portField = new TextField(Settings.getPreferences().getInteger(Settings.JOIN_PORT)+"", Sprites.skin(), "small");
        portField.setMessageText("11265");
        portField.setMaxLength(6);
        portField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        portField.setAlignment(Align.center);
        label = new Label("Port:", Sprites.skin(), "small");
        label.setAlignment(Align.center);
        table.add(label).expandX().padRight(10.0f).colspan(1);
        table.add(portField).expandX().fillX().pad(10).colspan(1).row();

        TextButton back = new TextButton("Back", Sprites.skin(), "small");
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ProjectTetraTD game = ProjectTetraTD.getInstance(ProjectTetraTD.class);
                game.setScreen(new SelectionScreen());
            }
        });
        table.add(back).left().bottom().pad(5f).width(100).expandX().colspan(1);
        TextButton join = new TextButton("Join", Sprites.skin(), "small");
        join.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (join.isDisabled()) {
                    return;
                }
                join.setDisabled(true);

                ProjectTetraTD game = ProjectTetraTD.getInstance(ProjectTetraTD.class);
                final String ip = ipField.getText();
                final int port = portField.getText().isEmpty() ? 11265 : Integer.parseInt(portField.getText());

                Settings.getPreferences().putString(Settings.JOIN_IP, ip);
                Settings.getPreferences().putInteger(Settings.JOIN_PORT, port);
                Settings.getPreferences().flush();

                GameConnector.joinMultiplayerGame(ip, port);
            }
        });
        table.add(join).right().bottom().pad(5f).width(100).expandX().colspan(1);
    }

    @Override
    public GameMusic getScreenMusic() {
        return GameMusic.menuMusic();
    }

}
