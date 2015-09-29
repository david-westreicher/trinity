package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.westreicher.birdsim.Chunk;
import com.westreicher.birdsim.ChunkManager;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.MapCoordinate;

/**
 * Created by david on 9/28/15.
 */

@Wire
public class TranslateMapCoordinates extends EntityProcessingSystem {
    private static final int CHUNKNUMS = Config.CHUNKNUMS;
    private int dx;
    private int dy;
    private ComponentMapper<MapCoordinate> coordMapper;

    public TranslateMapCoordinates() {
        super(Aspect.all(MapCoordinate.class));
    }

    @Override
    protected boolean checkProcessing() {
        Entity camentity = world.getManager(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG);
        MapCoordinate coord = camentity.getComponent(MapCoordinate.class);
        this.dx = 0;
        this.dy = 0;
        if (Math.abs(coord.x) > Config.TILES_PER_CHUNK / 2.0)
            dx = (int) Math.signum(coord.x);
        if (Math.abs(coord.y) > Config.TILES_PER_CHUNK / 2.0)
            dy = (int) Math.signum(coord.y);
        return (dx != 0 || dy != 0);
    }

    @Override
    protected void begin() {
        ChunkManager cm = world.getManager(TagManager.class).getEntity(Artemis.CHUNKMANAGER_TAG).getComponent(ChunkManager.class);
        cm.pos[0] += dx;
        if (dx != 0) {
            int stax = dx > 0 ? 0 : CHUNKNUMS - 1;
            int endx = dx > 0 ? CHUNKNUMS - 1 : 0;
            int plus = dx > 0 ? 1 : -1;
            for (int x = stax; dx > 0 ? x < endx : x > endx; x += plus)
                for (int y = 0; y < CHUNKNUMS; y++)
                    cm.swap(x, y, x + plus, y);
            for (int y = 0; y < CHUNKNUMS; y++) {
                Chunk c = cm.chunks[endx][y];
                long realX = (endx - (CHUNKNUMS / 2)) + cm.pos[0];
                long realY = (y - (CHUNKNUMS / 2)) + cm.pos[1];
                c.setPos(realX, realY);
            }
        }
        cm.pos[1] += dy;
        if (dy != 0) {
            int stay = dy > 0 ? 0 : CHUNKNUMS - 1;
            int endy = dy > 0 ? CHUNKNUMS - 1 : 0;
            int plus = dy > 0 ? 1 : -1;
            for (int x = 0; x < CHUNKNUMS; x++)
                for (int y = stay; dy > 0 ? y < endy : y > endy; y += plus)
                    cm.swap(x, y, x, y + plus);
            for (int x = 0; x < CHUNKNUMS; x++) {
                Chunk c = cm.chunks[x][endy];
                long realX = (x - (CHUNKNUMS / 2)) + cm.pos[0];
                long realY = (endy - (CHUNKNUMS / 2)) + cm.pos[1];
                c.setPos(realX, realY);
            }
        }
    }

    @Override
    protected void process(Entity e) {
        MapCoordinate coord = coordMapper.get(e);
        coord.x -= dx * Config.TILES_PER_CHUNK;
        coord.y -= dy * Config.TILES_PER_CHUNK;
    }
}
