package com.westreicher.birdsim;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.westreicher.birdsim.util.ManagedRessources;
import com.westreicher.birdsim.util.Spiral;

/**
 * Created by david on 9/4/15.
 */
public class ChunkManager {
    private static final Vector3 GRADIENT = new Vector3(1, 0, 0);
    public static final int CHUNKNUMS = (MyGdxGame.isDesktop ? 12 : 7) * 2 + 1;
    private Chunk chunks[][] = new Chunk[CHUNKNUMS][CHUNKNUMS];
    private int[] pos = new int[]{0, 0};
    private Spiral s = new Spiral();
    private ShaderProgram shader;
    private float[] tmpfloat = new float[3];

    public ChunkManager() {
        for (int x = 0; x < CHUNKNUMS; x++) {
            for (int y = 0; y < CHUNKNUMS; y++) {
                int realX = (x - (CHUNKNUMS / 2));
                int realY = (y - (CHUNKNUMS / 2));
                chunks[x][y] = new Chunk();
                chunks[x][y].setPos(realX, realY);
            }
        }
        updateTranslations();
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

    public void render(Camera cam, Vector3 virtualcam) {
        s.reset();
        int maxupdates = MyGdxGame.isDesktop ? 20 : 5;
        while (true) {
            Vector2 spos = s.next();
            int x = ((int) spos.x) + CHUNKNUMS / 2;
            int y = ((int) spos.y) + CHUNKNUMS / 2;
            if (x < 0 || y < 0 || x >= CHUNKNUMS || y >= CHUNKNUMS)
                break;
            Chunk mi = chunks[x][y];
            if (!mi.isReady) {
                if (maxupdates == 0)
                    break;
                if (chunks[x][y].genMesh()) maxupdates -= 1;
                //updateTranslations();
            }
        }
        Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl20.glEnable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
        shader = ManagedRessources.getShader(ManagedRessources.Shaders.CHUNK);
        shader.begin();
        shader.setUniformMatrix("u_projTrans", cam.combined);
        if (Config.POST_PROCESSING) {
            shader.setUniformf("virtualcam", virtualcam);
            shader.setUniformf("maxdstsqinv", 1f / (140f * 140f));
        }
        shader.setUniformf("pointsize", Config.POST_PROCESSING ? 8 : 10);
        for (int x = 0; x < CHUNKNUMS; x++) {
            for (int y = 0; y < CHUNKNUMS; y++) {
                Chunk mi = chunks[x][y];
                if (mi.isReady && mi.shouldDraw) {
                    tmpfloat[0] = (x - (CHUNKNUMS / 2)) * Config.TILES_PER_CHUNK - Config.TILES_PER_CHUNK / 2;
                    tmpfloat[1] = (y - (CHUNKNUMS / 2)) * Config.TILES_PER_CHUNK + Config.TILES_PER_CHUNK / 2;
                    shader.setUniform3fv("trans", tmpfloat, 0, 3);
                    mi.m.render(shader, GL20.GL_POINTS);
                }
            }
        }
        shader.end();
        Gdx.gl20.glDisable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
        Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);
    }

    public void dispose() {
        for (int x = 0; x < CHUNKNUMS; x++)
            for (int y = 0; y < CHUNKNUMS; y++) {
                Chunk mi = chunks[x][y];
                if (mi != null) {
                    mi.dispose();
                    chunks[x][y] = null;
                }
            }
        pos[0] = 0;
        pos[1] = 0;
    }

    public void updateDirection(int dx, int dy) {
        this.pos[0] += dx;
        if (dx != 0) {
            int stax = dx > 0 ? 0 : CHUNKNUMS - 1;
            int endx = dx > 0 ? CHUNKNUMS - 1 : 0;
            int plus = dx > 0 ? 1 : -1;
            for (int x = stax; dx > 0 ? x < endx : x > endx; x += plus)
                for (int y = 0; y < CHUNKNUMS; y++)
                    swap(x, y, x + plus, y);
            for (int y = 0; y < CHUNKNUMS; y++) {
                Chunk c = chunks[endx][y];
                int realX = (endx - (CHUNKNUMS / 2)) + pos[0];
                int realY = (y - (CHUNKNUMS / 2)) + pos[1];
                c.setPos(realX, realY);
            }
        }
        this.pos[1] += dy;
        if (dy != 0) {
            int stay = dy > 0 ? 0 : CHUNKNUMS - 1;
            int endy = dy > 0 ? CHUNKNUMS - 1 : 0;
            int plus = dy > 0 ? 1 : -1;
            for (int x = 0; x < CHUNKNUMS; x++)
                for (int y = stay; dy > 0 ? y < endy : y > endy; y += plus)
                    swap(x, y, x, y + plus);
            for (int x = 0; x < CHUNKNUMS; x++) {
                Chunk c = chunks[x][endy];
                int realX = (x - (CHUNKNUMS / 2)) + pos[0];
                int realY = (endy - (CHUNKNUMS / 2)) + pos[1];
                c.setPos(realX, realY);
            }
        }
        updateTranslations();
    }

    private void swap(int x1, int y1, int x2, int y2) {
        Chunk tmp = chunks[x1][y1];
        chunks[x1][y1] = chunks[x2][y2];
        chunks[x2][y2] = tmp;
    }

    private void updateTranslations() {
        for (int x = 0; x < CHUNKNUMS; x++) {
            for (int y = 0; y < CHUNKNUMS; y++) {
                if (chunks[x][y] != null) {
                    float realX = (x - (CHUNKNUMS / 2)) * Config.TILES_PER_CHUNK;
                    float realY = (y - (CHUNKNUMS / 2)) * Config.TILES_PER_CHUNK;
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
