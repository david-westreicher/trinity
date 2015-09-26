package com.westreicher.birdsim.util;

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
}
