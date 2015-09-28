package com.westreicher.birdsim.artemis;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.managers.TagManager;
import com.artemis.utils.EntityBuilder;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.westreicher.birdsim.ChunkManager;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.components.RenderPosition;
import com.westreicher.birdsim.artemis.components.MapCoordinate;
import com.westreicher.birdsim.artemis.components.Speed2;
import com.westreicher.birdsim.artemis.factories.UberFactory;
import com.westreicher.birdsim.artemis.managers.ModelManager;
import com.westreicher.birdsim.artemis.systems.*;
import com.westreicher.birdsim.util.InputHelper;

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
        config.setManager(ModelManager.class);

        //LOGIC
        config.setSystem(MovementSystem.class);
        config.setSystem(TranslateMapCoordinates.class);

        //RENDERING
        config.setSystem(Interpolate.class);
        config.setSystem(AdjustHeight.class);
        config.setSystem(StartRendering.class);
        config.setSystem(RenderChunks.class);
        config.setSystem(RenderModels.class);
        //config.setSystem(TextRendering.class);

        Artemis a = new Artemis(config);
        a.setInvocationStrategy(new FixedTimestepStrategy(a));
        Viewport v = addCamAndViewport(a);
        addChunkManager(a);
        addPlayers(v, a);
        return a;
    }

    private static void addPlayers(Viewport v, World w) {
        InputHelper.init(true, v);
        int id = 0;
        //for (InputHelper.PlayerInput pi : InputHelper.players) {
        for (int i = 0; i < 10; i++) {
            UberFactory.createPlayer(w, id++);
        }
    }

    private static Viewport addCamAndViewport(Artemis a) {
        CameraComponent camcomp = new CameraComponent();
        camcomp.cam.near = 1f;
        camcomp.cam.far = 500f;
        new EntityBuilder(a)
                .with(camcomp)
                .with(new MapCoordinate())
                .with(new Speed2())
                .with(new RenderPosition())
                .tag(VIRTUAL_CAM_TAG)
                .build();
        return camcomp.viewport;
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
