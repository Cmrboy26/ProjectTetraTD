package net.cmr.rtd;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.desktop.DesktopFileChooser;
// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		if (arg.length >= 1) {
			if (arg[0].equals("debug")) {
				ProjectTetraTD.setDebug(true);
			}
		}

		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle(ProjectTetraTD.GAME_NAME);
		config.setWindowIcon("raw/icon.png");
		NativeFileChooser chooser = new DesktopFileChooser();
		new Lwjgl3Application(new ProjectTetraTD(chooser), config);
	}
}
