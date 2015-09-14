package com.westreicher.birdsim.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 9/14/15.
 */
public class MaxArray {
    public int pointer;
    private int maxSize;

    public MaxArray(int maxsize) {
        maxSize = maxsize;
    }

    public void reset() {
        pointer = 0;
    }


    public int size() {
        return pointer;
    }

    public int maxSize() {
        return maxSize;
    }

    public static class MaxArrayFloat extends MaxArray {
        public final float[] arr;

        public MaxArrayFloat(int maxsize) {
            super(maxsize);
            arr = new float[maxsize];
        }

        public void add(float... xs) {
            for (int i = 0; i < xs.length; i++)
                arr[pointer++] = xs[i];
        }

    }

    public static class MaxArrayShort extends MaxArray {
        public final short[] arr;

        public MaxArrayShort(int maxsize) {
            super(maxsize);
            arr = new short[maxsize];
        }

        public void add(short... xs) {
            for (int i = 0; i < xs.length; i++)
                arr[pointer++] = xs[i];
        }

    }
}
