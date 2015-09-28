package com.westreicher.birdsim.artemis;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.managers.TagManager;
import com.artemis.utils.EntityBuilder;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.westreicher.birdsim.ChunkManager;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.components.MapCoordinate;
import com.westreicher.birdsim.artemis.components.Speed2;
import com.westreicher.birdsim.artemis.systems.*;

/**
 * Created by david on 9/25/15.
 */
public class Artemis extends World {

    public static final Class[] LOGIC_SYSTEMS = new Class[]{MovementSystem.class, TranslateMapCoordinates.class};
    public static final String VIRTUAL_CAM_TAG = "virtualcam";
    public static final String CHUNKMANAGER_TAG = "chunkmanager";

    private Artemis(WorldConfiguration config) {
        super(config);
    }

    public static Artemis init() {
        WorldConfiguration config = new WorldConfiguration();
        config.setManager(TagManager.class);

        //LOGIC
        config.setSystem(MovementSystem.class);
        config.setSystem(TranslateMapCoordinates.class);

        //RENDERING
        config.setSystem(StartRendering.class);
        config.setSystem(RenderChunks.class);
        config.setSystem(TextRendering.class);

        Artemis a = new Artemis(config);
        a.setInvocationStrategy(new FixedTimestepStrategy(a));
        addCamAndViewport(a);
        addChunkManager(a);
        return a;
    }

    private static void addCamAndViewport(Artemis a) {
        CameraComponent camcomp = new CameraComponent();
        camcomp.cam.near = 1f;
        camcomp.cam.far = 500f;
        new EntityBuilder(a)
                .with(camcomp)
                .with(new MapCoordinate())
                .with(new Speed2())
                .tag(VIRTUAL_CAM_TAG)
                .build();
    }

    private static void addChunkManager(Artemis a) {
        new EntityBuilder(a)
                .with(new ChunkManager())
                .tag(CHUNKMANAGER_TAG)
                .build();
    }

    @Override
    public void dispose() {
        super.dispose();
        ChunkManager cm = this.getManager(TagManager.class).getEntity(CHUNKMANAGER_TAG).getComponent(ChunkManager.class);
        cm.dispose();
    }

    public void resize(int width, int height) {
        ChunkManager cm = this.getManager(TagManager.class).getEntity(CHUNKMANAGER_TAG).getComponent(ChunkManager.class);
        cm.resize(width, height);
        Viewport viewport = this.getManager(TagManager.class).getEntity(VIRTUAL_CAM_TAG).getComponent(CameraComponent.class).viewport;
        viewport.update(width, height);
    }
}
