package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.westreicher.birdsim.Chunk;
import com.westreicher.birdsim.ChunkManager;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.util.Spiral;

/**
 * Created by david on 9/29/15.
 */
@Wire
public class RegenerateChunks extends IteratingSystem {
    ComponentMapper<ChunkManager> chunkMapper;
    private Spiral spiral;

    private static final int[][] possNeigh = new int[][]{//
            new int[]{0, -1},//
            new int[]{1, -1},//
            new int[]{1, 0},//
            new int[]{1, 1},//
            new int[]{0, 1},//
            new int[]{-1, 1},//
            new int[]{-1, 0},//
            new int[]{-1, 1},//
    };
    private static final Chunk[] neighbs = new Chunk[possNeigh.length];


    public RegenerateChunks() {
        super(Aspect.all(ChunkManager.class));
    }

    @Override
    protected void initialize() {
        spiral = new Spiral();
    }

    @Override
    protected void process(int e) {
        ChunkManager cm = chunkMapper.get(e);
        spiral.reset();
        int maxupdates = 1;
        while (true) {
            Vector2 spos = spiral.next();
            int x = ((int) spos.x) + Config.CHUNKNUMS / 2;
            int y = ((int) spos.y) + Config.CHUNKNUMS / 2;
            if (x < 0 || y < 0 || x >= Config.CHUNKNUMS || y >= Config.CHUNKNUMS)
                break;
            Chunk c = cm.chunks[x][y];
            //fill neigh
            int neighbors = 0;
            for (int i = 0; i < possNeigh.length; i++) {
                int nx = x + possNeigh[i][0];
                int ny = y + possNeigh[i][1];
                //border chunk
                if (nx < 0 || ny < 0 || nx >= Config.CHUNKNUMS || ny >= Config.CHUNKNUMS) break;
                neighbs[neighbors++] = cm.chunks[nx][ny];
            }
            if (neighbors < possNeigh.length) continue;

            if (c.stateAdvance(neighbs, cm))
                if (--maxupdates <= 0) break;
            /*
            if (!c.isReady) {
                if (cm.chunks[x][y].genMesh(cm))
                    maxupdates -= 1;
                if (maxupdates <= 0)
                    break;
            }
            */
        }
    }

    @Override
    protected void dispose() {
        for (int i = 0; i < neighbs.length; i++)
            neighbs[i] = null;
    }
}