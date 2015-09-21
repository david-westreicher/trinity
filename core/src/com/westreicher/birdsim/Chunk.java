package com.westreicher.birdsim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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
    private static int NOISE_OCTAVES = 10;
    private static final int SIZE = Config.TILES_PER_CHUNK;
    private float[][] map = new float[SIZE][SIZE];
    public Mesh m;
    public boolean isReady = false;
    public final MaxArray.MaxArrayFloat verts = new MaxArray.MaxArrayFloat((int) Math.pow(Config.TILES_PER_CHUNK, 2) * 2);
    private static final Vector3 tmp = new Vector3();
    public Random rand = new Random();
    public boolean shouldDraw;
    private int absx;
    private int absy;
    private float randdark;

    public Chunk() {
        m = new Mesh(false, verts.maxSize(), 0,
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 3, ShaderProgram.COLOR_ATTRIBUTE));
    }

    public float getNoise(float x, float y, float octave) {
        double mul = Math.pow(octave, 2);
        float noise = (float) (mul * SimplexNoise.noise(x * NOISE_SCALE / mul, y * NOISE_SCALE / mul));
        return noise;
    }

    public float getNoise(float x, float y) {
        float noise = 0;
        for (int i = 10; i > 10 - NOISE_OCTAVES; i -= 2)
            noise += getNoise(x, y, i);
        return noise / ((float) Math.pow(8, 2));
    }

    public void clear() {
        for (int x = 0; x < SIZE; x += 1)
            for (int y = 0; y < SIZE; y += 1)
                map[x][y] = 0;
        isReady = false;
    }

    public float getVal(int x, int y) {
        return Math.max(0, map[x][y]);
    }

    public void mulVal(int x, int y, float percent) {
        map[x][y] *= percent;
        isReady = false;
    }

    public void addVal(int x, int y, float val) {
        map[x][y] += val;
        isReady = false;
    }


    public void setPos(int absx, int absy) {
        rand.setSeed(getSeed(absx, absy));
        randdark = (float) rand.nextDouble();
        randdark = (float) rand.nextDouble();
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
        shouldDraw = false;
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
        ChunkManager chunkman = MyGdxGame.single.chunkManager;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                float scale = map[x][y];
                //if (!MyGdxGame.isDesktop && scale > -1 && scale < 0)
                //    continue;
                tmp.set(getCol(scale));
                if (scale > 0) {
                    float dark = 0;//randdark;
                    for (int x1 = x - 2; x1 <= x; x1++) {
                        for (int y1 = y - 2; y1 <= y; y1++) {
                            float diff = -scale;
                            if (x1 < 0 || y1 < 0) {
                                //TODO fetch from neighbouring chunk
                                float neighborval = chunkman.getValAbs(x1, SIZE - y1 - 1, absx, absy);
                                if (neighborval == ChunkManager.OUTSIDE)
                                    diff += getNoise((x1 + absx * SIZE), (-y1 + absy * SIZE));
                                else
                                    diff += neighborval;
                                //MyGdxGame.single.chunkManager.setValRel(absx * SIZE - SIZE / 2, absy * SIZE - SIZE / 2, 2);
                            } else
                                diff += map[x1][y1];
                            if (diff > 0)
                                dark += diff * 0.6f;
                        }
                    }
                    tmp.scl(Math.max(0, 1 - dark));
                } else
                    tmp.scl(Math.min(1, 0.5f - scale * 0.25f));
                float z = Math.min(1, Math.max(0, scale * (1.0f / 2.5f)));
                // x,y,z should be in range 0-1 (min-max)
                //TODO could encode occlusion + colormap uv in alpha
                verts.add(Color.toFloatBits((float) x / SIZE, (float) (SIZE - y - 1) / SIZE, z, 1f));
                verts.add(Color.toFloatBits(tmp.x, tmp.y, tmp.z, 1f));
            }
        }
        shouldDraw = verts.size() > 0;
        if (shouldDraw)
            m.setVertices(verts.arr, 0, verts.size());
        isReady = true;
        return shouldDraw;
    }

    private float[] getCol(float scale) {
        /*if (scale < -1.4)
            return Tiles.REALLYDARKWATER.col;
        if (scale < -0.9)
            return Tiles.DARKWATER.col;
        else*/
        if (scale <= 0)
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

    public void dispose() {
        m.dispose();
    }

}
