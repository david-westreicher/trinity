package com.westreicher.birdsim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.westreicher.birdsim.util.MaxArray;
import com.westreicher.birdsim.util.SimplexNoise;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by david on 8/24/15.
 */
public class Chunk {

    private static final Vector3 LIGHT_VEC = new Vector3(-1, 1, -1).nor();
    private State state;

    private static final State[] states = State.values();

    public boolean shouldDraw() {
        return shoulddraw;
        //return state == State.MESH;
    }

    public boolean hasData() {
        return state.ordinal() >= State.HEIGHT.ordinal();
    }

    private enum State {INIT, HEIGHT, SHADOW, MESH}

    public boolean stateAdvance(Chunk[] neighbs, ChunkManager cm) {
        int thisstate = state.ordinal();
        //already last state
        if (thisstate == states.length - 1) return false;
        if (state.ordinal() >= state.HEIGHT.ordinal())
            for (int i = 0; i < neighbs.length; i++) {
                Chunk otherc = neighbs[i];
                int otherstatei = otherc.state.ordinal();
                //can't advance because of neighbor
                if (otherstatei < thisstate) return false;
            }
        //advance state
        State nextstate = states[(thisstate + 1) % states.length];
        switch (nextstate) {
            case INIT:
                //reset();
                throw new RuntimeException("should never be in INIT after advance");
            case HEIGHT:
                calcNoise();
                break;
            case SHADOW:
                calcShadow(cm);
                break;
            case MESH:
                renderStyle.genMesh(cm, map, colors);
                renderStyle.setMesh(m);
                shoulddraw = true;
                break;
        }
        //Gdx.app.log("chunk", absx + "," + absy + ": " + state + "->" + nextstate);
        state = nextstate;
        return true;
    }

    private void calcNoise() {
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
    }

    /**
     * TODO
     * use state machine for chunk states (INIT, HEIGHT, COLOR, SHADOW, MESH)
     * a chunk can only advance to the next state if its (dependent) neighbours are in the same state
     * INIT: no data at all, don't draw this
     * HEIGHT: the noise function was applied and saved its height data in the map[][]
     * COLOR: the color was calculated and saved in the colors[][]
     * SHADOW: the shadow was calculated and saved in the shadows[][]
     * MESH: the vertex data (height+color+shadow) was synced to the GPU
     * <p/>
     * the state will be resetted: * to INIT if the chunk will be reused at the border
     * * to HEIGHT if the height data was changed (explosion, building, ...)
     * ? should the state be resetted for dependent neighbours too ?
     */
    private enum Tiles {
        SAND(0.929f, 0.788f, 0.686f),
        GRASS(0.271f, 0.545f, 0f),
        MOUNTAIN(0.5f, 0.5f, 0.6f),
        SNOW(0.9f, 0.9f, 0.9f),
        WATER(0.251f, 0.643f, 0.875f),
        DARKWATER(0.1f, 0.3f, 0.4f),
        REALLYDARKWATER(0.05f, 0.1f, 0.2f);

        private final float[] col;

        Tiles(float r, float g, float b) {
            this.col = new float[]{r, g, b};
        }
    }

    private static final double NOISE_SCALE = 0.8;
    private static int NOISE_OCTAVES = 10;
    private static final int SIZE = Config.TILES_PER_CHUNK;
    private float[][] map = new float[SIZE][SIZE];
    public Color[][] colors = new Color[SIZE][SIZE];
    public Mesh m;
    private static final Vector3 tmp = new Vector3();
    private static final Vector3 tmp2 = new Vector3();
    public Random rand = new Random();
    public long absx;
    public long absy;
    private float randdark;
    private boolean shoulddraw;
    public final ChunkRenderStyle renderStyle;

    public Chunk() {
        switch (Config.CHUNK_RENDER_STYLE) {
            case MINECRAFT:
                renderStyle = new MineCraftStyle();
                break;
            case SPRITE:
                renderStyle = new SpriteStyle();
                break;
            default:
            case TERRAIN:
                renderStyle = new TerrainStyle();
                break;
        }
        //TODO use VertexBufferObjectSubData
        m = new Mesh(Mesh.VertexDataType.VertexBufferObjectSubData, false, renderStyle.vertNum(), renderStyle.indsNum(),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, Config.ALLOW_NEGATIVE_CHUNK_DATA ? 4 : 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 3, ShaderProgram.COLOR_ATTRIBUTE));
        for (int x = 0; x < SIZE; x++)
            for (int y = 0; y < SIZE; y++)
                colors[x][y] = new Color();
    }

    public float getNoise(double x, double y, int octave) {
        double mul = Math.pow(octave, 2);
        float noise = (float) (mul * SimplexNoise.noise(x * NOISE_SCALE / mul, y * NOISE_SCALE / mul));
        return noise;
    }

    public float getNoise(double x, double y) {
        float noise = 0;
        for (int i = 10; i > 10 - NOISE_OCTAVES; i -= 2)
            noise += getNoise(x, y, i);
        return noise / ((float) Math.pow(8, 2));
        //return 0;
    }

    public void clear() {
        for (int x = 0; x < SIZE; x += 1)
            for (int y = 0; y < SIZE; y += 1)
                map[x][y] = 0;
        state = State.INIT;
    }

    public float getVal(int x, int y) {
        if (state == State.INIT) {
            throw new RuntimeException("has no data yet");
        }
        return Math.max(0, map[x][y]);
    }

    public void mulVal(int x, int y, float percent) {
        map[x][y] *= percent;
        if (state == State.INIT) {
            throw new RuntimeException("has no data yet");
        }
        state = State.HEIGHT;
    }

    public void addVal(int x, int y, float val) {
        map[x][y] += val;
        if (state == State.INIT) {
            throw new RuntimeException("has no data yet");
        }
        state = State.HEIGHT;
    }


    public void resetPos(long absx, long absy) {
        rand.setSeed(getSeed(absx, absy));
        randdark = (float) rand.nextDouble();
        randdark = (float) rand.nextDouble();
        this.absx = absx;
        this.absy = absy;
        state = State.INIT;
        shoulddraw = false;
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

    private float getOutsideVal(ChunkManager cm, int x, int y) {
        if (x < 0 || y < 0 || x >= SIZE || y >= SIZE) {
            float neighborval = cm.getValAbs(x, SIZE - y - 1, absx, absy);
            if (neighborval == ChunkManager.OUTSIDE)
                throw new RuntimeException("OUTSIDE");
            return neighborval;
        }
        return map[x][y];
    }

    private void calcShadow(ChunkManager cm) {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                float scale = map[x][y];
                tmp.set(getCol(scale));
                if (scale > 0) {
                    float gradx = (getOutsideVal(cm, x - 1, y) - getOutsideVal(cm, x + 1, y)) / 2.0f;
                    float grady = (getOutsideVal(cm, x, y - 1) - getOutsideVal(cm, x, y + 1)) / 2.0f;
                    tmp2.set(gradx, -grady, -1.0f / Config.TERRAIN_HEIGHT);
                    tmp2.nor();
                    float dot = Math.max(0.2f, tmp2.dot(LIGHT_VEC));
                    tmp.scl(dot);
                }
                float dark = 0;
                for (int x1 = x - 3; x1 <= x; x1++) {
                    for (int y1 = y - 3; y1 <= y; y1++) {
                        float other = getOutsideVal(cm, x1, y1);
                        float diff = other - scale;
                        if (other > 0 && diff > 0)
                            dark += diff * 0.2f;
                    }
                }
                tmp.scl(Math.max(0, 1 - dark));
                if (scale <= 0)
                    tmp.scl(Math.min(1, 0.5f - scale * 0.25f));
                colors[x][y].set(tmp.x, tmp.y, tmp.z, 1);
            }
        }
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

    public enum Renderstyle {SPRITE, TERRAIN, MINECRAFT}

    public interface ChunkRenderStyle {
        void genMesh(ChunkManager chunkman, float[][] map, Color[][] cols);

        int indsNum();

        int vertNum();

        void setMesh(Mesh m);

        int getType();

        MaxArray.MaxArrayFloat getVerts();
    }

    public static class SpriteStyle implements ChunkRenderStyle {
        public final MaxArray.MaxArrayFloat verts = new MaxArray.MaxArrayFloat((int) Math.pow(Config.TILES_PER_CHUNK, 2) * 2);

        @Override
        public void genMesh(ChunkManager chunkman, float[][] map, Color[][] cols) {
            verts.reset();
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    Color col = cols[x][y];
                    float z = Math.min(1, Math.max(0, map[x][y] * (1.0f / 2.5f)));
                    // x,y,z should be in range 0-1 (min-max)
                    //TODO could encode occlusion + colormap uv in alpha
                    verts.add(Color.toFloatBits((float) x / SIZE, (float) (SIZE - y - 1) / SIZE, z, 1f));
                    verts.add(Color.toFloatBits(col.r, col.g, col.b, 1f));
                }
            }
        }

        @Override
        public int indsNum() {
            return 0;
        }

        @Override
        public int vertNum() {
            return verts.maxSize();
        }

        @Override
        public void setMesh(Mesh m) {
            m.setVertices(verts.arr, 0, verts.size());
        }

        @Override
        public int getType() {
            return GL20.GL_POINTS;
        }

        @Override
        public MaxArray.MaxArrayFloat getVerts() {
            return verts;
        }
    }

    public class TerrainStyle implements ChunkRenderStyle {
        public final MaxArray.MaxArrayFloat verts = new MaxArray.MaxArrayFloat((int) Math.pow(SIZE + 1, 2) * 2);
        public final MaxArray.MaxArrayShort inds = new MaxArray.MaxArrayShort(SIZE * (SIZE * 2 + 2));
        private boolean shouldSetIndices = true;

        @Override
        public void genMesh(ChunkManager chunkman, float[][] map, Color[][] cols) {
            verts.reset();
            // add vertices
            for (int x = 0; x <= SIZE; x++) {
                for (int y = 0; y <= SIZE; y++) {
                    Color col;
                    float val;
                    if (x < SIZE && y < SIZE) {
                        col = cols[x][y];
                        val = map[x][y];
                    } else {
                        ChunkManager.TileResult tr = chunkman.getValAbs2(x, SIZE - y - 1, absx, absy);
                        if (tr == null) throw new RuntimeException("OUTSIDE");
                        val = tr.c.map[tr.innerx][tr.innery];
                        col = tr.c.colors[tr.innerx][tr.innery];
                    }
                    float normval = val * (1.0f / 2.5f);

                    float z = Math.min(1, Math.max(0, normval));
                    float zneg = Math.min(1, Math.max(0, -normval));

                    verts.add(Color.toFloatBits((float) x / SIZE, (float) (SIZE - y) / SIZE, z, zneg));
                    verts.add(Color.toFloatBits(col.r, col.g, col.b, 1f));
                }
            }
        }

        @Override
        public int indsNum() {
            return inds.maxSize();
        }

        @Override
        public int vertNum() {
            return verts.maxSize();
        }

        @Override
        public void setMesh(Mesh m) {
            m.setVertices(verts.arr, 0, verts.size());
            //TODO use update Vertices!!!!
            //m.updateVertices()
            if (shouldSetIndices) {
                createIndices();
                m.setIndices(inds.arr, 0, inds.size());
                shouldSetIndices = false;
            }
        }

        private void createIndices() {
            /* indices of vertices with SIZE=4
                0 - 4 - 8 - ....
                |   |   |
                1 - 5 - 9 - ....
                |   |   |
                2 - 6 - 10- ....
                |   |   |
                3 - 7 - 11- ....

               use one TRIANGLE_STRIP for efficient encoding
                0 ->4 ->8 - ....
                | / | \ |
                1 ->5 ->9 - ....
                | / | \ |
                2 ->6 ->10- ....
                | / | \ |
                3 ->7 ->11- ....

               thus we get the following indices buffer:
               0,4,1,5,2,6,3,7,7,11,6,10,5,9,4,8 */
            int STITCH_SIZE = SIZE + 1;
            // start with 0,8,16,24,... because we run down and then up again
            for (int start = 0; start <= STITCH_SIZE * (STITCH_SIZE - 2); ) {
                // run down: (0,4),(1,5),(2,6),(3,7)
                for (int y = start; y < start + STITCH_SIZE; y++)
                    inds.add((short) y, (short) (y + STITCH_SIZE));
                start += STITCH_SIZE;
                if (start / STITCH_SIZE >= STITCH_SIZE - 1) break;
                // run up: (7,11),(6,10),(5,9),(4,8)
                for (int y = start + STITCH_SIZE - 1; y >= start; y--)
                    inds.add((short) y, (short) (y + STITCH_SIZE));
                start += STITCH_SIZE;
            }
        }

        @Override
        public int getType() {
            //return GL20.GL_LINE_STRIP;
            return GL20.GL_TRIANGLE_STRIP;
        }

        @Override
        public MaxArray.MaxArrayFloat getVerts() {
            return verts;
        }
    }

    public static class MineCraftStyle implements ChunkRenderStyle {
        public final MaxArray.MaxArrayFloat verts = new MaxArray.MaxArrayFloat((int) Math.pow(Config.TILES_PER_CHUNK, 2) * 8);
        public final MaxArray.MaxArrayShort inds = new MaxArray.MaxArrayShort((int) Math.pow(Config.TILES_PER_CHUNK, 2) * 24);

        @Override
        public void genMesh(ChunkManager chunkman, float[][] map, Color[][] cols) {
            verts.reset();
            inds.reset();
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    Color col = cols[x][y];
                    float z = Math.min(1, Math.max(0, map[x][y] * (1.0f / 2.5f)));

                    short currentinds = (short) (verts.size() / 2);
                    // x,y,z should be in range 0-1 (min-max)
                    //TODO could encode occlusion + colormap uv in alpha
                    float color = Color.toFloatBits(col.r, col.g, col.b, 1f);
                    /*

                       0 - 1
                      / x /|
                     2 - 3 8 - 9
                    / x /|/ x /
                   4 - 5 10 -11
                  / x /|/ x /
                 6 - 7 12 -13
                     |/ x /
                     14 -15

                     */
                    verts.add(Color.toFloatBits((float) x / SIZE, (float) (SIZE - y - 1) / SIZE, z, 1f));
                    verts.add(color);
                    verts.add(Color.toFloatBits((float) (x + 1) / SIZE, (float) (SIZE - y - 1) / SIZE, z, 1f));
                    verts.add(color);
                    verts.add(Color.toFloatBits((float) (x + 1) / SIZE, (float) (SIZE - y) / SIZE, z, 1f));
                    verts.add(color);
                    verts.add(Color.toFloatBits((float) x / SIZE, (float) (SIZE - y) / SIZE, z, 1f));
                    verts.add(color);
                    inds.add((short) (currentinds + 0), (short) (currentinds + 1), (short) (currentinds + 2));
                    inds.add((short) (currentinds + 0), (short) (currentinds + 2), (short) (currentinds + 3));
                    if (y > 0) {
                        inds.add((short) (currentinds + 3), (short) (currentinds + 2), (short) (currentinds - 4));
                        inds.add((short) (currentinds - 4), (short) (currentinds - 3), (short) (currentinds + 2));
                    }
                    if (x > 0) {
                        int leftindex = currentinds - 4 * (Config.TILES_PER_CHUNK);
                        inds.add((short) (currentinds + 0), (short) (currentinds + 3), (short) (leftindex + 1));
                        inds.add((short) (leftindex + 1), (short) (leftindex + 2), (short) (currentinds + 3));
                    }
                }
            }
        }

        @Override
        public int indsNum() {
            return inds.maxSize();
        }

        @Override
        public int vertNum() {
            return verts.maxSize();
        }

        @Override
        public void setMesh(Mesh m) {
            m.setVertices(verts.arr, 0, verts.size());
            m.setIndices(inds.arr, 0, inds.size());
        }

        @Override
        public int getType() {
            return GL20.GL_TRIANGLES;
        }

        @Override
        public MaxArray.MaxArrayFloat getVerts() {
            return verts;
        }
    }
}
