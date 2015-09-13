package com.westreicher.birdsim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.westreicher.birdsim.util.SimplexNoise;
import com.westreicher.birdsim.util.Spiral;

/**
 * Created by david on 9/4/15.
 */
public class ChunkManager {
    private static final Vector3 GRADIENT = new Vector3(1, 0, 0);
    public static final int CHUNKNUMS = 6 + 1;
    private long lastupdate = 0;
    private Chunk chunks[][] = new Chunk[CHUNKNUMS][CHUNKNUMS];
    private int[] pos = new int[]{0, 0};
    private Spiral s = new Spiral();

    public ChunkManager() {
        //for (int x = 0; x < 3; x++)
        //for (int y = 0; y < 3; y++)
        // chunks[CHUNKNUMS / 2 - 1][CHUNKNUMS / 2 - 1] = getInstance(0, 0);
        Gdx.app.log("game", "" + (CHUNKNUMS / 2));

    }

    public Chunk getInstance(int absx, int absy) {
        return new Chunk((int) MyGdxGame.SIZE, (int) MyGdxGame.SIZE, absx, absy);
    }

    public boolean isStuck(Vector3 pos, int size) {
        if (size == 0)
            return (Chunk.getVals(chunks, pos, 0, 0) > Chunk.THRESHOLD);
        for (int x = -size; x <= size; x++)
            for (int y = -size; y <= size; y++)
                if (Chunk.getVals(chunks, pos, x, y) > Chunk.THRESHOLD)
                    return true;
        return false;
    }

    public void explode(Vector3 pos, int distance) {
        for (Chunk mis[] : chunks)
            for (Chunk mi : mis)
                if (mi != null)
                    mi.explode(pos, distance);
    }

    public void render(ModelBatch mb) {
        long currentFrame = Gdx.graphics.getFrameId();
        s.reset();
        while (true) {
            Vector2 spos = s.next();
            int x = ((int) spos.x) + CHUNKNUMS / 2;
            int y = ((int) spos.y) + CHUNKNUMS / 2;
            if (x < 0 || y < 0 || x >= CHUNKNUMS || y >= CHUNKNUMS)
                break;
            Chunk mi = chunks[x][y];
            if (mi == null) {
                if (currentFrame - lastupdate > 0) {
                    chunks[x][y] = getInstance(pos[0] + (int) spos.x, pos[1] + (int) spos.y);
                    updateTranslations();
                    lastupdate = currentFrame;
                    if (x == 0 || y == 0 || y == CHUNKNUMS - 1 || x == CHUNKNUMS - 1) {
                        Entity.spawn(new Vector3((x - (CHUNKNUMS / 2)) * MyGdxGame.SIZE,
                                (y - (CHUNKNUMS / 2)) * MyGdxGame.SIZE, 0));
                    }
                }
                break;
            } else if (mi.regenerateMesh()) {
                lastupdate = currentFrame;
                break;
            }
        }
        for (int x = 0; x < CHUNKNUMS; x++) {
            for (int y = 0; y < CHUNKNUMS; y++) {
                Chunk mi = chunks[x][y];
                if (mi != null)
                    mb.render(mi.modelinstance);
            }
        }
    }

    public void dispose() {
        for (int x = 0; x < CHUNKNUMS; x++)
            for (int y = 0; y < CHUNKNUMS; y++) {
                Chunk mi = chunks[x][y];
                if (mi != null) {
                    mi.modelinstance.model.dispose();
                    chunks[x][y] = null;
                }
            }
        pos[0] = 0;
        pos[1] = 0;
    }

    public void updateDirection(int dx, int dy) {
        this.pos[0] += dx;
        this.pos[1] += dy;
        if (dx != 0) {
            Chunk newchunks[][] = new Chunk[CHUNKNUMS][CHUNKNUMS];
            for (int x = (dx > 0 ? 0 : 1); x < (dx > 0 ? CHUNKNUMS - 1 : CHUNKNUMS); x++)
                for (int y = 0; y < CHUNKNUMS; y++) {
                    newchunks[x][y] = chunks[x + (dx > 0 ? 1 : -1)][y];
                }
            for (int y = 0; y < CHUNKNUMS; y++) {
                Chunk c = chunks[dx > 0 ? 0 : CHUNKNUMS - 1][y];
                if (c != null) c.modelinstance.model.dispose();
            }
            chunks = newchunks;
        }
        if (dy != 0) {
            Chunk newchunks[][] = new Chunk[CHUNKNUMS][CHUNKNUMS];
            for (int x = 0; x < CHUNKNUMS; x++)
                for (int y = (dy > 0 ? 0 : 1); y < (dy > 0 ? CHUNKNUMS - 1 : CHUNKNUMS); y++) {
                    newchunks[x][y] = chunks[x][y + (dy > 0 ? 1 : -1)];
                }
            for (int x = 0; x < CHUNKNUMS; x++) {
                Chunk c = chunks[x][dy > 0 ? 0 : CHUNKNUMS - 1];
                if (c != null) c.modelinstance.model.dispose();
            }
            chunks = newchunks;
        }
        updateTranslations();
    }

    private void updateTranslations() {
        for (int x = 0; x < CHUNKNUMS; x++) {
            for (int y = 0; y < CHUNKNUMS; y++) {
                if (chunks[x][y] != null) {
                    float realX = (x - (CHUNKNUMS / 2)) * MyGdxGame.SIZE;
                    float realY = (y - (CHUNKNUMS / 2)) * MyGdxGame.SIZE;
                    chunks[x][y].setTranslation(realX, realY);
                }
            }
        }
    }

    public Vector3 getGradient(float x, float y) {
        GRADIENT.set(x, y, 0);
        float h = Chunk.getVals(chunks, GRADIENT, 0, 0);
        float dx = Chunk.getVals(chunks, GRADIENT, 1, 0) - h;
        float dy = Chunk.getVals(chunks, GRADIENT, 0, 1) - h;
        GRADIENT.set(-dx, dy, 0);
        GRADIENT.nor();
        return GRADIENT;
    }
}
