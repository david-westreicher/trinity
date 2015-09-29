package com.westreicher.birdsim.input;

import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.westreicher.birdsim.Config;

import java.util.ArrayList;

/**
 * Created by david on 9/21/15.
 */
public abstract class InputHelper {

    protected boolean isPaused;
    protected float movx;
    protected float movy;
    protected float shootx;
    protected float shooty;
    protected boolean ismoving;
    protected boolean isshooting;

    public abstract void update();

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isMoving() {
        return ismoving;
    }

    public boolean isShooting() {
        return isshooting;
    }

    public float getMoveRadiant() {
        return (float) Math.atan2(-movy, movx);
    }

    public float getShootRadiant() {
        return (float) Math.atan2(-shooty, shootx);
    }

    public void resize(World w) {
    }
}
