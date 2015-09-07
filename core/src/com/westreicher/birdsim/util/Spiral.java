package com.westreicher.birdsim.util;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by david on 9/4/15.
 */
public class Spiral {
    private Vector2 pos = new Vector2();
    private Vector2 ret = new Vector2();
    private Vector2 dir = new Vector2();
    private int len;
    private int step;

    public Spiral() {
        reset();
    }

    public void reset() {
        pos.set(0, 0);
        dir.set(1, 0);
        len = 1;
        step = 0;
    }

    public Vector2 next() {
        ret.set(pos);
        pos.add(dir);
        step++;
        if (step == len) {
            dir.rotate90(0);
            step = 0;
            if (Math.abs(dir.x) == 1)
                len++;
        }
        return ret;
    }
}
