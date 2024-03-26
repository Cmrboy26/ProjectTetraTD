package net.cmr.rtd.screen;

import java.util.function.Function;

import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Sprites;

public class TeamSelectionScreen extends AbstractScreenEX {
    
    Function<Integer, Void> joinGameFunction;

    public TeamSelectionScreen(Function<Integer, Void> joinGameFunction, int availableTeams) {
        super(INITIALIZE_ALL);
        this.joinGameFunction = joinGameFunction;

        if (availableTeams == 1) {
            joinGameFunction.apply(0);
            return;
        }

        Table table = new Table();
		table.setFillParent(true);

        Label label = new Label("Select a team", Sprites.skin(), "default");
        label.setAlignment(Align.center);
        table.add(label).padTop(20.0f).padBottom(20.0f).expandX().fillX();

        ButtonGroup<TextButton> buttonGroup = new ButtonGroup<TextButton>();
        buttonGroup.setMaxCheckCount(1);
        buttonGroup.setMinCheckCount(0);
        for (int i = 0; i < availableTeams; i++) {
            int team = i;
            TextButton button = new TextButton("Team " + (team + 1), Sprites.skin(), "small");
            buttonGroup.add(button);
            int sidePad = 100;
            table.add(button).pad(sidePad).padTop(20.0f).padBottom(20.0f).space(10.0f).fillX();
        }

        TextButton joinButton = new TextButton("Join", Sprites.skin(), "small");
        joinButton.addListener(event -> {
            int team = buttonGroup.getCheckedIndex();
            if (team != -1) {
                joinGameFunction.apply(team);
            }
            return true;
        });

        add(Align.center, table);
    }

}
