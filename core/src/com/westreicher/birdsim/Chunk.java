package com.westreicher.birdsim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.westreicher.birdsim.util.MaxArray;
import com.westreicher.birdsim.util.SimplexNoise;

import java.util.Random;

/**
 * Created by david on 8/24/15.
 */
public class Chunk {

    private enum Tiles {
        SAND(0.929f, 0.788f, 0.686f),
        GRASS(0.271f, 0.545f, 0f),
        MOUNTAIN(0.612f, 0.667f, 0.776f),
        SNOW(0.9f, 0.9f, 0.9f),
        WATER(0.251f, 0.643f, 0.875f),
        DARKWATER(0.1f, 0.3f, 0.4f),
        REALLYDARKWATER(0.05f, 0.1f, 0.2f);

        private final float[] col;

        Tiles(float r, float g, float b) {
            this.col = new float[]{r, g, b};
        }
    }

    ;

    private static final double NOISE_SCALE = 0.5;
    private static int NOISE_OCTAVES = 9;
    private static final int SIZE = Config.TILES_PER_CHUNK;
    public static final float THRESHOLD = Config.DEBUG ? -10f : 0.55f;
    private float[][] map = new float[SIZE][SIZE];
    public Mesh m;
    private Vector3 translation = new Vector3();
    public boolean isReady = false;
    private final MaxArray.MaxArrayFloat verts = new MaxArray.MaxArrayFloat(getMaxVerts() * (3 + 3));
    private static final Vector3 tmp = new Vector3();
    private Random rand = new Random();
    public boolean shouldDraw;
    private int absx;
    private int absy;

    public Chunk() {
        m = new Mesh(false, verts.maxSize(), 0,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 3, ShaderProgram.COLOR_ATTRIBUTE));
    }

    private int getMaxVerts() {
        return (int) Math.pow(Config.TILES_PER_CHUNK, 2) * 4;
    }

    public float getNoise(float x, float y, float octave) {
        double mul = Math.pow(octave, 2);
        float noise = (float) (mul * SimplexNoise.noise(x * NOISE_SCALE / mul, y * NOISE_SCALE / mul));
        return noise;
    }

    public float getNoise(float x, float y) {
        float noise = 0;
        for (int i = 10; i > 10 - NOISE_OCTAVES; i--)
            noise += getNoise(x, y, i);
        return noise / ((float) Math.pow(10, 2));
    }

    public void setPos(int absx, int absy) {
        rand.setSeed(getSeed(absx, absy));
        for (int x = 0; x < SIZE; x += 2)
            for (int y = 0; y < SIZE; y += 2)
                map[x][y] = getNoise((x + absx * SIZE), (-y + absy * SIZE));
        {
            int xlast = SIZE - 1;
            for (int y = 0; y < SIZE; y++)
                map[xlast][y] = getNoise((xlast + absx * SIZE), (-y + absy * SIZE));
        }
        {
            int ylast = SIZE - 1;
            for (int x = 0; x < SIZE; x++)
                map[x][ylast] = getNoise((x + absx * SIZE), (-ylast + absy * SIZE));
        }
        for (int x = 0; x < SIZE; x += 2)
            for (int y = 1; y < SIZE - 1; y += 2)
                map[x][y] = (map[x][y - 1] + map[x][y + 1]) / 2f;
        for (int x = 1; x < SIZE - 1; x += 2)
            for (int y = 0; y < SIZE; y += 2)
                map[x][y] = (map[x - 1][y] + map[x + 1][y]) / 2f;
        for (int x = 1; x < SIZE - 1; x += 2)
            for (int y = 1; y < SIZE; y += 2)
                map[x][y] = (map[x - 1][y] + map[x + 1][y]) / 2f;
        isReady = false;
        this.absx = absx;
        this.absy = absy;
    }

    private long getSeed(long x, long y) {
        long p = 0;
        if (y * y >= x * x) {
            p = 4 * y * y - y - x;
            if (y < x)
                p -= 2 * (y - x);
        } else {
            p = 4 * x * x - y - x;
            if (y < x)
                p += 2 * (y - x);
        }
        return p;
    }

    public boolean genMesh() {
        verts.reset();
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                float scale = map[x][y];
                float z = Math.max(0, scale * 20);
                tmp.set(getCol(scale));
                float dark = 0;
                if (scale > 0)
                    for (int x1 = x - 2; x1 <= x; x1++) {
                        for (int y1 = y - 2; y1 <= y; y1++) {
                            float diff = ((x1 < 0 || y1 < 0) ? getNoise((x1 + absx * SIZE), (-y1 + absy * SIZE)) : map[x1][y1]) - scale;
                            if (diff > 0)
                                dark += diff * 0.6f;
                        }
                    }
                else
                    dark = -scale / 4f;
                tmp.scl(1 - dark);
                verts.add(x + 0.5f, -y + 0.5f, z);
                verts.add(tmp.x, tmp.y, tmp.z);
            }
        }
        shouldDraw = verts.size() > 0;
        if (shouldDraw)
            m.setVertices(verts.arr, 0, verts.size());
        isReady = true;
        return shouldDraw;
    }

    private float[] getCol(float scale) {
        if (scale < -1.4)
            return Tiles.REALLYDARKWATER.col;
        if (scale < -0.9)
            return Tiles.DARKWATER.col;
        else if (scale < 0)
            return Tiles.WATER.col;
        else if (scale < 0.2)
            return Tiles.SAND.col;
        else if (scale < 0.9)
            return Tiles.GRASS.col;
        else if (scale < 1.4)
            return Tiles.MOUNTAIN.col;
        else
            return Tiles.SNOW.col;
    }

    public void explode(Vector3 cam, int explodedist) {
        //Gdx.app.log("expl", modelinstance.transform.toString());
        boolean inside = false;
        for (int x = -explodedist; x <= explodedist; x += explodedist)
            for (int y = -explodedist; y <= explodedist; y += explodedist) {
                if (getVal(cam, x, y) != -1f)
                    inside = true;
            }
        if (!inside)
            return;
        isReady = false;

        for (int x = -explodedist; x <= explodedist; x++)
            for (int y = -explodedist; y <= explodedist; y++) {
                float dst = Vector2.dst(0, 0, x, y);
                if (dst > explodedist - 1)
                    continue;
                float val = getVal(cam, x, y);
                if (val != -1f)
                    setVal(cam, x, y, Math.min(1, Math.max(val + ((explodedist - 1) / dst) / 100, 0)));
                // setVal(cam, x, y, val - 0.2f);
            }
        //Gdx.app.log("expl", cam.toString() + "," + offsetx + "," + offsety);
        //Gdx.app.log("expl", cam.toString() + "," + modelinstance.transform.getTranslation(new Vector3()).toString());
        //regenerateMesh();
    }

    private float getVal(Vector3 cam, float x, float y) {
        int realx = (int) (cam.x + SIZE / 2 + x - translation.x);
        int realy = (int) (-cam.y + SIZE / 2 + y + translation.y + 1);
        if (realx >= 0 && realx < SIZE && realy >= 0 && realy < SIZE)
            return map[realx][realy];
        return -1f;
    }

    private void setVal(Vector3 cam, float x, float y, float val) {
        int realx = (int) (cam.x + SIZE / 2 + x - translation.x);
        int realy = (int) (-cam.y + SIZE / 2 + y + translation.y + 1);
        if (realx >= 0 && realx < SIZE && realy >= 0 && realy < SIZE)
            map[realx][realy] = val;
    }

    public static float getVals(Chunk chunks[][], Vector3 cam, float x, float y) {
        for (Chunk mis[] : chunks)
            for (Chunk mi : mis) {
                if (mi == null)
                    continue;
                float val = mi.getVal(cam, x, y);
                if (val > 0)
                    return val;
            }
        return -1;
    }

    public void setTranslation(float realX, float realY) {
        translation.set(realX, realY, 0);
    }

    public void dispose() {
        m.dispose();
    }

}
