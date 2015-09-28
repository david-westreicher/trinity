package com.westreicher.birdsim.util;

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

        public void add(float x1) {
            arr[pointer++] = x1;
        }

        public void add(float x1, float x2) {
            arr[pointer++] = x1;
            arr[pointer++] = x2;
        }

        public void add(float x1, float x2, float x3) {
            arr[pointer++] = x1;
            arr[pointer++] = x2;
            arr[pointer++] = x3;
        }

    }

    public static class MaxArrayShort extends MaxArray {
        public final short[] arr;

        public MaxArrayShort(int maxsize) {
            super(maxsize);
            arr = new short[maxsize];
        }

        public void add(short x1) {
            arr[pointer++] = x1;
        }

        public void add(short x1, short x2) {
            arr[pointer++] = x1;
            arr[pointer++] = x2;
        }

        public void add(short x1, short x2, short x3) {
            arr[pointer++] = x1;
            arr[pointer++] = x2;
            arr[pointer++] = x3;
        }

    }
}
