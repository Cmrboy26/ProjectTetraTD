package net.cmr.rtd.screen;

import java.util.function.Function;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Sprites;

public class TeamSelectionScreen extends AbstractScreenEX {
    
    Function<Integer, Void> joinGameFunction;
    int availableTeams;
    int selectedTeam = -1;
    float countdown = 0.0f;

    public TeamSelectionScreen(Function<Integer, Void> joinGameFunction, int availableTeams) {
        super(INITIALIZE_ALL);
        this.joinGameFunction = joinGameFunction;
        this.availableTeams = availableTeams;
    }

    @Override
    public void show() {
        super.show();
        System.out.println(availableTeams);
        if (availableTeams == 1) {
            joinGameFunction.apply(0);
            return;
        }

        Table table = new Table();
		table.setFillParent(true);

        Label label = new Label("Select a team", Sprites.skin(), "default");
        label.setAlignment(Align.center);
        table.add(label).padTop(20.0f).padBottom(20.0f).colspan(availableTeams).expandX().fillX().row();

        ButtonGroup<TextButton> buttonGroup = new ButtonGroup<TextButton>();
        buttonGroup.setMaxCheckCount(1);
        buttonGroup.setMinCheckCount(0);
        for (int i = 0; i < availableTeams; i++) {
            int team = i;
            TextButton button = new TextButton("Team " + (team + 1), Sprites.skin(), "small");
            buttonGroup.add(button);
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (team != -1 && selectedTeam == -1) {
                        System.out.println(team);
                        table.row();
                        Label label = new Label("Joining game...", Sprites.skin(), "small");
                        label.setAlignment(Align.center);
                        table.add(label).padTop(20.0f).padBottom(20.0f).colspan(availableTeams).expandX().fillX().row();
                        selectedTeam = team;
                    }
                }
            });
            int sidePad = 0;
            table.add(button).pad(sidePad).padTop(20.0f).padBottom(20.0f).space(10.0f).growX();
        }

        /*TextButton joinButton = new TextButton("Join", Sprites.skin(), "small");
        joinButton.addListener(event -> {
            int team = buttonGroup.getCheckedIndex();
            if (team != -1) {
                System.out.println(team);
                joinGameFunction.apply(team);
            }
            return true;
        });
        table.row();
        table.add(joinButton).padTop(20.0f).padBottom(20.0f).space(10.0f).colspan(availableTeams);*/

        add(Align.center, table);
    }

    @Override
    public void render(float delta) {
        if (selectedTeam != -1) {
            countdown += delta;
        }
        if (countdown > .1f) {
            joinGameFunction.apply(selectedTeam);
        }
        super.render(delta);
    }

}
