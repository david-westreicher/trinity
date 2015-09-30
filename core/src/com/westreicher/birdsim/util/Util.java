package com.westreicher.birdsim.util;

import com.badlogic.gdx.Gdx;

/**
 * Created by david on 9/26/15.
 */
public class Util {
    public static <T> boolean contains(final T[] array, final T v) {
        for (final T e : array)
            if (e == v)
                return true;
        return false;
    }

    public static float interpolateRot(float from, float to, float alpha) {
        float dist = normalizeRot(to) - normalizeRot(from);
        if (Math.abs(dist) > Math.PI)
            dist += -(float) Math.PI * 2 * Math.signum(dist);
        return dist * alpha;
    }

    private static float normalizeRot(float rot) {
        while (rot < 0) {
            rot += Math.PI * 2;
        }
        while (rot >= Math.PI * 2) {
            rot -= Math.PI * 2;
        }
        return rot;
    }

}
