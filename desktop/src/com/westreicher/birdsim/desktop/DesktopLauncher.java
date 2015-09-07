package com.westreicher.birdsim.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.westreicher.birdsim.MyGdxGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.samples = 4;
		config.vSyncEnabled = false;
		config.resizable = true;
		new LwjglApplication(new MyGdxGame(), config);
	}
}
