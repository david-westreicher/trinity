package com.westreicher.birdsim;

import com.badlogic.gdx.graphics.Color;

/**
 * Created by david on 9/4/15.
 */
public class Util {
    public static Color randomColor() {
        return new Color((float) Math.random(), (float) Math.random(), (float) Math.random(), 1);
    }
}
