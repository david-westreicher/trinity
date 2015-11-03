package com.westreicher.birdsim;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.westreicher.birdsim.artemis.Artemis;

public class MyGdxGame extends ApplicationAdapter {

    private Artemis artemis;

    @Override
    public void resize(int width, int height) {
        artemis.resize(width, height);
    }

    @Override
    public void create() {
        artemis = Artemis.init();
    }

    @Override
    public void render() {
        artemis.process();
    }

    @Override
    public void dispose() {
        Gdx.app.log("game", "dispose");
        artemis.dispose();
    }
}
