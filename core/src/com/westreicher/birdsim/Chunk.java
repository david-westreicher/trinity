package com.westreicher.birdsim;

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

    public static final float NOISE_SCALE = 0.035f;
    public static final float THRESHOLD = MyGdxGame.DEBUG ? 0f : 0.55f;
    private final float[][] map;
    private final Vector3[][] col;
    private final int w;
    private final int h;
    public Mesh m;
    private int size = 1;
    private Vector3 translation = new Vector3();
    private boolean dirtyflag = false;
    private MaxArray.MaxArrayFloat verts = new MaxArray.MaxArrayFloat(getMaxVerts() * (3 + 3));
    private MaxArray.MaxArrayShort inds = new MaxArray.MaxArrayShort(getMaxInds());
    private static final Vector3 tmp = new Vector3();

    private int getMaxVerts() {
        return (int) Math.pow(MyGdxGame.SIZE, 2) * 4;
    }

    private int getMaxInds() {
        return (int) Math.pow(MyGdxGame.SIZE, 2) * 6;
    }

    public Chunk(int w, int h, int absx, int absy) {
        Random r = new Random((absx + "," + absy).hashCode());
        this.w = w;
        this.h = h;
        map = new float[w][h];
        if (MyGdxGame.DEBUG)
            for (int x = 0; x < w; x++)
                for (int y = 0; y < h; y++)
                    map[x][y] = (float) SimplexNoise.noise((x + absx * w) * NOISE_SCALE, (-y + absy * h) * NOISE_SCALE) / 2.0f + 0.5f;

        col = new Vector3[w][h];
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                col[x][y] = new Vector3(r.nextFloat(), r.nextFloat(), r.nextFloat());
        genMesh();
    }

    private void genMesh() {
        if (m == null) {
            m = new Mesh(false, verts.maxSize(), getMaxInds(),
                    new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                    new VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 3, ShaderProgram.COLOR_ATTRIBUTE));
        }
        verts.reset();
        inds.reset();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (map[x][y] <= THRESHOLD)
                    continue;
                float scale = ((map[x][y] - THRESHOLD) * 1f + 0.1f);
                float z = scale * 7;//0;//(float) Math.random();
                short startIndex = (short) (verts.pointer / 6);
                tmp.set(col[x][y]);
                tmp.scl(scale);
                verts.add(x, -y, z);
                verts.add(tmp.x, tmp.y, tmp.z);
                verts.add(x + size, -y, z);
                verts.add(tmp.x, tmp.y, tmp.z);
                verts.add(x, -y + size, z);
                verts.add(tmp.x, tmp.y, tmp.z);
                verts.add(x + size, -y + size, z);
                verts.add(tmp.x, tmp.y, tmp.z);
                inds.add(startIndex, (short) (startIndex + 1), (short) (startIndex + 2), (short) (startIndex + 2), (short) (startIndex + 3), (short) (startIndex + 1));
            }
        }
        m.setVertices(verts.arr, 0, verts.size());
        m.setIndices(inds.arr, 0, inds.size());
    }

    public boolean regenerateMesh() {
        if (!dirtyflag)
            return false;
        genMesh();
        dirtyflag = false;
        return true;
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
        dirtyflag = true;

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
        int realx = (int) (cam.x + w / 2 + x - translation.x);
        int realy = (int) (-cam.y + h / 2 + y + translation.y + 1);
        if (realx >= 0 && realx < w && realy >= 0 && realy < h)
            return map[realx][realy];
        return -1f;
    }

    private void setVal(Vector3 cam, float x, float y, float val) {
        int realx = (int) (cam.x + w / 2 + x - translation.x);
        int realy = (int) (-cam.y + h / 2 + y + translation.y + 1);
        if (realx >= 0 && realx < w && realy >= 0 && realy < h)
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
