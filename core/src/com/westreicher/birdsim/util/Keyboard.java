package com.westreicher.birdsim.util;

import com.badlogic.gdx.Gdx;

/**
 * Created by juanolon on 21/09/15.
 */
public class Keyboard extends InputHelper {
    private final int controls;
    private int deltay;
    private int deltax;

    private static final int KEYS[][] = new int[][]{
            new int[]{com.badlogic.gdx.Input.Keys.A, com.badlogic.gdx.Input.Keys.LEFT},//LEFT
            new int[]{com.badlogic.gdx.Input.Keys.W, com.badlogic.gdx.Input.Keys.UP},//UP
            new int[]{com.badlogic.gdx.Input.Keys.D, com.badlogic.gdx.Input.Keys.RIGHT},//RIGHT
            new int[]{com.badlogic.gdx.Input.Keys.S, com.badlogic.gdx.Input.Keys.DOWN},//DOWN
    };
    private static final int DELTAS[][] = new int[][]{
            new int[]{-1, 0},//LEFT
            new int[]{0, 1},//UP
            new int[]{1, 0},//RIGHT
            new int[]{0, -1},//DOWN
    };


    public Keyboard(int controls) {
        this.controls = controls;
        startX = -100;
        startY = -100;
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
        super.setDown(deltax != 0 || deltay != 0);
        return super.isDown();
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
