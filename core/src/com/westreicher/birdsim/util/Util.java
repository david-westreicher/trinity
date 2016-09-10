package com.westreicher.birdsim.util;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;
import com.westreicher.birdsim.artemis.components.EntityTypeComponent;
import com.westreicher.birdsim.artemis.components.MapCoordinateComponent;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
            if (c instanceof MapCoordinateComponent) {
                Gdx.app.log("components: ", ((MapCoordinateComponent) c).x + "," + ((MapCoordinateComponent) c).y);
            }
            if (c instanceof EntityTypeComponent) {
                Gdx.app.log("components: ", ((EntityTypeComponent) c).type.toString());
            }
        }
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
