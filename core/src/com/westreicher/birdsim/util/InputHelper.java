package com.westreicher.birdsim.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.westreicher.birdsim.Config;

/**
 * Created by david on 9/21/15.
 */
public abstract class InputHelper {

    public static InputHelper firstPointer;
    public static InputHelper secondPointer;
    public static InputHelper thirdPointer;

    public static void init(boolean isDesktop, Viewport viewport) {
        Controller ctrl = null;
        for (Controller controller : Controllers.getControllers()) {
            ctrl = controller;
            break;
        }

        if (isDesktop) {
            if (ctrl != null) {
                firstPointer = new CController(0, ctrl);
                secondPointer = new CController(1, ctrl);
                Gdx.app.log("game", "Gamepad found: " + ctrl.getName());
            } else {
                firstPointer = new Keyboard(0);
                secondPointer = new Keyboard(1);
            }
        } else {
            firstPointer = new SaveMouse(0, viewport);
            secondPointer = new SaveMouse(1, viewport);
        }

        thirdPointer = new SaveMouse(2, viewport);
    }

    private boolean isDown;
    protected int startX;
    protected int startY;

    public abstract boolean update();

    public abstract int relx();

    public abstract int rely();

    public boolean isDown() {
        return isDown;
    }

    public void setDown(boolean down) {
        isDown = down;
    }

    public float getRadiant() {
        return (float) Math.atan2(-rely(), relx());
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

}
