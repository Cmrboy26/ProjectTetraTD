package net.cmr.rtd;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
//import games.spooky.gdx.nativefilechooser.android.AndroidFileChooser;
import net.cmr.rtd.ProjectTetraTD;
import net.cmr.util.Settings;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new ProjectTetraTD(null), config);
	}
}
