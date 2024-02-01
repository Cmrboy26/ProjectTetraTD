package net.cmr.rtd;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;

import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import net.cmr.util.CMRGame;

public class RetroTowerDefense extends CMRGame {
	
	public RetroTowerDefense(NativeFileChooser fileChooser) {
		super(fileChooser);
	}

	@Override
	public void create () {
		super.create();
		showIntroScreen(null);
	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);
		super.render();
	}
	
	@Override
	public void dispose () {
		super.dispose();
	}
}
