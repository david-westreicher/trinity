package com.westreicher.birdsim.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.westreicher.birdsim.Config;

/**
 * Created by david on 9/21/15.
 */
public abstract class InputHelper {

    private boolean isDown;
    private int startX;
    private int startY;

    public abstract boolean update();

    public abstract int relx();

    public abstract int rely();

    public boolean isDown() {
        return isDown;
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

    public static class Keyboard extends InputHelper {
        private static final int KEYS[][] = new int[][]{
                new int[]{com.badlogic.gdx.Input.Keys.A, com.badlogic.gdx.Input.Keys.LEFT},//LEFT
                new int[]{com.badlogic.gdx.Input.Keys.W, com.badlogic.gdx.Input.Keys.UP},//UP
                new int[]{com.badlogic.gdx.Input.Keys.D, com.badlogic.gdx.Input.Keys.RIGHT},//RIGHT
                new int[]{com.badlogic.gdx.Input.Keys.S, com.badlogic.gdx.Input.Keys.DOWN},//DOWN
        };
        private static final int DELTAS[][] = new int[][]{
                new int[]{-1, 0},//LEFT
                new int[]{0, 1},//UP
                new int[]{1, 0},//UP
                new int[]{0, -1},//DOWN
        };
        private final int controls;
        private int deltay;
        private int deltax;
        private boolean isDown;

        public Keyboard(int controls) {
            this.controls = controls;
            super.startX = -100;
            super.startY = -100;
        }

        @Override
        public boolean update() {
            deltax = 0;
            deltay = 0;
            for (int i = 0; i < KEYS.length; i++)
                if (Gdx.input.isKeyPressed(KEYS[i][controls])) {
                    deltax += DELTAS[i][0];
                    deltay += DELTAS[i][1];
                }
            isDown = deltax != 0 || deltay != 0;
            return isDown;
        }

        @Override
        public int relx() {
            return deltax;
        }

        @Override
        public int rely() {
            return -deltay;
        }

    }

    public static class SaveMouse extends InputHelper {
        private final int index;
        private final Viewport v;
        private boolean isDown = false;

        public SaveMouse(int index, Viewport v) {
            this.index = index;
            this.v = v;
        }

        public boolean update() {
            int width = v.getScreenWidth();
            int height = v.getScreenHeight();
            if (Gdx.input.isTouched(index)) {
                if (!isDown) {
                    int currentx = Gdx.input.getX(index);
                    int currenty = Gdx.input.getY(index);
                    if (Math.abs(currentx - width / 2) < width / 2 - 100
                            && Math.abs(currenty - height / 2) < height / 2 - 100) {
                        super.startX = currentx;
                        super.startY = currenty;
                    }
                }
                isDown = true;
            } else {
                isDown = false;
            }
            return isDown;
        }

        public int relx() {
            return (Gdx.input.getX(index) - super.startX);
        }

        public int rely() {
            return (Gdx.input.getY(index) - super.startY);
        }

    }
}
