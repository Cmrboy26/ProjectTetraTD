package net.cmr.rtd.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import net.cmr.rtd.ProjectTetraTD;
import net.cmr.rtd.ProjectTetraTD.LevelValueKey;
import net.cmr.rtd.game.GameConnector;
import net.cmr.rtd.game.files.LevelFolder;
import net.cmr.rtd.game.files.QuestFile;
import net.cmr.rtd.game.files.QuestTask;
import net.cmr.rtd.game.files.WorldFolder;
import net.cmr.rtd.game.world.store.ShopManager;
import net.cmr.rtd.shader.ShaderManager.CustomShader;
import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Audio;
import net.cmr.util.Audio.GameMusic;
import net.cmr.util.CMRGame;
import net.cmr.util.Log;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.AnimationType;
import net.cmr.util.Sprites.SpriteType;

public class SelectionScreen extends AbstractScreenEX {

    ExtendViewport viewport;
    Stage stage;
    final Batch batch;

    Table levelsTable;
    public SelectionScreen() {
        batch = CMRGame.getInstance().batch();
    }

    @Override
    public void show() {
        super.show();

        viewport = new ExtendViewport(640, 360);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        SpriteType backgroundSprite = SpriteType.WORLDS_BACKGROUND; 

        Image background = new Image(Sprites.drawable(backgroundSprite)) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
				ProjectTetraTD.getInstance(ProjectTetraTD.class).shaderManager.enableShader(batch, CustomShader.BACKGROUND);
				batch.setColor(Color.BLUE);
				super.draw(batch, parentAlpha);
				batch.setColor(Color.WHITE);
				ProjectTetraTD.getInstance(ProjectTetraTD.class).shaderManager.disableShader(batch);
            }
        };
        background.setFillParent(true);
        stage.addActor(background);

        levelsTable = new Table();
        levelsTable.setFillParent(true);
        levelsTable.align(Align.center);
        stage.addActor(levelsTable);

        Table rightBottomUI = new Table();
        rightBottomUI.setFillParent(true);
        rightBottomUI.align(Align.bottomRight);
        stage.addActor(rightBottomUI);

        TextButton joinOnlineGameButton = new TextButton("Join Online", Sprites.skin(), "small");
        Audio.addClickSFX(joinOnlineGameButton);
        joinOnlineGameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                CMRGame.getInstance().setScreen(new MultiplayerJoinScreen());
            }
        });
        joinOnlineGameButton.pad(0, 20, 0, 20);
        rightBottomUI.add(joinOnlineGameButton).align(Align.bottomRight).pad(10f);

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

        String preferredWorld = ProjectTetraTD.getLastPlayedWorld();
        WorldFolder selectedWorldFolder = validWorlds[0];
        for (WorldFolder world : validWorlds) {
            String worldName = world.getFolder().name();
            if (worldName.equals(preferredWorld)) {
                selectedWorldFolder = world;
                break;
            }
        }

        // Set the default selection and notify onWorldSelected
        worldSelection.setSelected(selectedWorldFolder);
        onWorldSelected(selectedWorldFolder);
        Audio.addClickSFX(worldSelection);

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
                GameConnector.startSingleplayerGame(quest, team);
            }
        },
        HOST_ONLINE_GAME {
            @Override
            public void startGame(QuestFile quest, int team) {
                HostScreen screen = new HostScreen(null, quest);
                CMRGame.getInstance().setScreen(screen);
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
        ProjectTetraTD.setLastPlayedWorld(world.getFolder().name());
        levelsTable.clear();

        Table levelSelection = new Table();
        
        //levelSelection.setBackground(Sprites.drawable(SpriteType.BACKGROUND));

        int buttonSize = 65;

        LevelFolder[] levels = world.readLevels();
        int levelIndex = 0;
        for (LevelFolder level : levels) {
            Table levelTable = new Table();
            boolean locked = level.isLevelLocked();
            levelTable.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    if (locked) {
                        Audio.getInstance().playSFX(Audio.GameSFX.WARNING, 1f);

                        Dialog dialog = new Dialog("Level Locked", Sprites.skin(), "small");
                        dialog.setMovable(false);
                        dialog.getTitleLabel().setAlignment(Align.center);
                        dialog.pad(20);
                        dialog.padTop(40);

                        Label text1 = new Label("This level is locked. Complete the requirements to unlock it:", Sprites.skin(), "small");
                        dialog.text(text1);
                        dialog.getContentTable().row();

                        for (String requirement : level.getUnlockRequirements()) {
                            Log.info("Requirement: " + requirement);
                            String[] parts = requirement.split("/");
                            if (parts.length != 4) {
                                Log.warning("Invalid unlock requirement: " + requirement);
                                continue;
                            }

                            WorldFolder world = new WorldFolder(parts[0]);
                            LevelFolder level = new LevelFolder(world, parts[1]);
                            QuestFile quest = new QuestFile(level, parts[2]);
                            QuestTask task = QuestTask.getTask(quest, Long.parseLong(parts[3]));
                            if (task == null) {
                                Log.warning("Invalid task ID: " + parts[3] + " in " + requirement);
                                continue;
                            }
                            boolean completed = false;
                            Long[] completedTasks = ProjectTetraTD.getStoredLevelValue(quest, LevelValueKey.COMPLETED_TASKS, Long[].class);
                            if (completedTasks != null) {
                                for (Long completedTask : completedTasks) {
                                    if (completedTask == task.id) {
                                        completed = true;
                                        break;
                                    }
                                }
                            }
                            if (completed) {
                                continue;
                            }
                            String taskDescription = task.getReadableTaskDescription();
                            String location = world.getDisplayName() + " / " + level.getDisplayName() + " / " + quest.getDisplayName();
                            Label text = new Label("- " + taskDescription + " in \""+location+"\"", Sprites.skin(), "small");
                            dialog.getContentTable().add(text).align(Align.left).pad(5).row();
                        }

                        TextButton close = new TextButton("Close", Sprites.skin(), "small");
                        close.pad(0, 20, 0, 20);
                        dialog.button(close, false);

                        dialog.key(Input.Keys.ESCAPE, false);
                        dialog.setOrigin(Align.topLeft);
                        dialog.setScale(.75f);
                        dialog.pack();
                        dialog.setPosition(10, 360 - 10, Align.topLeft);
                        dialog.show(stage, Actions.alpha(1));

                        return;
                    }
                    Audio.getInstance().playSFX(Audio.GameSFX.CLICK, 1f);
                    Log.info("Clicked level: " + level);
                    Dialog levelDialog = new Dialog(level.toString(), Sprites.skin(), "small");
                    levelDialog.getTitleLabel().setAlignment(Align.center);

                    Label questTasksLabel = new Label("Tasks:", Sprites.skin(), "small");
                    questTasksLabel.setAlignment(Align.left);

                    Label questTasks = new Label("", Sprites.skin(), "small");
                    questTasks.setAlignment(Align.left);

                    Label questLabel = new Label("Quest:", Sprites.skin(), "small");
                    questLabel.setAlignment(Align.center);

                    TextButton resumeGameButton = new TextButton("Resume", Sprites.skin(), "small");
                    Audio.addClickSFX(resumeGameButton);
                    resumeGameButton.pad(0, 20, 0, 20);

                    Label playTypeLabel = new Label("Play Type:", Sprites.skin(), "small");
                    playTypeLabel.setAlignment(Align.center);

                    Label highScoreLabel = new Label("", Sprites.skin(), "small");
                    highScoreLabel.setAlignment(Align.center);                    

                    Image trophy = new Image(Sprites.sprite(SpriteType.TROPHY));

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
                        public void changed(ChangeEvent event, Actor actor) {
                            setTasksList(levelDialog, questTasks, resumeGameButton, questSelect.getSelected(), highScoreLabel, trophy);
                        }
                    });
                    setTasksList(levelDialog, questTasks, resumeGameButton, questSelect.getSelected(), highScoreLabel, trophy);
                    Audio.addClickSFX(questSelect);

                    SelectBox<PlayType> playType = new SelectBox<>(style);
                    playType.getScrollPane().setScrollbarsVisible(false);
                    playType.setItems(PlayType.values());
                    playType.setAlignment(Align.center);
                    Audio.addClickSFX(playType);

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
                            playType.getSelected().startGame(quest, -1);
                        }
                    });
                    resumeGameButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            super.clicked(event, x, y);
                            if (resumeGameButton.isDisabled()) return;
                            Log.info("Resuming level: " + level);
                            levelDialog.hide();
                            playType.getSelected().startGame(new QuestFile(level, questSelect.getSelected()), -1);
                        }
                    });

                    startButton.pad(0, 20, 0, 20);

                    levelDialog.getContentTable().add(questTasksLabel).align(Align.left).colspan(2);
                    levelDialog.getContentTable().add(highScoreLabel).align(Align.topRight).colspan(1).row();
                    levelDialog.getContentTable().add(questTasks).align(Align.left).colspan(2);
                    levelDialog.getContentTable().add(trophy).size(64).align(Align.right).colspan(1).row();

                    levelDialog.getContentTable().add(questLabel).align(Align.center).colspan(1);
                    levelDialog.getContentTable().add(playTypeLabel).align(Align.center).colspan(1);
                    levelDialog.getContentTable().add(resumeGameButton).colspan(1).growX().row();

                    levelDialog.getContentTable().add(questSelect).align(Align.center).pad(10).padTop(labelTopPad).colspan(1);
                    levelDialog.getContentTable().add(playType).align(Align.center).pad(10).padTop(labelTopPad).colspan(1);
                    levelDialog.getContentTable().add(startButton).align(Align.center).pad(10).padTop(labelTopPad).growX().colspan(1);

                    TextButton closeButton = new TextButton("Close", Sprites.skin(), "small");
                    closeButton.pad(0, 20, 0, 20);
                    Audio.addClickSFX(closeButton);
                    levelDialog.button(closeButton, false);

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

            //Image slot = new Image(Sprites.sprite(SpriteType.WORLD_LEVEL));
            Image slot = new AnimatedImage(AnimationType.WORLD_LEVEL_ANIMATED, (levels.length - levelIndex) * 0.5f);
            boolean areAllTasksCompleted = true;
            boolean noTasksPresent = true;
            for (QuestFile quest : getValidQuests(level)) {
                if (!quest.hasNoTasks()) {
                    noTasksPresent = false;
                }
                if (!quest.areAllTasksCompleted()) {
                    areAllTasksCompleted = false;
                    break;
                }
            }
            if (areAllTasksCompleted && !noTasksPresent) {
                slot.setColor(Color.GREEN);
            } else {
                slot.setColor(Color.WHITE);
            }
            if (locked) {
                slot.setColor(Color.GRAY);
            }
            float ratio = 1.0f * slot.getHeight() / slot.getWidth();
            levelTable.add(slot).size(buttonSize, buttonSize*ratio).align(Align.center).pad(5);
            
            levelIndex++;
        }

        ScrollPaneStyle scrollStyle = Sprites.skin().get("default", ScrollPaneStyle.class);
        scrollStyle.background = null;

        ScrollPane worldView = new ScrollPane(levelSelection, scrollStyle);
        worldView.setScrollbarsVisible(false);
        levelsTable.add(worldView).align(Align.center).height(360).width(640).colspan(1);
    }

    public WorldFolder[] getValidWorlds() {
        return WorldFolder.listWorlds();
    }

    public QuestFile[] getValidQuests(LevelFolder level) {
        return level.readQuests();
    }

    public void setTasksList(Dialog levelDialog, Label taskListLabel, TextButton resumeGameButton, QuestFile file, Label highScoreLabel, Image trophy) {
        taskListLabel.setText(getTasksList(file));
        levelDialog.getTitleLabel().setText(file.getLevel().getDisplayName() + " [" + file.getDisplayName() + " | " + file.getDifficulty().name() + "]");

        boolean saveFileExists = file.questFileExists();
        if (!saveFileExists) {
            resumeGameButton.setDisabled(true);
            resumeGameButton.setColor(Color.DARK_GRAY);
            resumeGameButton.getLabel().setColor(Color.GRAY);
        } else {
            resumeGameButton.setDisabled(false);
            resumeGameButton.setColor(Color.WHITE);
            resumeGameButton.getLabel().setColor(Color.WHITE);
        }

        Long highScore = ProjectTetraTD.getStoredLevelValue(file, LevelValueKey.HIGHSCORE, Long.class);
        if (highScore != null) {
            String text = "High Score: " + ShopManager.costToString(highScore).substring(1);
            text += "\n";
            text += "Farthest Wave: " + ProjectTetraTD.getStoredLevelValue(file, LevelValueKey.FARTHEST_WAVE, Long.class);
            highScoreLabel.setText(text);
        } else {
            highScoreLabel.setText("");
        }

        if (file.areAllTasksCompleted() && !file.hasNoTasks()) {
            trophy.setVisible(true);
        } else {
            trophy.setVisible(false);
        }

        levelDialog.pack();
    }

    public String getTasksList(QuestFile file) {
        if (file == null) {
            return "No quests found!";
        }
        QuestTask[] tasks = file.getTasks();
        if (tasks == null || tasks.length == 0) {
            return "- No tasks found! Have fun!";
        }
        StringBuilder builder = new StringBuilder();

        Long[] completedTasks = ProjectTetraTD.getStoredLevelValue(file, LevelValueKey.COMPLETED_TASKS, Long[].class);
        if (completedTasks == null) {
            completedTasks = new Long[0];
        }

        for (QuestTask task : tasks) {
            builder.append("- ");
            builder.append(task.toString());
            for (Long completedTask : completedTasks) {
                if (completedTask == task.id) {
                    builder.append(" (Completed)");
                    break;
                }
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    @Override
    public GameMusic getScreenMusic() {
        return GameMusic.menuMusic();
    }

}
