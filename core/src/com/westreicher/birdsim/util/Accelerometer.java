package com.westreicher.birdsim.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by juanolon on 25/09/15.
 */
public class Accelerometer extends InputHelper {
    private final int index;
    private final Viewport v;

    private int deltay;
    private int deltax;
    private static final Matrix4 MATRIX = new Matrix4();
    private static final Vector3 DIRECTION = new Vector3(0, 0, -1).nor();
    private static final Vector3 TEMP_DIR = new Vector3();

    public Accelerometer(int index, Viewport v) {
        this.index = index;
        this.v = v;
        super.setDown(true);
    }

    @Override
    public boolean update() {
        float accelX = Gdx.input.getAccelerometerX();
        float accelY = Gdx.input.getAccelerometerY();

        Gdx.input.getRotationMatrix(MATRIX.val);
        TEMP_DIR.set(DIRECTION);
        TEMP_DIR.mul(MATRIX);
        deltax = (int) (TEMP_DIR.y * 100);
        deltay = (int) (TEMP_DIR.x * 100);
        return true;
    }

    @Override
    public int relx() {
        return -deltax;
    }

    @Override
    public int rely() {
        return -deltay;
    }
}
