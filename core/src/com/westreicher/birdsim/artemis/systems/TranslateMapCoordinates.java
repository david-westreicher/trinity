package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.westreicher.birdsim.ChunkManager;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.MapCoordinate;

/**
 * Created by david on 9/28/15.
 */

@Wire
public class TranslateMapCoordinates extends EntityProcessingSystem {
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
        cm.updateDirection(dx, dy);
    }

    @Override
    protected void process(Entity e) {
        MapCoordinate coord = coordMapper.get(e);
        coord.x -= dx * Config.TILES_PER_CHUNK;
        coord.y -= dy * Config.TILES_PER_CHUNK;
    }
}
