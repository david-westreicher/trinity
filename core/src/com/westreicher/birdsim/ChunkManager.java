package com.westreicher.birdsim;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.westreicher.birdsim.util.ManagedRessources;
import com.westreicher.birdsim.util.Spiral;
import com.westreicher.birdsim.util.BatchShaderProgram;

/**
 * Created by david on 9/4/15.
 */
public class ChunkManager {
    public static final float OUTSIDE = -10.0f;
    private static final int CHUNKNUMS = Config.CHUNKNUMS;
    private Chunk chunks[][] = new Chunk[CHUNKNUMS][CHUNKNUMS];
    private long[] pos = new long[]{0, 0};
    private Spiral s = new Spiral();
    private BatchShaderProgram shader;
    private float[] tmpfloat = new float[3];
    private static final TileResult TILE_RESULT = new TileResult();
    private float pointsize;

    public ChunkManager() {
        for (int x = 0; x < CHUNKNUMS; x++) {
            for (int y = 0; y < CHUNKNUMS; y++) {
                long realX = (x - (CHUNKNUMS / 2)) + pos[0];
                long realY = (y - (CHUNKNUMS / 2)) + pos[1];
                chunks[x][y] = new Chunk();
                chunks[x][y].setPos(realX, realY);
            }
        }
    }

    public void render(Camera cam) {
        s.reset();
        int maxupdates = 1;
        while (true) {
            Vector2 spos = s.next();
            int x = ((int) spos.x) + CHUNKNUMS / 2;
            int y = ((int) spos.y) + CHUNKNUMS / 2;
            if (x < 0 || y < 0 || x >= CHUNKNUMS || y >= CHUNKNUMS)
                break;
            Chunk mi = chunks[x][y];
            if (!mi.isReady) {
                if (chunks[x][y].genMesh()) {
                    maxupdates -= 1;
                    if (x == 0 || y == 0 || x == CHUNKNUMS - 1 || y == CHUNKNUMS - 1)
                        MyGdxGame.single.entitymanager.spawn(spos.x * Config.TILES_PER_CHUNK, spos.y * Config.TILES_PER_CHUNK, mi.rand);
                }
                if (maxupdates <= 0)
                    break;
            }
        }
        Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl20.glEnable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
        shader = ManagedRessources.getShader(ManagedRessources.Shaders.CHUNK);
        shader.begin();
        shader.setUniformMatrix("u_projTrans", cam.combined);
        if (Config.POST_PROCESSING) {
            shader.setUniformf("virtualcam", cam.position.x, cam.position.y);
            shader.setUniformf("maxdstsqinv", 1f / (140f * 140f));
        }
        shader.setUniformf("pointsize", pointsize);
        shader.setUniformf("chunksize", Config.TILES_PER_CHUNK);
        shader.setUniformf("heightscale", 2.5f * Config.TERRAIN_HEIGHT / Config.TILES_PER_CHUNK);
        shader.bind();
        for (int x = 0; x < CHUNKNUMS; x++) {
            for (int y = 0; y < CHUNKNUMS; y++) {
                Chunk mi = chunks[x][y];
                if (mi.shouldDraw) {
                    tmpfloat[0] = (x - (CHUNKNUMS / 2)) * Config.TILES_PER_CHUNK - Config.TILES_PER_CHUNK / 2;
                    tmpfloat[1] = (y - (CHUNKNUMS / 2)) * Config.TILES_PER_CHUNK - Config.TILES_PER_CHUNK / 2;
                    shader.setUniform3fv("trans", tmpfloat, 0, 3);
                    mi.m.render(shader, GL20.GL_POINTS);
                }
            }
        }
        shader.unbind();
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
                long realX = (endx - (CHUNKNUMS / 2)) + pos[0];
                long realY = (y - (CHUNKNUMS / 2)) + pos[1];
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
                long realX = (x - (CHUNKNUMS / 2)) + pos[0];
                long realY = (endy - (CHUNKNUMS / 2)) + pos[1];
                c.setPos(realX, realY);
            }
        }
    }

    private void swap(int x1, int y1, int x2, int y2) {
        Chunk tmp = chunks[x1][y1];
        chunks[x1][y1] = chunks[x2][y2];
        chunks[x2][y2] = tmp;
    }

    public void explode2(Vector3 position, float dist) {
        if (dist == 0) {
            addVal(position.x, position.y, -0.5f);
            return;
        }
        float distsq = dist * dist;
        for (float x = -dist; x <= dist; x++) {
            for (float y = -dist; y <= dist; y++) {
                float distfac = (x * x + y * y) / distsq;
                if (getVal(x + position.x, y + position.y) > 0 && distfac < 1)
                    addVal(x + position.x, y + position.y, (distfac - 1) / 2f);
            }
        }
    }

    private void addVal(float posx, float posy, float val) {
        TileResult tr = setTileResult(posx, posy);
        if (tr.c != null) tr.c.addVal(tr.innerx, tr.innery, val);
    }

    public float getVal(Vector3 pos) {
        return getVal(pos.x, pos.y);
    }

    public float getVal(float posx, float posy) {
        TileResult tr = setTileResult(posx, posy);
        return tr.c == null ? OUTSIDE : tr.c.getVal(tr.innerx, tr.innery);
    }

    public float getValAbs(int x, int y, long absx, long absy) {
        TileResult tr = setTileResult(x + (absx - pos[0]) * Config.TILES_PER_CHUNK - Config.TILES_PER_CHUNK / 2, y + (absy - pos[1]) * Config.TILES_PER_CHUNK - Config.TILES_PER_CHUNK / 2);
        return tr.c == null ? OUTSIDE : tr.c.getVal(tr.innerx, tr.innery);
    }

    public void mulVal(float posx, float posy, float percent) {
        TileResult tr = setTileResult(posx, posy);
        if (tr.c != null) tr.c.mulVal(tr.innerx, tr.innery, percent);
    }

    private TileResult setTileResult(float posx, float posy) {
        float tpc = (float) Config.TILES_PER_CHUNK;
        float xoffset = posx + tpc / 2.0f;
        float yoffset = posy + tpc / 2.0f;
        int divx = (int) Math.floor(xoffset / tpc);
        int divy = (int) Math.floor(yoffset / tpc);
        while (xoffset >= Config.TILES_PER_CHUNK)
            xoffset -= Config.TILES_PER_CHUNK;
        while (xoffset < 0)
            xoffset += Config.TILES_PER_CHUNK;
        while (yoffset >= Config.TILES_PER_CHUNK)
            yoffset -= Config.TILES_PER_CHUNK;
        while (yoffset < 0)
            yoffset += Config.TILES_PER_CHUNK;
        int innerx = (int) (xoffset);
        int innery = Config.TILES_PER_CHUNK - (int) (yoffset) - 1;
        int xChunk = divx + (CHUNKNUMS / 2);
        int yChunk = divy + (CHUNKNUMS / 2);
        if (xChunk < 0 || yChunk < 0 || xChunk >= CHUNKNUMS || yChunk >= CHUNKNUMS)
            TILE_RESULT.c = null;
        else
            TILE_RESULT.c = chunks[xChunk][yChunk];
        TILE_RESULT.innerx = innerx;
        TILE_RESULT.innery = innery;
        return TILE_RESULT;
    }

    public void resize(int width, int height) {
        this.pointsize = (Math.max(width, height) / 150f);
    }

    private static class TileResult {
        public Chunk c;
        public int innerx;
        public int innery;
    }
}
