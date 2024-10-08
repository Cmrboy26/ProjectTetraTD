package net.cmr.rtd.screen;

import java.util.ArrayList;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;

import net.cmr.rtd.ProjectTetraTD;
import net.cmr.rtd.game.EasterEgg;
import net.cmr.rtd.game.GameConnector;
import net.cmr.rtd.game.achievements.Achievement;
import net.cmr.rtd.game.achievements.Achievement.AchievementDisplay;
import net.cmr.rtd.game.achievements.AchievementManager;
import net.cmr.rtd.game.files.QuestFile;
import net.cmr.rtd.screen.SelectionScreen.PlayType;
import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Audio;
import net.cmr.util.Audio.GameMusic;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.SpriteType;

public class MainMenuScreen extends AbstractScreenEX {

	Dialog creditsDialog;
	boolean easterEggRunning = false;

    public MainMenuScreen() {
        super(INITIALIZE_ALL);
    }

	@Override
	public void show() {
		super.show();

		Table table = new Table();
		table.setFillParent(true);

		float iconPadding = 10;

		int outsideSpan = 1;
		int insideSpan = 2;

		float widthHeightRatio = 136.0f / 32.0f;
		float height = 96.0f;
		float width = height * widthHeightRatio;

		/*Image background = new Image(Sprites.drawable(SpriteType.AREA)) {
			@Override
			public void draw(Batch batch, float parentAlpha) {
				ProjectTetraTD.getInstance(ProjectTetraTD.class).shaderManager.enableShader(batch, CustomShader.HEAT);
				batch.setColor(Color.BLUE);
				super.draw(batch, parentAlpha);
				batch.setColor(Color.WHITE);
				ProjectTetraTD.getInstance(ProjectTetraTD.class).shaderManager.disableShader(batch);
			}
		};
		background.setFillParent(true);
		background.getColor().a = .5f;*/

		table.add(new Image(Sprites.drawable(SpriteType.TITLE))).height(height).width(width).pad(0, 50, 0, 50).colspan(outsideSpan * 2 + insideSpan).row();

		String labelType = "small";

		TextButton play = new TextButton("Play", Sprites.skin(), labelType);
		Audio.addClickSFX(play);
		play.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				ProjectTetraTD game = ProjectTetraTD.getInstance(ProjectTetraTD.class);
				game.setScreen(new SelectionScreen());
                //game.setScreen(new SelectionScreen());
			}
		});

		TextButton resume = new TextButton("Resume", Sprites.skin(), labelType);
		Audio.addClickSFX(resume);
		resume.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				ProjectTetraTD game = ProjectTetraTD.getInstance(ProjectTetraTD.class);
				String[] data = game.getLastPlayedQuest();
				QuestFile file = QuestFile.deserialize(data);
				int team = game.getLastPlayedTeam();
				PlayType.SINGLEPLAYER.startGame(file, team);
			}
		});
		boolean hasLastPlayed = ProjectTetraTD.getInstance(ProjectTetraTD.class).hasLastPlayedQuest();

		int playSpan = insideSpan + 2 * outsideSpan;
		int padInside = 100;
		if (hasLastPlayed) {
			playSpan /= 2;
			padInside = 0;
		}

		table.add(play).padLeft(100.0f).padRight(padInside).space(10.0f).colspan(playSpan).fillX();
		if (hasLastPlayed) {
			table.add(resume).padLeft(padInside).padRight(100.0f).space(10.0f).colspan(playSpan).fillX();
		}
		table.row();

		TextButton tutorial = new TextButton("Tutorial", Sprites.skin(), labelType);
		Audio.addClickSFX(tutorial);
		tutorial.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				GameConnector.startTutorial();
			}
		});
		table.add(tutorial).padLeft(100.0f).padRight(100.0f).space(10.0f).colspan(insideSpan + 2 * outsideSpan).fillX();
		table.row();

		if (!ProjectTetraTD.isMobile() && EasterEgg.isColten()) {
			TextButton editor = new TextButton("Editor (WIP)", Sprites.skin(), labelType);
			Audio.addClickSFX(editor);
			editor.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					FileHandle handle = Gdx.files.external("editorWorld.dat");
					fadeToScreen(new EditorScreen(handle), .5f, Interpolation.linear, false);
				}
			});
			table.add(editor).padLeft(100.0f).padRight(100.0f).space(10.0f).colspan(insideSpan + 2 * outsideSpan).fillX();
			table.row();	
		}

		TextButton textButton = new TextButton("Settings", Sprites.skin(), labelType);
		Audio.addClickSFX(textButton);
		textButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				ProjectTetraTD game = ProjectTetraTD.getInstance(ProjectTetraTD.class);
				game.setScreen(new SettingsScreen());	
			}
		});
		table.add(textButton).padLeft(100.0f).padRight(100.0f).space(10.0f).colspan(insideSpan + 2 * outsideSpan).fillX();
		table.row();

		textButton = new TextButton("Exit", Sprites.skin(), labelType);
		Audio.addClickSFX(textButton);
		textButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Gdx.app.exit();
			}
		});
		table.add(textButton).padLeft(100.0f).padRight(100.0f).space(10.0f).colspan(insideSpan + 2 * outsideSpan).fillX();
		add(Align.center, table);

		Table table1 = new Table();
		table1.setFillParent(true);
		Table leftBottomTable = new Table();
		table1.add(leftBottomTable).expand().align(Align.bottomLeft);
		String shortenedTitle = "";
		// Get all capital letters in the title
		for (int i = 0; i < ProjectTetraTD.GAME_NAME.length(); i++) {
			char c = ProjectTetraTD.GAME_NAME.charAt(i);
			if (Character.isUpperCase(c)) {
				shortenedTitle += c;
			}
		}
		String versionInfo = shortenedTitle+" v"+ProjectTetraTD.MAJORVERSION+"."+ProjectTetraTD.MINORVERSION+"."+ProjectTetraTD.PATCHVERSION;
		if (ProjectTetraTD.TEST_VERSION) {
			versionInfo += " (Development)";
		}
		if (Gdx.app.getType() == ApplicationType.Android) {
			versionInfo += " (Android)";
		}
		leftBottomTable.add(new Label(versionInfo, Sprites.skin(), "small")).pad(5.0f);
		add(Align.bottomLeft, table1);

		Table table2 = new Table();
		table2.setFillParent(true);

		Table icongroup = new Table();
		table2.add(icongroup).expand().align(Align.bottomRight);
		int size = 30;

		TextButton feedback = new TextButton("Feedback", Sprites.skin(), labelType);
		Audio.addClickSFX(feedback);
		feedback.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				ProjectTetraTD game = ProjectTetraTD.getInstance(ProjectTetraTD.class);
				game.setScreen(new FeedbackScreen());
			}
		});
		feedback.pad(0, 15, 0, 15);
		icongroup.add(feedback).pad(10.0f).expandX();

		TextButton credits = new TextButton("Credits", Sprites.skin(), labelType);
		Audio.addClickSFX(credits);
		final Stage stage = this.stages.get(Align.center);
		credits.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				ProjectTetraTD game = ProjectTetraTD.getInstance(ProjectTetraTD.class);

				LabelStyle small = Sprites.skin().get("small", LabelStyle.class);

				if (creditsDialog != null) {
					creditsDialog.remove();
					creditsDialog = null;
				}
				creditsDialog = new Dialog("Credits", Sprites.skin());
				creditsDialog.setKeepWithinStage(false);
				creditsDialog.pad(20);
				creditsDialog.padTop(50);
				creditsDialog.text("Programming, Music, Art: Colten Reissmann", small);
				creditsDialog.getContentTable().row();
				creditsDialog.text("Testing: Felipe, Andrew, Maxwell", small);
				creditsDialog.getContentTable().row();
				creditsDialog.text("Copyright Colten Reissmann (C)", small);
				creditsDialog.getContentTable().row();

				// April fools joke
				if (EasterEgg.isAprilFools() || EasterEgg.isMaxwell() || EasterEgg.isFelipe()) {
					creditsDialog.text("Icon: Barnold Barnoldfanger Sr. (from Georgia)", small);
					Image barnold = new Image(Sprites.drawable(SpriteType.BARNOLD2));
					creditsDialog.getContentTable().add(barnold).size(48).pad(1);
					creditsDialog.getContentTable().row();
					creditsDialog.text("Thank you to the Barnoldfanger \nfamily for their generous \"donation\"", small);
					creditsDialog.getContentTable().row();
	
					Stack urlstack = new Stack();
					Label underlines = new Label("__________________", small);
					underlines.setColor(Color.BLUE);
					urlstack.add(underlines);
					Label label = new Label("In memory of Junior...", small);
					label.addListener(new ClickListener() {
						@Override
						public void clicked(InputEvent event, float x, float y) {
							Gdx.net.openURI("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
							label.addAction(Actions.sequence(Actions.delay(3), Actions.run(() -> {
								label.setText("April fools");
							})));
						}
					});
					urlstack.add(label);
					label.setColor(Color.BLUE);
					creditsDialog.getContentTable().add(urlstack);
					barnold = new Image(Sprites.drawable(SpriteType.BARNOLD));
					creditsDialog.getContentTable().add(barnold).size(48).pad(1);
					creditsDialog.getContentTable().row();
				}

				TextButton button = new TextButton("Close", Sprites.skin(), "small");
				button.pad(0, 15, 0, 15);

				creditsDialog.button(button, false);
				
				creditsDialog.pack();
				creditsDialog.setScale(.75f);
				creditsDialog.setOrigin(Align.center);
				creditsDialog.setPosition(640/2, 360/2, Align.center);

				creditsDialog.show(stage);
			}
		});

		icongroup.add(credits).pad(5).width(credits.getWidth()+15);
		ImageButtonStyle style2 = new ImageButtonStyle();
		int patch = 5;
		style2.down = new NinePatchDrawable(new NinePatch(Sprites.sprite(SpriteType.BORDER_DOWN), patch, patch, patch, patch));
        style2.up = new NinePatchDrawable(new NinePatch(Sprites.sprite(SpriteType.BORDER_DEFAULT), patch, patch, patch, patch));
        style2.over = new NinePatchDrawable(new NinePatch(Sprites.sprite(SpriteType.BORDER_HOVER), patch, patch, patch, patch));
        style2.checked = new NinePatchDrawable(new NinePatch(Sprites.sprite(SpriteType.BORDER_DEFAULT), patch, patch, patch, patch));
        style2.disabled = new NinePatchDrawable(new NinePatch(Sprites.sprite(SpriteType.BORDER_DISABLED), patch, patch, patch, patch));
		style2.imageUp = Sprites.drawable(SpriteType.TROPHY);
		ImageButton achievements = new ImageButton(style2);
		Audio.addClickSFX(achievements);
		achievements.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				openAchievementWindow();
			}
		});
		icongroup.add(achievements).pad(5.0f).size(size);

		add(Align.bottomRight, table2);
	}

	@Override
	public void resume() {
		super.resume();
		stages.clear();
	}

    @Override
    public void render(float delta) {
        game.batch().setColor(Color.WHITE);
		//ProjectTetraTD.getInstance(ProjectTetraTD.class).shaderManager.enableShader(batch, CustomShader.HEAT);
        super.render(delta);
		//ProjectTetraTD.getInstance(ProjectTetraTD.class).shaderManager.disableShader(batch);
    }
    
    @Override
    public void hide() {
        super.hide();
    }

	public void openAchievementWindow() {
		Window window = new Window("Achievements", Sprites.skin(), "small");
		window.setMovable(false);
		window.pad(50, 5, 0, 5);
		window.getTitleLabel().setAlignment(Align.center);
		window.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if (keycode == Input.Keys.ESCAPE) {
					window.remove();
					return true;
				}
				return false;
			}
		});

		Table achievementTable = new Table();
		achievementTable.defaults().pad(5);

		ArrayList<Achievement<?>> orderedAchievements = new ArrayList<>(AchievementManager.getInstance().getAchievements().values());
		ArrayList<Class<? extends Achievement<?>>> order = Achievement.getAchievementRegisterOrder();
		orderedAchievements.sort((a1, a2) -> {
			return order.indexOf(a1.getClass()) - order.indexOf(a2.getClass());
		});

		for (Achievement<?> achievement : orderedAchievements) {
			achievementTable.add(new AchievementDisplay((ProjectTetraTD) game, achievement)).row();
		}

		ScrollPane scrollPane = new ScrollPane(achievementTable, Sprites.skin());
		scrollPane.setFadeScrollBars(false);
		scrollPane.setScrollingDisabled(true, false);
		scrollPane.setScrollbarsOnTop(false);
		scrollPane.setScrollbarsVisible(true);

		window.add(scrollPane).width(400).height(200).expand().fill().row();;
		
		TextButton close = new TextButton("Close", Sprites.skin(), "small");
		Audio.addClickSFX(close);
		close.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				window.remove();
			}
		});
		window.add(close).pad(5).growX().row();

		stages.get(Align.center).addActor(window);
		window.pack();
		window.setPosition(640/2, 360/2, Align.center);
		window.toFront();
	}

    @Override
    public GameMusic getScreenMusic() {
        return GameMusic.menuMusic();
    }

}
