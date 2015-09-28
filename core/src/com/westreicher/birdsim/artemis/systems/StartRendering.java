package com.westreicher.birdsim.artemis.systems;

import com.artemis.BaseSystem;
import com.artemis.Entity;
import com.artemis.managers.TagManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.components.RenderPosition;

/**
 * Created by david on 9/28/15.
 */
public class StartRendering extends BaseSystem {
    @Override
    protected void processSystem() {
        interpolateCam();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        //Gdx.gl.glClearColor(0.251f, 0.643f, 0.875f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    private void interpolateCam() {
        Entity camentity = world.getManager(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG);
        RenderPosition pos = camentity.getComponent(RenderPosition.class);
        Camera cam = camentity.getComponent(CameraComponent.class).cam;
        cam.position.set(pos.x, pos.y, 250);
        cam.update();
    }
}
