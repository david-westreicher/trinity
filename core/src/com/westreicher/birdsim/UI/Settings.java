package com.westreicher.birdsim.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Created by juanolon on 17/10/15.
 */
public class Settings {

    // debug
    // -- post processing
    // -- debug = PROFILE
    // controls
    // -- use vibration
    // UI
    // -- particles?

    private Preferences prefs;

    public Settings(){
        prefs = Gdx.app.getPreferences("preferences");
    }

    public boolean isDebug() {
        return prefs.getBoolean("debug.debug", false);
    }

    public Settings setDebug(boolean state){
        prefs.putBoolean("debug.debug", state);
        return this;
    }

    public void flush(){
        prefs.flush();
    }
}
