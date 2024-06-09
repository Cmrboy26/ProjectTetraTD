package net.cmr.rtd.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import net.cmr.rtd.RetroTowerDefense;
import net.cmr.rtd.game.GameConnector;
import net.cmr.rtd.game.files.LevelFolder;
import net.cmr.rtd.game.files.QuestFile;
import net.cmr.rtd.game.files.QuestTask;
import net.cmr.rtd.game.files.WorldFolder;
import net.cmr.util.Audio;
import net.cmr.util.CMRGame;
import net.cmr.util.Log;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.SpriteType;

public class NewSelectionScreen extends ScreenAdapter {

    ExtendViewport viewport;
    Stage stage;
    final Batch batch;

    Table levelsTable;
    public NewSelectionScreen() {
        batch = CMRGame.getInstance().batch();
    }

    @Override
    public void show() {
        super.show();

        viewport = new ExtendViewport(640, 360);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        levelsTable = new Table();
        levelsTable.setFillParent(true);
        levelsTable.align(Align.center);
        stage.addActor(levelsTable);

        Table rightBottomUI = new Table();
        // TODO: Put the join online game button here

        Table leftBottomUI = new Table();
        leftBottomUI.setFillParent(true);
        leftBottomUI.align(Align.bottomLeft);
        stage.addActor(leftBottomUI);

        // Create the back button

        TextButton backButton = new TextButton("Back", Sprites.skin(), "small");
        Audio.addClickSFX(backButton);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                CMRGame.getInstance().setScreen(new MainMenuScreen());
            }
        });
        backButton.pad(0, 20, 0, 20);

        leftBottomUI.add(backButton).align(Align.bottomLeft).pad(10f);

        // Add worlds selection

        WorldFolder[] validWorlds = getValidWorlds();
        if (validWorlds.length == 0) {
            Label noWorldsLabel = new Label("No worlds found :/", Sprites.skin(), "small");
            noWorldsLabel.setAlignment(Align.center);
            leftBottomUI.add(noWorldsLabel).align(Align.left).pad(10f).colspan(1);
            return;
        }

        Label worldSelectionLabel = new Label("World: ", Sprites.skin(), "small");
        worldSelectionLabel.setAlignment(Align.center);
        leftBottomUI.add(worldSelectionLabel).align(Align.left).pad(10f).padLeft(20).colspan(1);

        SelectBoxStyle style = new SelectBoxStyle(Sprites.skin().get("small", SelectBoxStyle.class));

        float selectionBoxListSpacing = 5;
        style.scrollStyle.background = new NinePatchDrawable(Sprites.skin().get("box", NinePatch.class));
        style.scrollStyle.background.setTopHeight(selectionBoxListSpacing);
        style.scrollStyle.background.setBottomHeight(selectionBoxListSpacing);
        style.scrollStyle.background.setLeftWidth(selectionBoxListSpacing);
        style.scrollStyle.background.setRightWidth(selectionBoxListSpacing);

        float backgroundSelectSpacing = 10;
        style.background = new NinePatchDrawable(Sprites.skin().get("box", NinePatch.class));
        style.background.setLeftWidth(backgroundSelectSpacing);
        style.background.setRightWidth(backgroundSelectSpacing);
        SelectBox<WorldFolder> worldSelection = new SelectBox<>(style);
        worldSelection.setItems(validWorlds);
        worldSelection.setAlignment(Align.center);
        worldSelection.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                onWorldSelected(worldSelection.getSelected());
            }
        });
        leftBottomUI.add(worldSelection).align(Align.left).pad(10f).colspan(1);

        // Set the default selection and notify onWorldSelected
        worldSelection.setSelected(validWorlds[0]);
        onWorldSelected(validWorlds[0]);


    }

    @Override
    public void render(float delta) {
        super.render(delta);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        stage.act(delta);
        stage.draw();
        batch.end();
    }
    
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height, true);
    }

    public enum PlayType {
        SINGLEPLAYER {
            @Override
            public void startGame(QuestFile quest, int team) {
                GameConnector.startSingleplayerGame(quest, 0);
            }
        },
        HOST_ONLINE_GAME {
            @Override
            public void startGame(QuestFile quest, int team) {
                
            }
        };

        @Override
        public String toString() {
            if (this == SINGLEPLAYER) {
                return "Singleplayer";
            } else if (this == HOST_ONLINE_GAME) {
                return "Host Online Game";
            }
            return super.toString();
        }

        public abstract void startGame(QuestFile quest, int team);
    }

    public void onWorldSelected(final WorldFolder world) {
        Log.info("Selected world: " + world);
        levelsTable.clear();

        Table levelSelection = new Table();

        int buttonSize = 50;

        LevelFolder[] levels = world.readLevels();
        for (LevelFolder level : levels) {
            Table levelTable = new Table();
            levelTable.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    Audio.getInstance().playSFX(Audio.GameSFX.CLICK, 1f);
                    Log.info("Clicked level: " + level);
                    Dialog levelDialog = new Dialog("Level: "+level, Sprites.skin(), "small");
                    // TODO: Make a dialog that shows the information
                    // - show all quests TODO: STILL WORK ON THIS !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! SHOW WHEN THE QUESTS ARE COMPLETED
                    // - have a sidebar that shows tasks to be completed in the level (for story mode) DONE
                    // - have a button to start the level singleplayer OR host a multiplayer online game DONE


                    Label questTasksLabel = new Label("Tasks:", Sprites.skin(), "small");
                    questTasksLabel.setAlignment(Align.left);

                    Label questTasks = new Label("", Sprites.skin(), "small");
                    questTasks.setAlignment(Align.left);

                    Label questLabel = new Label("Quest", Sprites.skin(), "small");
                    questLabel.setAlignment(Align.center);

                    TextButton resumeGameButton = new TextButton("Resume", Sprites.skin(), "small");
                    Audio.addClickSFX(resumeGameButton);
                    resumeGameButton.pad(0, 20, 0, 20);

                    Label playTypeLabel = new Label("Play Type", Sprites.skin(), "small");
                    playTypeLabel.setAlignment(Align.center);

                    float labelTopPad = 3;

                    SelectBoxStyle style = new SelectBoxStyle(Sprites.skin().get("small", SelectBoxStyle.class));
                    style.background = new NinePatchDrawable(Sprites.skin().get("box", NinePatch.class));
                    style.scrollStyle.background = new NinePatchDrawable(Sprites.skin().get("box", NinePatch.class));
                    float selectionBoxListSpacing = 5;
                    style.scrollStyle.background.setTopHeight(selectionBoxListSpacing);
                    style.scrollStyle.background.setBottomHeight(selectionBoxListSpacing);
                    style.scrollStyle.background.setLeftWidth(selectionBoxListSpacing);
                    style.scrollStyle.background.setRightWidth(selectionBoxListSpacing);

                    SelectBox<QuestFile> questSelect = new SelectBox<>(style);
                    questSelect.getScrollPane().setScrollbarsVisible(false);
                    QuestFile[] questNames = getValidQuests(level);
                    questSelect.setItems(questNames);
                    questSelect.setAlignment(Align.center);
                    questSelect.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                            setTasksList(questTasks, resumeGameButton, questSelect.getSelected());
                        }
                    });
                    setTasksList(questTasks, resumeGameButton, questSelect.getSelected());

                    SelectBox<PlayType> playType = new SelectBox<>(style);
                    playType.getScrollPane().setScrollbarsVisible(false);
                    playType.setItems(PlayType.values());
                    playType.setAlignment(Align.center);

                    TextButton startButton = new TextButton("Start New", Sprites.skin(), "small");
                    Audio.addClickSFX(startButton);
                    startButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            super.clicked(event, x, y);
                            Log.info("Starting level: " + level);
                            Log.info("- Quest: " + questSelect.getSelected());
                            Log.info("- Play Type: " + playType.getSelected().name());
                            QuestFile quest = questSelect.getSelected();
                            quest.createSave();

                            levelDialog.hide();
                            playType.getSelected().startGame(quest, 0);
                        }
                    });
                    resumeGameButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            super.clicked(event, x, y);
                            if (resumeGameButton.isDisabled()) return;
                            Log.info("Resuming level: " + level);
                            levelDialog.hide();
                            playType.getSelected().startGame(new QuestFile(level, questSelect.getSelected()), 0);
                        }
                    });

                    startButton.pad(0, 20, 0, 20);

                    levelDialog.getContentTable().add(questTasksLabel).align(Align.left).colspan(3).row();
                    levelDialog.getContentTable().add(questTasks).align(Align.left).colspan(3).row();

                    levelDialog.getContentTable().add(questLabel).align(Align.center).colspan(1);
                    levelDialog.getContentTable().add(playTypeLabel).align(Align.center).colspan(1);
                    levelDialog.getContentTable().add(resumeGameButton).colspan(1).growX().row();

                    levelDialog.getContentTable().add(questSelect).align(Align.center).pad(10).padTop(labelTopPad).colspan(1);
                    levelDialog.getContentTable().add(playType).align(Align.center).pad(10).padTop(labelTopPad).colspan(1);
                    levelDialog.getContentTable().add(startButton).align(Align.center).pad(10).padTop(labelTopPad).growX().colspan(1);

                    levelDialog.pad(10);
                    levelDialog.padTop(30);
                    levelDialog.show(stage, Actions.alpha(1));
                    levelDialog.setPosition(Math.round((stage.getWidth() - levelDialog.getWidth()) / 2), Math.round((stage.getHeight() - levelDialog.getHeight()) / 2));
                    levelDialog.key(Input.Keys.ESCAPE, null);
                }
            });
            levelSelection.add(levelTable).align(Align.center).pad(10);

            Label name = new Label(level.toString(), Sprites.skin(), "small");
            name.setAlignment(Align.center);
            levelTable.add(name).align(Align.center).row();

            // TODO: If the level isn't unlocked yet, gray out the image
            // TODO: If the level is completed, make the image green or something
            Image slot = new Image(Sprites.sprite(SpriteType.WORLD_LEVEL));
            levelTable.add(slot).size(buttonSize, buttonSize / 2).align(Align.center).pad(5);
        }

        ScrollPane worldView = new ScrollPane(levelSelection, Sprites.skin());
        worldView.setScrollbarsVisible(false);
        levelsTable.add(worldView).align(Align.center).height(360).width(640).colspan(1);

    }


    public WorldFolder[] getValidWorlds() {
        return WorldFolder.listWorlds();
    }

    public QuestFile[] getValidQuests(LevelFolder level) {
        return level.readQuests();
    }

    public void setTasksList(Label taskListLabel, TextButton resumeGameButton, QuestFile file) {
        taskListLabel.setText(getTasksList(file));

        boolean saveFileExists = file.questFileExists();
        if (!saveFileExists) {
            resumeGameButton.setDisabled(true);
            resumeGameButton.setColor(Color.DARK_GRAY);
            resumeGameButton.getLabel().setColor(Color.GRAY);
        }
    }

    public String getTasksList(QuestFile file) {
        QuestTask[] tasks = file.getTasks();
        if (tasks == null) {
            return "- No tasks found! Have fun!";
        }
        StringBuilder builder = new StringBuilder();
        for (QuestTask task : tasks) {
            builder.append("- ");
            builder.append(task.toString()).append("\n");
        }
        return builder.toString();
    }

}
