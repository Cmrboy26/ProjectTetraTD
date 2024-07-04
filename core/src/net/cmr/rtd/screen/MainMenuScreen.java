package net.cmr.rtd.screen;

import java.util.Calendar;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;

import net.cmr.rtd.ProjectTetraTD;
import net.cmr.rtd.game.GameConnector;
import net.cmr.rtd.game.files.QuestFile;
import net.cmr.rtd.screen.NewSelectionScreen.PlayType;
import net.cmr.util.AbstractScreenEX;
import net.cmr.util.Audio;
import net.cmr.util.CMRGame;
import net.cmr.util.Audio.GameSFX;
import net.cmr.util.IntroScreen;
import net.cmr.util.Sprites;
import net.cmr.util.Sprites.AnimationType;
import net.cmr.util.Sprites.SpriteType;

public class MainMenuScreen extends AbstractScreenEX {

	Dialog creditsDialog;
	boolean easterEggRunning = false;

    public MainMenuScreen() {
        super(INITIALIZE_ALL);

		Table table = new Table();
		table.setFillParent(true);

		float iconPadding = 10;

		int outsideSpan = 1;
		int insideSpan = 2;

		float widthHeightRatio = 136.0f / 32.0f;
		float height = 96.0f;
		float width = height * widthHeightRatio;

		table.add(new Image(Sprites.drawable(SpriteType.TITLE))).height(height).width(width).pad(0, 50, 0, 50).colspan(outsideSpan * 2 + insideSpan).row();

		/*table.add(new Image(Sprites.drawable(AnimationType.SHOOTER_TOWER_2, 0))).size(32).padRight(iconPadding).colspan(outsideSpan);

		Label label = new Label(RetroTowerDefense.GAME_NAME, Sprites.skin(), "default");
		label.setOrigin(Align.bottom);
		label.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				float sizeBeforeX = label.getFontScaleX();
				float sizeBeforeY = label.getFontScaleY();
				float scaleX = 0.25f;
				float scaleY = 1f;
				if (easterEggRunning) return;
				easterEggRunning = true;
				Audio.getInstance().playSFX(GameSFX.FIREBALL_LAUNCH, 1f);
				Action action = Actions.sequence(
					new Action() {
						float elapsedTime = 0;
						float time = .15f / 2;
						@Override
						public boolean act(float delta) {
							elapsedTime += delta;
							label.setFontScaleX(label.getFontScaleX() + delta * scaleX / time);
							label.setFontScaleY(label.getFontScaleY() + delta * scaleY / time);
							return elapsedTime > time;
						}
					},
					new Action() {
						float elapsedTime = 0;
						float time = .2f;
						@Override
						public boolean act(float delta) {
							elapsedTime += delta;
							label.setFontScaleX(label.getFontScaleX() - delta * scaleX / time);
							label.setFontScaleY(label.getFontScaleY() - delta * scaleY / time);
							return elapsedTime > time;
						}
					},
					new Action() {
						@Override
						public boolean act(float delta) {
							label.setFontScaleX(sizeBeforeX);
							label.setFontScaleY(sizeBeforeY);
							easterEggRunning = false;
							return true;
						}
					}
				);
				label.addAction(action);
			}
		});
		label.setAlignment(Align.center);
		Interpolation interpolation = Interpolation.smooth;
		float duration = 2.0f;
		float offset = 3.0f;
		label.setPosition(label.getX(), label.getY());
		label.addAction(Actions.forever(Actions.sequence(Actions.moveBy(0, offset, duration, interpolation), Actions.moveBy(0, -offset, duration, interpolation))));
		table.add(label).colspan(insideSpan);

		table.add(new Image(Sprites.drawable(SpriteType.ICE_TOWER))).size(32).padLeft(iconPadding);
		table.row().colspan(outsideSpan);*/

		String labelType = "small";

		TextButton play = new TextButton("Play", Sprites.skin(), labelType);
		Audio.addClickSFX(play);
		play.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				ProjectTetraTD game = ProjectTetraTD.getInstance(ProjectTetraTD.class);
				game.setScreen(new NewSelectionScreen());
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

		if (!ProjectTetraTD.isMobile() && ProjectTetraTD.getInstance(ProjectTetraTD.class).getUsername().equals("Cmrboy26")) {
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
				creditsDialog.text("Beta Testing: SirPotato42, Andrew", small);
				creditsDialog.getContentTable().row();
				creditsDialog.text("Copyright Colten Reissmann (C)", small);
				creditsDialog.getContentTable().row();

				// April fools joke
				if (Calendar.getInstance().get(Calendar.MONTH) == Calendar.APRIL && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1) {
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
		icongroup.add(new ImageButton(style2)).pad(5.0f).size(size);

		add(Align.bottomRight, table2);
    }

	@Override
	public void resume() {
		super.resume();
		stages.clear();
		IntroScreen introScreen = new IntroScreen(new MainMenuScreen());
		game.setScreen(introScreen);
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

}
