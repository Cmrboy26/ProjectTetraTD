package net.cmr.rtd.screen;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserIntent;
import net.cmr.rtd.game.GameManager;
import net.cmr.rtd.game.world.GameObject;
import net.cmr.rtd.game.world.World;
import net.cmr.rtd.game.world.tile.StructureTileData;
import net.cmr.rtd.game.world.tile.StartTileData;
import net.cmr.rtd.game.world.tile.TeamTileData;
import net.cmr.rtd.game.world.tile.Tile;
import net.cmr.rtd.game.world.tile.Tile.TileType;
import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Files;
import net.cmr.util.Log;
import net.cmr.util.Sprites;

/**
 * The screen for the level editor.
 * The world is loaded from a file, and the user can create, edit, and finally export the level.
 * The world will be exported as a world.dat file that can be loaded by GameScreen.
 */
public class EditorScreen extends AbstractScreenEX {

    float speed = Tile.SIZE * 4;
    float cameraX = 0, cameraY = 0;
    ExtendViewport viewport;
    ShapeRenderer shapeRenderer;

    String saveName = "";

    boolean entered;
    int selectedTile = 2;
    int teamNumber = 0;
    Window window;
    TextButton wall, floor, clear, path, start, end;

    Dialog openDialog;
    World world;

    public EditorScreen(FileHandle worldFile) {
        super(INITIALIZE_ALL);
        Objects.requireNonNull(worldFile);
        this.viewport = new ExtendViewport(640, 360);

        if (worldFile.exists()) {
            byte[] data = worldFile.readBytes();
            world = (World) GameObject.deserializeGameObject(data);
            Log.info("Loaded world from " + worldFile.path());
        } else {
            world = new World();
            Log.info("Created new world");
        }
    }

    @Override
    public void show() {
        super.show();
        shapeRenderer = new ShapeRenderer();

        window = new Window("Creator", Sprites.getInstance().getSkin(), "small");
        window.getTitleLabel().setAlignment(Align.center);
        window.setScale(.5f);
        window.setSize(150, 300);
        window.setOrigin(Align.center);
        window.setPosition(5, (360 / 2), Align.left);
        window.padTop(20);
        window.setVisible(true);
        window.setMovable(true);
        window.setResizable(true);
        window.setModal(false);

        add(Align.left, window);

        float size = 40;

        ButtonGroup<Button> group = new ButtonGroup<Button>();
        wall = new TextButton("Wall", Sprites.skin(), "small");
        wall.setSize(size, size);
        wall.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedTile = 2;
            }
        });

        floor = new TextButton("Floor", Sprites.skin(), "small");
        floor.setSize(size, size);
        floor.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedTile = 1;
            }
        });

        clear = new TextButton("Clear", Sprites.skin(), "small");
        clear.setSize(size, size);
        clear.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedTile = 0;
            }
        });

        path = new TextButton("Path", Sprites.skin(), "small");
        path.setSize(size, size);
        path.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedTile = 3;
            }
        });

        start = new TextButton("Start", Sprites.skin(), "small");
        start.setSize(size, size);
        start.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedTile = 4;
            }
        });

        end = new TextButton("End", Sprites.skin(), "small");
        end.setSize(size, size);
        end.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedTile = 5;
            }
        });

        Label teamLabel = new Label("#" + teamNumber, Sprites.skin(), "small");
        teamLabel.setSize(size, size);
        teamLabel.setAlignment(Align.center);

        TextButton add = new TextButton("+", Sprites.skin(), "small");
        add.setSize(size, size);
        add.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                teamNumber++;
                if (teamNumber >= GameManager.MAX_TEAMS)
                    teamNumber = GameManager.MAX_TEAMS - 1;
                teamLabel.setText("#" + teamNumber);
            }
        });

        TextButton sub = new TextButton("-", Sprites.skin(), "small");
        sub.setSize(size, size);
        sub.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                teamNumber--;
                if (teamNumber < 0)
                    teamNumber = 0;
                teamLabel.setText("#" + teamNumber);
            }
        });
        
        TextButton importButton;
        importButton = new TextButton("Import", Sprites.skin(), "toggle-small");
        importButton.setSize(size * 3, size);
        importButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                openImportDialog();
            }
        });

        TextButton export = new TextButton("Export", Sprites.skin(), "toggle-small");
        export.setSize(size * 3, size);
        export.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                openExportDialog();
            }
        });

        TextField color = new TextField("6663ff", Sprites.skin(), "small") {
            @Override
            public void act(float delta) {
                super.act(delta);
            }
        };
        color.setSize(size, size);
        color.setMessageText("Color");
        color.setTextFieldFilter(new TextField.TextFieldFilter() {
            @Override
            public boolean acceptChar(TextField textField, char c) {
                return Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
            }
        });
        color.setMaxLength(6);
        color.setAlignment(Align.center);
        color.addListener(new InputListener() {
            @Override
            public boolean keyTyped(InputEvent event, char character) {
                try {
                    if (color.getText().length() == 6) {
                        world.worldColor = Color.valueOf("#"+color.getText());
                    }
                } catch (Exception e) {
                    color.getStyle().fontColor = Color.valueOf("#6663ff");
                }
                return true;
            }
        });

        group.add(wall);
        group.add(floor);
        group.add(clear);
        group.add(path);
        group.add(start);
        group.add(end);

        window.add(wall).size(size).pad(4);
        window.add(floor).size(size).pad(4);
        window.add(clear).size(size).pad(4).row();
        window.add(path).size(size).pad(4);
        window.add(start).size(size).pad(4);
        window.add(end).size(size).pad(4).row();
        window.add(sub).size(size).pad(4);
        window.add(teamLabel).size(size).pad(4);
        window.add(add).size(size).pad(4).row();
        window.add(export).size(size * 3, size).pad(4).colspan(3).row();
        window.add(importButton).size(size * 3, size).pad(4).colspan(3).row();
        window.add(color).size(size * 3, size).pad(4).colspan(3).row();
    }

    public void exportFile(FileHandle worldFile) {
        byte[] data = GameObject.serializeGameObject(world);
        worldFile.writeBytes(data, false);

        Label label = new Label("Saved as " + saveName + ".game", Sprites.skin().get("small", Label.LabelStyle.class));
        label.setAlignment(Align.right);
        label.setSize(100, 40);
        label.setPosition(640 - 10, 10, Align.bottomRight);

        label.addAction(Actions.sequence(
            Actions.fadeIn(.5f), 
            Actions.delay(2), 
            Actions.fadeOut(.5f), 
            Actions.removeActor()
        ));

        add(Align.bottomRight, label);
    }
    public void exportFile(String name) {
        FileHandle editorFolder = Gdx.files.external("retrotowerdefense/editor/");
        editorFolder.mkdirs();
        exportFile(editorFolder.child(name + ".game"));
    }
    private void openExportDialog() {
        TextField name = new TextField(saveName, Sprites.skin(), "small");
        name.setMessageText("Name");
        name.setTextFieldFilter(new TextField.TextFieldFilter() {
            @Override
            public boolean acceptChar(TextField textField, char c) {
                return Character.isLetterOrDigit(c) || c == '_';
            }
        });

        if (openDialog != null) {
            openDialog.remove();
            openDialog = null;
        }

        openDialog = new Dialog("Export", Sprites.skin(), "small") {
            @Override
            protected void result(Object object) {
                if (object instanceof Boolean) {
                    boolean result = (Boolean) object;
                    if (result) {
                        // Open file dialog
                        NativeFileChooserConfiguration conf = new NativeFileChooserConfiguration();
                        conf.directory = Gdx.files.absolute(System.getProperty("user.home")+"/retrotowerdefense/editor/");
                        conf.nameFilter = new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                return name.endsWith("game");
                            }
                        };
                        conf.title = "Export game...";
                        conf.intent = NativeFileChooserIntent.SAVE;
                        Files.promptFile(conf, new NativeFileChooserCallback() {
                            @Override
                            public void onFileChosen(FileHandle file) {
                                saveName = file.nameWithoutExtension();
                                exportFile(file);
                            }

                            @Override
                            public void onCancellation() {

                            }

                            @Override
                            public void onError(Exception exception) {

                            }
                        });
                    }
                    remove();
                    openDialog = null;
                    return;
                }
                saveName = name.getText();
                exportFile(name.getText());
                remove();
                openDialog = null;
            }
        };
        openDialog.padTop(30);

        openDialog.getContentTable().add(name).size(100, 20).pad(4);

        openDialog.button("Confirm", null, Sprites.skin().get("small", TextButtonStyle.class));
        openDialog.button("File...", true, Sprites.skin().get("small", TextButtonStyle.class));
        openDialog.button("Cancel", false, Sprites.skin().get("small", TextButtonStyle.class));
        openDialog.show(stages.get(Align.center));
    }
    
    public void importFile(String name) {
        FileHandle editorFolder = Gdx.files.external("retrotowerdefense/editor/");
        editorFolder.mkdirs();
        importFile(editorFolder.child(name + ".game"));
    }
    public void importFile(FileHandle worldFile) {
        if (worldFile.exists()) {
            byte[] data = worldFile.readBytes();
            world = (World) GameObject.deserializeGameObject(data);
        }

        Label label = new Label("Opened " + saveName + ".game", Sprites.skin().get("small", Label.LabelStyle.class));
        label.setAlignment(Align.right);
        label.setSize(100, 40);
        label.setPosition(640 - 10, 10, Align.bottomRight);

        label.addAction(Actions.sequence(
            Actions.fadeIn(.5f), 
            Actions.delay(2), 
            Actions.fadeOut(.5f), 
            Actions.removeActor()
        ));

        add(Align.bottomRight, label);
    }
    private void openImportDialog() {
        TextField name = new TextField(saveName, Sprites.skin(), "small");
        name.setMessageText("Name");
        name.setTextFieldFilter(new TextField.TextFieldFilter() {
            @Override
            public boolean acceptChar(TextField textField, char c) {
                return Character.isLetterOrDigit(c) || c == '_';
            }
        });

        if (openDialog != null) {
            openDialog.remove();
            openDialog = null;
        }

        openDialog = new Dialog("Import", Sprites.skin(), "small") {
            @Override
            protected void result(Object object) {
                if (object instanceof Boolean) {
                    boolean result = (Boolean) object;
                    if (result) {
                        // Open file dialog
                        NativeFileChooserConfiguration conf = new NativeFileChooserConfiguration();
                        conf.directory = Gdx.files.absolute(System.getProperty("user.home")+"/retrotowerdefense/editor/");
                        conf.nameFilter = new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                return name.endsWith("game");
                            }
                        };
                        conf.title = "Import game...";
                        conf.intent = NativeFileChooserIntent.OPEN;
                        Files.promptFile(conf, new NativeFileChooserCallback() {
                            @Override
                            public void onFileChosen(FileHandle file) {
                                saveName = file.nameWithoutExtension();
                                importFile(file);
                            }

                            @Override
                            public void onCancellation() {

                            }

                            @Override
                            public void onError(Exception exception) {

                            }
                        });
                    }
                    remove();
                    openDialog = null;
                    return;
                }

                saveName = name.getText();
                importFile(name.getText());
                remove();
                openDialog = null;
            }
        };
        openDialog.padTop(30);

        openDialog.getContentTable().add(name).size(100, 20).pad(4);

        openDialog.button("Confirm", null, Sprites.skin().get("small", TextButtonStyle.class));
        openDialog.button("File...", true, Sprites.skin().get("small", TextButtonStyle.class));
        openDialog.button("Cancel", false, Sprites.skin().get("small", TextButtonStyle.class));
        openDialog.show(stages.get(Align.center));
    }

    @Override
    public void render(float delta) {
        int mx = Gdx.input.getX();
        int my = Gdx.input.getY();
        Vector2 mousePos = viewport.unproject(new Vector2(mx, my));
        mousePos.x = (int) Math.floor(mousePos.x/Tile.SIZE) * Tile.SIZE;
        mousePos.y = (int) Math.floor(mousePos.y/Tile.SIZE) * Tile.SIZE;

        int tileX = (int) Math.floor(mousePos.x/Tile.SIZE);
        int tileY = (int) Math.floor(mousePos.y/Tile.SIZE);

        updateCamera();
        processMouse(tileX, tileY);
        processKeyboard();

        if (world != null) {
            viewport.apply();
            batch.setProjectionMatrix(viewport.getCamera().combined);
            batch.begin();
            world.render(batch, delta);
            batch.end();
        }

        TileType type = TileType.getType(selectedTile);

        if (type != null) {
            Tile tile = type.getTile();
            batch.begin();
            if (world != null) {
                tile.render(batch, delta, world, tileX, tileY, 1);
            }
            batch.end();
        }

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, 1);
        shapeRenderer.rect(mousePos.x, mousePos.y, Tile.SIZE, Tile.SIZE);
        shapeRenderer.setColor(.5f, .5f, .5f, .5f);
        shapeRenderer.rect(0, 0, Tile.SIZE * world.getWorldSize(), Tile.SIZE * world.getWorldSize());
        shapeRenderer.end();

        stages.actAll(delta);
        stages.drawAll(batch);
    }

    private void processKeyboard() {
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
            selectedTile = 2; // wall
            wall.setChecked(true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
            selectedTile = 1; // floor
            floor.setChecked(true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_3)) {
            selectedTile = 3; // path
            path.setChecked(true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_4)) {
            selectedTile = 4; // start
            start.setChecked(true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_5)) {
            selectedTile = 5; // end
            end.setChecked(true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.GRAVE)) {
            selectedTile = 0; // clear
            clear.setChecked(true);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_LEFT) ^ Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isKeyPressed(Input.Keys.S)) {
                if (saveName != null && !saveName.isEmpty()) {
                    exportFile(saveName);
                } else {
                    openExportDialog();
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_LEFT) ^ Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isKeyPressed(Input.Keys.L)) {
                openImportDialog();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (openDialog != null) {
                openDialog.reset();
                openDialog.remove();
                openDialog = null;
            } else {
                openDialog = new Dialog("Exit", Sprites.skin(), "small") {
                    @Override
                    protected void result(Object object) {
                        if (object instanceof Boolean && !((Boolean) object)) {
                            remove();
                            return;
                        }
                        game.setScreen(new MainMenuScreen());
                        remove();
                    }
                };

                openDialog.padTop(30);
                openDialog.text("Are you sure?", Sprites.skin().get("small", Label.LabelStyle.class));
                openDialog.button("Yes", true, Sprites.skin().get("small", TextButtonStyle.class));
                openDialog.button("No", false, Sprites.skin().get("small", TextButtonStyle.class));

                openDialog.show(stages.get(Align.center));
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height);
    }

    private void processMouse(int tileX, int tileY) {

        int mx = Gdx.input.getX();
        int my = Gdx.input.getY();

        Vector2 localMouse = window.screenToLocalCoordinates(new Vector2(mx, my));

        if (localMouse.x < window.getX() + window.getWidth() && !entered) {
            entered = true;
            window.clearActions();
            window.addAction(Actions.fadeIn(.15f));
        } else if (localMouse.x >= window.getX() + window.getWidth() && entered) {
            entered = false;
            window.clearActions();
            window.addAction(Actions.fadeOut(.15f));
        }

        // If mouse is over window, don't process mouse input.
        if (window.hit(localMouse.x, localMouse.y, true) != null) {
            return;
        }
        if (openDialog != null) {
            localMouse = openDialog.screenToLocalCoordinates(new Vector2(mx, my));
            if (openDialog.hit(localMouse.x, localMouse.y, true) != null) {
                return;
            }
        }

        Vector2 mousePos = viewport.unproject(new Vector2(mx, my));
        mousePos.x = (int) Math.floor(mousePos.x/Tile.SIZE) * Tile.SIZE;
        mousePos.y = (int) Math.floor(mousePos.y/Tile.SIZE) * Tile.SIZE;

        boolean left = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        boolean right = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);

        TileType type = TileType.getType(selectedTile);
        if (right) {
            type = null;
        }
        if ((left || right) && world != null && !(tileX < 0 || tileY < 0 || tileX >= world.getWorldSize() || tileY >= world.getWorldSize())) {
            if (selectedTile == 1 || selectedTile == 0) {
                world.setTile(tileX, tileY, 0, type);
            }
            if (selectedTile != 1 || selectedTile == 0) {
                world.setTile(tileX, tileY, 1, type);
            }

            if (selectedTile == 3) {
                world.setTileData(tileX, tileY, 1, new TeamTileData(teamNumber));
            }
            if (selectedTile == 4) {
                world.setTileData(tileX, tileY, 1, new StartTileData(teamNumber));
            }
            if (selectedTile == 5) {
                world.setTileData(tileX, tileY, 1, new StructureTileData(teamNumber));
            }
        }
    }

    private void updateCamera() {
        // Get the local player's position and center the camera on it.

        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            return;
        }

        float deltaSpeed = speed * Gdx.graphics.getDeltaTime() * (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? 3 : 1);
        float vx = Gdx.input.isKeyPressed(Input.Keys.A) ? -deltaSpeed : Gdx.input.isKeyPressed(Input.Keys.D) ? deltaSpeed : 0;
        float vy = Gdx.input.isKeyPressed(Input.Keys.S) ? -deltaSpeed : Gdx.input.isKeyPressed(Input.Keys.W) ? deltaSpeed : 0;
        cameraX += vx;
        cameraY += vy;

        if (world != null) {
            OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
            camera.position.set(cameraX, cameraY, 0);
        }
    }

}
