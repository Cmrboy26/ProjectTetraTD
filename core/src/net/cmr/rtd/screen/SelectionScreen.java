package net.cmr.rtd.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.cmr.rtd.RetroTowerDefense;
import net.cmr.rtd.game.GameManager.GameManagerDetails;
import net.cmr.rtd.game.GameSave;
import net.cmr.rtd.game.LevelSave;
import net.cmr.rtd.waves.WavesData;
import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Sprites;

public class SelectionScreen extends AbstractScreenEX {
    
    Table details;
    Table playOptions;

    public SelectionScreen() {
        super(INITIALIZE_ALL);

		Table table = new Table();
		table.setFillParent(true);

        // right of the screen
        details = new Table();


        // left of the screen
        Table selection = new Table(Sprites.skin());

        Label title = new Label("Select a level", Sprites.skin(), "small");
        title.setAlignment(Align.center);
        selection.add(title).fillX().expandX().colspan(1).top().row();

        ButtonGroup<TextButton> group = new ButtonGroup<TextButton>();
        group.setMaxCheckCount(1);
        group.setMinCheckCount(0);
        // Load all available levels from the levels directory
        FileHandle[] levels = Gdx.files.external("retrotowerdefense/levels").list();
        for (FileHandle level : levels) {
            TextButton button = new TextButton(level.nameWithoutExtension(), Sprites.skin(), "toggle-small");
            group.add(button);
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (button.isChecked()) {
                        fillDetails(button);
                    } else {
                        details.clear();
                    }
                }
            });
            selection.add(button).pad(5f).fillX().expandX().row();
        }


        ScrollPane scrollPane = new ScrollPane(selection);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollbarsOnTop(false);
        scrollPane.setScrollBarPositions(false, true);

        table.add(scrollPane).fillX().expand();
        table.add(details).fillX().expand().row();
        
        TextButton back = new TextButton("Back", Sprites.skin(), "small");
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                RetroTowerDefense game = RetroTowerDefense.getInstance(RetroTowerDefense.class);
                game.setScreen(new MainMenuScreen());
            }
        });
        table.add(back).right().bottom().pad(5f).width(100).expandX().colspan(2);

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

    public void fillDetails(TextButton pressed) {
        String folderName = pressed.getText().toString();
        FileHandle level = Gdx.files.external("retrotowerdefense/levels/" + folderName + "/world.dat");
        FileHandle[] difficulties = Gdx.files.external("retrotowerdefense/levels/" + folderName + "/waves/").list();

        details.clear();
        
        // Add a title with the level name
        Label title = new Label(folderName, Sprites.skin(), "small");
        title.setAlignment(Align.center);
        details.add(title).fillX().expandX().colspan(2).row();

        // TODO: ADD A LABEL THAT SHOWS HOW MANY PLAYERS THE LEVEL SUPPORTS

        ButtonGroup<TextButton> group = new ButtonGroup<TextButton>();
        group.setMaxCheckCount(1);
        group.setMinCheckCount(0);
        for (final FileHandle difficulty : difficulties) {
            try {
                final WavesData data = WavesData.load(difficulty);
                String name = data.name;
                TextButton button = new TextButton(name, Sprites.skin(), "toggle-small");
                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        setPlayOptions(folderName, difficulty, data, button.isChecked());
                    }
                });
                group.add(button);
                details.add(button).pad(5f).fillX().expandX().colspan(2).row();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        playOptions = new Table();
        details.add(playOptions).fillX().expandX().colspan(2).row();
    }

    public void setPlayOptions(String folderName, FileHandle difficultyFile, WavesData data, boolean show) {

        String fn = minimizeString(folderName) + "-" + minimizeString(data.getName());

        playOptions.clear();
        if (!show) {
            return;
        }

        Label difficultyLabel = new Label("Difficulty: " + data.getDifficulty().name(), Sprites.skin(), "small");
        difficultyLabel.setAlignment(Align.center);
        playOptions.add(difficultyLabel).fillX().expandX().colspan(2).row();

        Label waveCount = new Label("Waves: " + data.size(), Sprites.skin(), "small");
        waveCount.setAlignment(Align.center);
        playOptions.add(waveCount).fillX().expandX().colspan(2).row();
        
        if (data.endlessMode) {
            Label endless = new Label("Endless Mode", Sprites.skin(), "small");
            endless.setAlignment(Align.center);
            playOptions.add(endless).fillX().expandX().colspan(2).row();
        }

        TextButton resume = new TextButton("Resume", Sprites.skin(), "small");
        resume.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                // TODO: PROMPT THE PLAYER TO SEE IF THEY WANT ONLINE OR SINGLEPLAYER

                try {
				    GameManagerDetails details = new GameManagerDetails();
                    GameSave save = new GameSave(fn);
				    RetroTowerDefense game = RetroTowerDefense.getInstance(RetroTowerDefense.class);
				    game.joinSingleplayerGame(details, save);
                } catch (Exception e) {
                    e.printStackTrace();
                    Dialog dialog = new Dialog("Error", Sprites.skin());
                    dialog.getTitleLabel().setAlignment(Align.center);
                    dialog.pad(20f);
                    dialog.padTop(50f);
                    Label text = new Label("Failed to load save file:\n"+e, Sprites.skin(), "small");
                    text.setFontScale(.4f);
                    text.setWidth(400);
                    text.setWrap(true);
                    dialog.getContentTable().add(text).width(400).fillX().expandX().row();
                    dialog.button("OK");

                    dialog.show(stages.get(Align.center));
                }
            }
        });
        playOptions.add(resume).pad(5f).fillX().expandX();

        // If there is no save file, disable the resume button
        GameSave save = new GameSave(fn);
        if (!save.getSaveFolder().exists()) {
            resume.setDisabled(true);
            resume.setColor(Color.DARK_GRAY);
            resume.getLabel().setColor(Color.GRAY);
        }

        TextButton newSave = new TextButton("New Game", Sprites.skin(), "small");
        newSave.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Runnable startGame = new Runnable() {
                    @Override
                    public void run() {
                        // TODO: PROMPT THE PLAYER TO SEE IF THEY WANT ONLINE OR SINGLEPLAYER
                        try {
                            GameManagerDetails details = new GameManagerDetails();
                            LevelSave levelSave = new LevelSave(folderName);
                            RetroTowerDefense game = RetroTowerDefense.getInstance(RetroTowerDefense.class);
                            game.joinSingleplayerGame(details, levelSave, fn, difficultyFile.nameWithoutExtension(), true);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Dialog dialog = new Dialog("Error", Sprites.skin());
                            dialog.getTitleLabel().setAlignment(Align.center);
                            dialog.pad(20f);
                            dialog.padTop(50f);
                            Label text = new Label("Failed to load save file:\n"+e, Sprites.skin(), "small");
                            text.setFontScale(.4f);
                            text.setWidth(400);
                            text.setWrap(true);
                            dialog.getContentTable().add(text).width(400).fillX().expandX().row();
                            dialog.button("OK");

                            dialog.show(stages.get(Align.center));
                        }
                    }
                };

                if (!resume.isDisabled()) {
                    Dialog dialog = new Dialog("WARNING", Sprites.skin()) {
                        @Override
                        protected void result(Object object) {
                            if ((Boolean) object == true) {
                                startGame.run();
                            }
                        }
                    };

                    dialog.getTitleLabel().setAlignment(Align.center);
                    dialog.pad(20f);
                    dialog.padTop(50f);
                    Label text = new Label("A save already exists here! Do you want to override it?", Sprites.skin(), "small");
                    text.setFontScale(.4f);
                    text.setWidth(400);
                    text.setWrap(true);
                    dialog.getContentTable().add(text).width(400).fillX().expandX().row();
                    dialog.button("OVERRIDE IT", true, Sprites.skin().get("small", TextButton.TextButtonStyle.class));
                    dialog.button("NO, CANCEL", false, Sprites.skin().get("small", TextButton.TextButtonStyle.class));
                    dialog.key(com.badlogic.gdx.Input.Keys.ENTER, true);
                    dialog.key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
                    dialog.key(com.badlogic.gdx.Input.Keys.Y, true);
                    dialog.key(com.badlogic.gdx.Input.Keys.N, false);
                    dialog.show(stages.get(Align.center));
                } else {
                    startGame.run();
                }
            }
        });
        playOptions.add(newSave).pad(5f).fillX().expandX().row();
    }

    public String getNameOfLevel(FileHandle level) {
        return level.nameWithoutExtension();
    }

    public String minimizeString(String folderName) {
        // Remove all whitespace, keep only alphanumeric characters, and make it lowercase
        return folderName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }
}
