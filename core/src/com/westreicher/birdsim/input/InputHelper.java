package com.westreicher.birdsim.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.westreicher.birdsim.Config;

import java.util.ArrayList;

/**
 * Created by david on 9/21/15.
 */
public abstract class InputHelper {

    // giving as %
    private static final int center1X = 15;
    private static final int center2X = 85;

    private static final int centerY = 70;

    public static class PlayerInput {
        public InputHelper firstPointer;
        public InputHelper secondPointer;
        public InputHelper thirdPointer;

        public PlayerInput(InputHelper... inputs) {
            this.firstPointer = inputs[0];
            this.secondPointer = inputs[1];
            if (inputs.length <= 2)
                this.thirdPointer = new DummyInput();
            else
                this.thirdPointer = inputs[2];
        }
    }

    public static ArrayList<PlayerInput> players;

    public static void init(Viewport viewport) {
        players = new ArrayList<PlayerInput>();
        if (Config.IS_DESKTOP) {
            for (Controller ctrl : Controllers.getControllers()) {
                players.add(new PlayerInput(new CController(0, ctrl), new CController(1, ctrl)));
                Gdx.app.log("game", "Gamepad found: " + ctrl.getName());
            }
            if (players.size() == 0) {
                players.add(new PlayerInput(new Keyboard(0), new Keyboard(1)));
            }
        } else {
            players.add(new PlayerInput(new SaveMouse(0, viewport, center1X, centerY), new SaveMouse(1, viewport, center2X, centerY), new SaveMouse(2, viewport, 0, 0)));
        }
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

    public int getStartX(int w) {
        return (w * startX) / 100;
    }

    public int getStartY(int h) {
        return (h * startY) / 100;
    }

    public static class DummyInput extends InputHelper {
        @Override
        public boolean update() {
            return false;
        }

        @Override
        public int relx() {
            return 0;
        }

        @Override
        public int rely() {
            return 0;
        }
    }

}
