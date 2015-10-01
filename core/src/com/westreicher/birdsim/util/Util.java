package com.westreicher.birdsim.util;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;
import com.westreicher.birdsim.artemis.components.EntityType;
import com.westreicher.birdsim.artemis.components.MapCoordinate;

/**
 * Created by david on 9/26/15.
 */
public class Util {
    private static final Bag<Component> TMP_BAG = new Bag<Component>();

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

    public static void log(Entity e) {
        TMP_BAG.clear();
        for (Component c : e.getComponents(TMP_BAG)) {
            if (c != null)
                Gdx.app.log("components: ", c.getClass().getSimpleName());
            if (c instanceof MapCoordinate) {
                Gdx.app.log("components: ", ((MapCoordinate) c).x + "," + ((MapCoordinate) c).y);
            }
            if (c instanceof EntityType) {
                Gdx.app.log("components: ", ((EntityType) c).type.toString());
            }
        }
    }
}
