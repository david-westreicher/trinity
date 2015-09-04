package com.westreicher.birdsim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Matrix4;
import com.westreicher.birdsim.util.SimplexNoise;

import java.util.Random;

/**
 * Created by david on 8/24/15.
 */
public class Chunk {

    private static final float NOISE_SCALE = 0.04f;
    public static final float THRESHOLD = MyGdxGame.DEBUG ? 0 : 0.55f;
    private final float[][] map;
    private final int w;
    private final int h;
    public final int absx;
    public final int absy;
    private Random r;
    private int size = 1;
    public ModelInstance modelinstance;
    private Vector3 translation = new Vector3();
    private boolean dirtyflag = true;

    public Chunk(int w, int h, int offsetx, int offsety, int absx, int absy) {
        this.r = new Random((absx + "," + absy).hashCode());
        this.w = w;
        this.h = h;
        this.absx = absx;
        this.absy = absy;
        map = new float[w][h];
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                map[x][y] = (float) SimplexNoise.noise((x + absx * w) * NOISE_SCALE, (-y + absy * h) * NOISE_SCALE) / 2.0f + 0.5f;
        modelinstance = new ModelInstance(generateMesh(), w * (offsetx - 1), h * (offsety - 1), 0);
    }

    public Model generateMesh() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        modelBuilder.node().translation.set(-w / 2.0f, h / 2.0f, 0);
        MeshPartBuilder meshBuilder;
        Material mat;
        if (MyGdxGame.DEBUG)
            mat = new Material(ColorAttribute.createDiffuse(r.nextFloat(), r.nextFloat(), r.nextFloat(), 1));
        else
            mat = new Material(ColorAttribute.createDiffuse(1, 1, 1, 1));
        meshBuilder = modelBuilder.part("part1", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked, mat);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                boolean isEnemy = false;
                Color col;
                if (MyGdxGame.DEBUG || map[x][y] > 2) {
                    col = new Color(1, 1, 1, 1);
                    //isEnemy = true;
                    //Gdx.app.log("name", map[x][y] + "");
                    //map[x][y] -= 2;
                    for (int i = 0; i < 3; i++)
                        r.nextFloat();
                } else
                    col = new Color(r.nextFloat(), r.nextFloat(), r.nextFloat(), 1);
                if (map[x][y] > THRESHOLD || isEnemy) {
                    float scale = isEnemy ? 1 : ((map[x][y] - THRESHOLD) * 2f + 0.1f);
                    col.r *= scale;
                    col.g *= scale;
                    col.b *= scale;
                    //.app.log("game", scale+"");
                    float z = isEnemy ? 2 : scale * 7;//0;//(float) Math.random();
                    short lu = meshBuilder.vertex(new Vector3(x, -y, z), null, col, null);
                    short ru = meshBuilder.vertex(new Vector3(x + size, -y, z), null, col, null);
                    short ld = meshBuilder.vertex(new Vector3(x, -y + size, z), null, col, null);
                    short rd = meshBuilder.vertex(new Vector3(x + size, -y + size, z), null, col, null);
                    meshBuilder.rect(lu, ru, rd, ld);
                }
            }
        }
        return modelBuilder.end();
    }

    public void regenerateMesh() {
        if (!dirtyflag)
            return;
        modelinstance.model.dispose();
        Matrix4 trans = modelinstance.transform.cpy();
        r = new Random((absx + "," + absy).hashCode());
        translation = modelinstance.transform.getTranslation(translation);
        modelinstance = new ModelInstance(generateMesh());
        modelinstance.transform.set(trans);
        dirtyflag = false;
    }

    public void explode(Vector3 cam, boolean isEnemy) {
        dirtyflag = true;
        int explodedist = 5;
        //Gdx.app.log("expl", modelinstance.transform.toString());
        boolean inside = false;
        for (int x = -explodedist; x <= explodedist; x += explodedist)
            for (int y = -explodedist; y <= explodedist; y += explodedist) {
                if (getVal(cam, x, y) != -1f)
                    inside = true;
            }
        if (!inside)
            return;

        for (int x = -explodedist; x <= explodedist; x++)
            for (int y = -explodedist; y <= explodedist; y++) {
                float dst = Vector2.dst(0, 0, x, y);
                if (dst > explodedist - 1)
                    continue;
                float val = getVal(cam, x, y);
                if (isEnemy)
                    setVal(cam, x, y, 2 + val);
                else
                    setVal(cam, x, y, Math.max(val - ((explodedist - 1) / dst) / 20, 0));
                // setVal(cam, x, y, val - 0.2f);
            }
        //Gdx.app.log("expl", cam.toString() + "," + offsetx + "," + offsety);
        //Gdx.app.log("expl", cam.toString() + "," + modelinstance.transform.getTranslation(new Vector3()).toString());
        //regenerateMesh();
    }

    private float getVal(Vector3 cam, float x, float y) {
        int realx = (int) (cam.x + w / 2 + x - translation.x);
        int realy = (int) (-cam.y + h / 2 + y + translation.y);
        if (realx >= 0 && realx < w && realy >= 0 && realy < h)
            return map[realx][realy];
        return -1f;
    }

    private void setVal(Vector3 cam, float x, float y, float val) {
        int realx = (int) (cam.x + w / 2 + x - translation.x);
        int realy = (int) (-cam.y + h / 2 + y + translation.y);
        if (realx >= 0 && realx < w && realy >= 0 && realy < h)
            map[realx][realy] = val;
    }

    public static float getVals(Chunk chunks[][], Vector3 cam, float x, float y) {
        for (Chunk mis[] : chunks)
            for (Chunk mi : mis) {
                mi.translation = mi.modelinstance.transform.getTranslation(mi.translation);
                float val = mi.getVal(cam, x, y);
                if (val > 0)
                    return val;
            }
        return -1;
    }
}
