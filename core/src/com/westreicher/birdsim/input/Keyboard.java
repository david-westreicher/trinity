package com.westreicher.birdsim.input;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Created by david on 9/29/15.
 */
public class Keyboard extends AbstractInput {
    private static final int KEYS[][] = new int[][]{
            new int[]{Input.Keys.A, Input.Keys.LEFT},//LEFT
            new int[]{Input.Keys.W, Input.Keys.UP},//UP
            new int[]{Input.Keys.D, Input.Keys.RIGHT},//RIGHT
            new int[]{Input.Keys.S, Input.Keys.DOWN},//DOWN
    };
    private static final int DELTAS[][] = new int[][]{
            new int[]{-1, 0},//LEFT
            new int[]{0, -1},//UP
            new int[]{1, 0},//RIGHT
            new int[]{0, 1},//DOWN
    };

    @Override
    public void update() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            isPaused = !isPaused;
        movx = 0;
        movy = 0;
        for (int i = 0; i < KEYS.length; i++)
            if (Gdx.input.isKeyPressed(KEYS[i][0])) {
                movx += DELTAS[i][0];
                movy += DELTAS[i][1];
            }
        ismoving = (movx != 0 || movy != 0);

        shootx = 0;
        shooty = 0;
        for (int i = 0; i < KEYS.length; i++)
            if (Gdx.input.isKeyPressed(KEYS[i][1])) {
                shootx += DELTAS[i][0];
                shooty += DELTAS[i][1];
            }
        isshooting = (shootx != 0 || shooty != 0);
    }

}
