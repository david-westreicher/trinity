package com.westreicher.birdsim.artemis.systems;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

/**
 * Created by david on 9/28/15.
 */
public class StartRendering extends BaseSystem {
    @Override
    protected void processSystem() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        //Gdx.gl.glClearColor(0.251f, 0.643f, 0.875f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

}
