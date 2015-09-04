package com.westreicher.birdsim;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by david on 9/4/15.
 */
public class ChunkManager {
    Chunk chunks[][] = new Chunk[3][3];

    public ChunkManager() {
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                chunks[x][y] = getInstance(x, y, x - 1, y - 1);
    }

    public Chunk getInstance(int x, int y, int absx, int absy) {
        return new Chunk((int) MyGdxGame.SIZE, (int) MyGdxGame.SIZE, x, y, absx, absy);
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

    public void explode(Vector3 pos, boolean isEnemy) {
        for (Chunk mis[] : chunks)
            for (Chunk mi : mis)
                mi.explode(pos, isEnemy);
    }

    public void render(ModelBatch mb) {
        for (Chunk mis[] : chunks)
            for (Chunk mi : mis) {
                mi.regenerateMesh();
                mb.render(mi.modelinstance);
            }
    }

    public void dispose() {
        for (Chunk mis[] : chunks)
            for (Chunk mi : mis)
                mi.modelinstance.model.dispose();
    }

    public void updateDirection(int dx, int dy) {
        if (dx != 0) {
            Chunk newchunks[][] = new Chunk[3][3];
            for (int x = (dx > 0 ? 0 : 1); x < (dx > 0 ? 2 : 3); x++)
                for (int y = 0; y < 3; y++) {
                    newchunks[x][y] = chunks[x + (dx > 0 ? 1 : -1)][y];
                    newchunks[x][y].modelinstance.transform.translate(-dx * MyGdxGame.SIZE, 0, 0);
                }
            int x = dx > 0 ? 2 : 0;
            for (int y = 0; y < 3; y++) {
                chunks[dx > 0 ? 0 : 2][y].modelinstance.model.dispose();
                newchunks[x][y] = getInstance(x, y, chunks[x][y].absx + dx, chunks[x][y].absy);
            }
            chunks = newchunks;
        }
        if (dy != 0) {
            Chunk newchunks[][] = new Chunk[3][3];
            for (int x = 0; x < 3; x++)
                for (int y = (dy > 0 ? 0 : 1); y < (dy > 0 ? 2 : 3); y++) {
                    newchunks[x][y] = chunks[x][y + (dy > 0 ? 1 : -1)];
                    newchunks[x][y].modelinstance.transform.translate(0, -dy * MyGdxGame.SIZE, 0);
                }
            int y = dy > 0 ? 2 : 0;
            for (int x = 0; x < 3; x++) {
                chunks[x][dy > 0 ? 0 : 2].modelinstance.model.dispose();
                newchunks[x][y] = getInstance(x, y, chunks[x][y].absx, chunks[x][y].absy + dy);
            }
            chunks = newchunks;
        }
    }
}
