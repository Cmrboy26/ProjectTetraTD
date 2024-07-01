package net.cmr.rtd.screen;

import java.util.function.Consumer;
import java.util.function.Function;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.cmr.rtd.RetroTowerDefense;
import net.cmr.rtd.game.GameConnector;
import net.cmr.rtd.game.packets.GameInfoPacket;
import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Settings;
import net.cmr.util.Sprites;

public class MultiplayerJoinScreen extends AbstractScreenEX {
    
    public MultiplayerJoinScreen() {
        super(INITIALIZE_ALL);

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
                RetroTowerDefense game = RetroTowerDefense.getInstance(RetroTowerDefense.class);
                game.setScreen(new NewSelectionScreen());
            }
        });
        table.add(back).left().bottom().pad(5f).width(100).expandX().colspan(1);
        TextButton join = new TextButton("Join", Sprites.skin(), "small");
        join.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                RetroTowerDefense game = RetroTowerDefense.getInstance(RetroTowerDefense.class);
                final String ip = ipField.getText();
                final int port = portField.getText().isEmpty() ? 11265 : Integer.parseInt(portField.getText());

                Settings.getPreferences().putString(Settings.JOIN_IP, ip);
                Settings.getPreferences().putInteger(Settings.JOIN_PORT, port);
                Settings.getPreferences().flush();

                GameConnector.joinMultiplayerGame(ip, port);

                /*Function<Integer, Void> joinGameFunction = new Function<Integer, Void>() {
                    @Override
                    public Void apply(Integer team) {
                        try {
                            RetroTowerDefense game = RetroTowerDefense.getInstance(RetroTowerDefense.class);
                            game.joinOnlineGame(ip, port, team);
                        } catch (Exception e) {
                            AbstractScreenEX screen = new SelectionScreen();
                            game.setScreen(screen);
                            SelectionScreen.displayErrorDialog(e, screen.stages);
                        }
                        return null;
                    }
                };
                Consumer<GameInfoPacket> gameInfoCallback = (gameInfo) -> {
                    game.setScreen(new TeamSelectionScreen(joinGameFunction, gameInfo.teams));
                };

                try {
                    game.getOnlineGameData(ip, port, gameInfoCallback);
                } catch (Exception e) {
                    System.out.println("Could not fetch game data.");
                    AbstractScreenEX screen = new SelectionScreen();
                    game.setScreen(screen);
                    SelectionScreen.displayErrorDialog(e, screen.stages);
                    e.printStackTrace();
                }*/
            }
        });
        table.add(join).right().bottom().pad(5f).width(100).expandX().colspan(1);
    }

}
