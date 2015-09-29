package com.westreicher.birdsim;

import com.artemis.Component;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.westreicher.birdsim.util.BatchShaderProgram;

/**
 * Created by david on 9/4/15.
 */
public class ChunkManager extends Component {
    public static final float OUTSIDE = -10.0f;
    private static final int CHUNKNUMS = Config.CHUNKNUMS;
    public Chunk chunks[][] = new Chunk[CHUNKNUMS][CHUNKNUMS];
    public long[] pos = new long[]{0, 0};
    private static final TileResult TILE_RESULT = new TileResult();
    public float pointsize;

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

    public void swap(int x1, int y1, int x2, int y2) {
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
