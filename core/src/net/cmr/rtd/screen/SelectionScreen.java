package net.cmr.rtd.screen;

import java.util.function.Consumer;
import java.util.function.Function;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.cmr.rtd.RetroTowerDefense;
import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.GameManager.GameManagerDetails;
import net.cmr.rtd.game.GameSave;
import net.cmr.rtd.game.LevelSave;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.TeamData;
import net.cmr.rtd.game.world.TeamData.NullTeamException;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.game.world.store.ShopManager;
import net.cmr.rtd.waves.WavesData;
import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Audio;
import net.cmr.util.Audio.GameSFX;
import net.cmr.util.Sprites;
import net.cmr.util.Stages;

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
            Audio.addClickSFX(button);
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
        scrollPane.setScrollbarsVisible(true);
        scrollPane.setScrollingDisabled(true, false);

        table.add(scrollPane).fillX().expand();
        table.add(details).fillX().expand().row();
        

        TextButton back = new TextButton("Back", Sprites.skin(), "small");
        Audio.addClickSFX(back);
        back.pad(0, 15f, 0, 15f);
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                RetroTowerDefense game = RetroTowerDefense.getInstance(RetroTowerDefense.class);
                game.setScreen(new MainMenuScreen());
            }
        });
        table.add(back).left().bottom().padBottom(10f).width(120).expandX().colspan(1);

        TextButton online = new TextButton("Join Online", Sprites.skin(), "small");
        online.pad(0, 15f, 0, 15f);
        Audio.addClickSFX(online);
        online.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                RetroTowerDefense game = RetroTowerDefense.getInstance(RetroTowerDefense.class);
                game.setScreen(new MultiplayerJoinScreen());
            }
        });
        table.add(online).right().bottom().padBottom(10f).width(120).expandX().colspan(1);

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

        FileHandle worldFile = Gdx.files.external("retrotowerdefense/levels/" + folderName + "/world.dat");
        World world;
        try {
            world = (World) GameObject.deserializeGameObject(worldFile.readBytes());
        } catch(Exception e ) {
            Label label = new Label("Invalid Level: Failed to load world", Sprites.skin(), "small");
            label.setAlignment(Align.center);
            details.add(label).fillX().expandX().colspan(2).row();
            e.printStackTrace();
            return;
        }
        int teamCounter = 0;
        for (int i = 0; i < GameManager.MAX_TEAMS; i++) {
            try {
                TeamData team = new TeamData(world, i);
                // If this succeeded, then this team exists in the world.
                teamCounter++;
            } catch (NullTeamException e) {
                // This team does not exist in the world.
            }
        }
        final int teamCount = teamCounter;
        if (teamCount == 0) {
            Label noTeams = new Label("Invalid Level: No Teams Found", Sprites.skin(), "small");
            noTeams.setAlignment(Align.center);
            details.add(noTeams).fillX().expandX().colspan(2).row();
        } else if (teamCount == 1) {
            Label oneTeam = new Label("Solo/Co-op", Sprites.skin(), "small");
            oneTeam.setAlignment(Align.center);
            details.add(oneTeam).fillX().expandX().colspan(2).row();
        } else {
            Label multipleTeams = new Label("Competitive: "+teamCount+" Teams", Sprites.skin(), "small");
            multipleTeams.setAlignment(Align.center);
            details.add(multipleTeams).fillX().expandX().colspan(2).row();
        }

        ButtonGroup<TextButton> group = new ButtonGroup<TextButton>();
        group.setMaxCheckCount(1);
        group.setMinCheckCount(0);
        for (final FileHandle difficulty : difficulties) {
            // Create buttons for each type of difficulty/wave types
            try {
                final WavesData data = WavesData.load(difficulty);
                String name = data.name;
                String fn = minimizeString(folderName) + "-" + minimizeString(data.getName());
                if (RetroTowerDefense.isLevelCleared(fn)) {
                    name += " (Clear)";
                }
                TextButton button = new TextButton(name, Sprites.skin(), "toggle-small");
                Audio.addClickSFX(button);
                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        setPlayOptions(folderName, difficulty, data, button.isChecked(), teamCount);
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

    public void setPlayOptions(String folderName, FileHandle difficultyFile, WavesData data, boolean show, final int teams) {

        String fn = minimizeString(folderName) + "-" + minimizeString(data.getName());
        LevelSave lsave = new LevelSave(folderName);

        playOptions.clear();
        if (!show) {
            return;
        }

        Label difficultyLabel = new Label("Difficulty: " + data.getDifficulty().name(), Sprites.skin(), "small");
        difficultyLabel.setAlignment(Align.center);
        playOptions.add(difficultyLabel).fillX().expandX().colspan(2).row();

        if (data.endlessMode) {
            Label endless = new Label("Endless Mode", Sprites.skin(), "small");
            endless.setAlignment(Align.center);
            playOptions.add(endless).fillX().expandX().colspan(2).row();

            long highscore = RetroTowerDefense.getHighscore(fn);
            long farthestWave = RetroTowerDefense.getFarthestWave(fn);
            if (highscore > 0) {
                Label highscoreLabel = new Label("High Score: " + ShopManager.costToString(highscore).substring(1) + ", Farthest Wave: "+farthestWave, Sprites.skin(), "small");
                highscoreLabel.setAlignment(Align.center);
                playOptions.add(highscoreLabel).fillX().expandX().colspan(2).row();
            }
        } else {   
            Label waveCount = new Label("Waves: " + data.size(), Sprites.skin(), "small");
            waveCount.setAlignment(Align.center);
            playOptions.add(waveCount).fillX().expandX().colspan(2).row();
        }

        TextButton resume = new TextButton("Resume", Sprites.skin(), "small");
        Audio.addClickSFX(resume);
        resume.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showPlayTypeDialog((Boolean online) -> {
                    Function<Integer, Void> joinGameFunction = new Function<Integer, Void>() {
                        @Override
                        public Void apply(Integer team) {
                            try {
                                GameManagerDetails details = new GameManagerDetails();
                                GameSave save = new GameSave(fn);
                                RetroTowerDefense game = RetroTowerDefense.getInstance(RetroTowerDefense.class);
                                if (online) {
                                    game.hostOnlineGame(details, save, lsave, teams);
                                } else {
                                    game.joinSingleplayerGame(details, save, lsave, team);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                // Set screen to this screen
                                RetroTowerDefense game = RetroTowerDefense.getInstance(RetroTowerDefense.class);
                                game.setScreen(SelectionScreen.this);
                                
                                displayErrorDialog(e, stages);
                            }
                            return null;
                        }
                    };
                    if (!online) {
                        game.setScreen(new TeamSelectionScreen(joinGameFunction, teams));
                    } else {
                        joinGameFunction.apply(0);
                    }
                });
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
        Audio.addClickSFX(newSave);
        newSave.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Runnable startGame = new Runnable() {
                    @Override
                    public void run() {
                        showPlayTypeDialog((Boolean online) -> {
                            Function<Integer, Void> joinGameFunction = new Function<Integer, Void>() {
                                @Override
                                public Void apply(Integer team) {
                                    try {
                                        GameManagerDetails details = new GameManagerDetails();
                                        LevelSave levelSave = new LevelSave(folderName);
                                        RetroTowerDefense game = RetroTowerDefense.getInstance(RetroTowerDefense.class);
                                        if (online) {
                                            game.hostOnlineGame(details, levelSave, fn, difficultyFile.nameWithoutExtension(), true, teams);
                                        } else {
                                            game.joinSingleplayerGame(details, levelSave, fn, difficultyFile.nameWithoutExtension(), true, team);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        displayErrorDialog(e, stages);
                                    }
                            
                                    return null;
                                }
                            };
                            if (!online) {
                                game.setScreen(new TeamSelectionScreen(joinGameFunction, teams));
                            } else {
                                joinGameFunction.apply(0);
                            }
                        });
                    }
                };

                if (!resume.isDisabled()) {
                    Dialog dialog = new Dialog("WARNING", Sprites.skin()) {
                        @Override
                        protected void result(Object object) {
                            Audio.getInstance().playSFX(GameSFX.CLICK, 1f);
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
                    TextButton cancel = new TextButton("NO, CANCEL", Sprites.skin(), "small");
                    cancel.pad(0, 10, 0, 10);
                    dialog.button(cancel, false);
                    TextButton confirm = new TextButton("OVERRIDE IT", Sprites.skin(), "small");
                    confirm.pad(0, 10, 0, 10);
                    dialog.button(confirm, true);
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

    // Consumer will return true if online, false if singleplayer/local
    public void showPlayTypeDialog(Consumer<Boolean> chooseOnlineCallback) {
        Dialog dialog = new Dialog("Choose Game Type", Sprites.skin()) {
            @Override
            protected void result(Object object) {
                if (object == null) {
                    return;
                }
                Audio.getInstance().playSFX(GameSFX.CLICK, 1f);
                chooseOnlineCallback.accept((Boolean) object);
            }
        };

        dialog.getTitleLabel().setAlignment(Align.center);
        dialog.pad(20f);
        dialog.padTop(50f);
        Label text = new Label("Select Play Type:", Sprites.skin(), "small");
        text.setFontScale(.4f);
        text.setWidth(400);
        text.setAlignment(Align.center);
        text.setWrap(true);
        dialog.getContentTable().add(text).width(400).fillX().expandX().row();
        TextButton cancel = new TextButton("Cancel", Sprites.skin(), "small");
        cancel.pad(0, 10, 0, 10);
        TextButton online = new TextButton("Online", Sprites.skin(), "small");
        online.pad(0, 10, 0, 10);
        TextButton singleplayer = new TextButton("Singleplayer", Sprites.skin(), "small");
        singleplayer.pad(0, 10, 0, 10);
        dialog.button(cancel, null);
        dialog.button(online, true);
        dialog.button(singleplayer, false);
        dialog.key(com.badlogic.gdx.Input.Keys.ENTER, true);
        dialog.key(com.badlogic.gdx.Input.Keys.ESCAPE, null);
        dialog.key(com.badlogic.gdx.Input.Keys.O, true);
        dialog.key(com.badlogic.gdx.Input.Keys.S, false);
        dialog.show(stages.get(Align.center));
    }

    public static void displayErrorDialog(Exception e, Stages stages) {
        Dialog dialog = new Dialog("Error", Sprites.skin());
        dialog.getTitleLabel().setAlignment(Align.center);
        dialog.pad(20f);
        dialog.padTop(50f);
        //Label text = new Label("Failed to load save file:\n"+e, Sprites.skin(), "small");
        Label text = new Label("An error occured performing that action:\n"+e.getMessage(), Sprites.skin(), "small");
        text.setFontScale(.4f);
        text.setWidth(400);
        text.setWrap(true);
        dialog.getContentTable().add(text).width(400).fillX().expandX().row();
        dialog.button("OK");

        dialog.show(stages.get(Align.center));
    }
}
