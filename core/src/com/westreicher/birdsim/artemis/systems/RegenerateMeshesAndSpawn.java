package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Vector2;
import com.westreicher.birdsim.Chunk;
import com.westreicher.birdsim.ChunkManager;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.artemis.factories.UberFactory;
import com.westreicher.birdsim.util.Spiral;

import java.util.Random;

/**
 * Created by david on 9/29/15.
 */
@Wire
public class RegenerateMeshesAndSpawn extends EntityProcessingSystem {
    ComponentMapper<ChunkManager> chunkMapper;
    private Spiral spiral;

    public RegenerateMeshesAndSpawn() {
        super(Aspect.all(ChunkManager.class));
    }

    @Override
    protected void initialize() {
        spiral = new Spiral();
    }

    @Override
    protected void process(Entity e) {
        ChunkManager cm = chunkMapper.get(e);
        spiral.reset();
        int maxupdates = 1;
        while (true) {
            Vector2 spos = spiral.next();
            int x = ((int) spos.x) + Config.CHUNKNUMS / 2;
            int y = ((int) spos.y) + Config.CHUNKNUMS / 2;
            if (x < 0 || y < 0 || x >= Config.CHUNKNUMS || y >= Config.CHUNKNUMS)
                break;
            Chunk mi = cm.chunks[x][y];
            if (!mi.isReady) {
                if (cm.chunks[x][y].genMesh(cm)) {
                    maxupdates -= 1;
                    //TODO don't spawn in regenerate mesh but when the position of chunks changes -.-
                    if (x == 0 || y == 0 || x == Config.CHUNKNUMS - 1 || y == Config.CHUNKNUMS - 1) {
                        maybespawn(spos.x * Config.TILES_PER_CHUNK, spos.y * Config.TILES_PER_CHUNK, mi.rand);
                    }
                }
                if (maxupdates <= 0)
                    break;
            }
        }
    }

    private void maybespawn(float x, float y, Random rand) {
        if (rand.nextDouble() > 0.3)
            return;
        if (rand.nextDouble() > 0.5)
            UberFactory.createEnemy(world, (float) (x + Math.random() * 5), (float) (y + Math.random() * 5));
        else
            UberFactory.createItem(world, (float) (x + Math.random() * 5), (float) (y + Math.random() * 5));
    }
}
