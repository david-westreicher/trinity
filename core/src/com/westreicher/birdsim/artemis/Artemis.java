package com.westreicher.birdsim.artemis;

import com.artemis.BaseSystem;
import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.artemis.utils.EntityBuilder;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.westreicher.birdsim.ChunkManager;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.factories.UberFactory;
import com.westreicher.birdsim.artemis.managers.InputManager;
import com.westreicher.birdsim.artemis.managers.ModelManager;
import com.westreicher.birdsim.artemis.managers.ShaderManager;
import com.westreicher.birdsim.artemis.managers.TextureManager;
import com.westreicher.birdsim.artemis.systems.AdjustHeight;
import com.westreicher.birdsim.artemis.systems.CollideTerrain;
import com.westreicher.birdsim.artemis.systems.DeleteEntities;
import com.westreicher.birdsim.artemis.systems.HandleGameInput;
import com.westreicher.birdsim.artemis.systems.HandlePause;
import com.westreicher.birdsim.artemis.systems.Interpolate;
import com.westreicher.birdsim.artemis.systems.MovementSystem;
import com.westreicher.birdsim.artemis.systems.PositionCam;
import com.westreicher.birdsim.artemis.systems.RegenerateMeshesAndSpawn;
import com.westreicher.birdsim.artemis.systems.RenderChunks;
import com.westreicher.birdsim.artemis.systems.RenderGui;
import com.westreicher.birdsim.artemis.systems.RenderModels;
import com.westreicher.birdsim.artemis.systems.StartRendering;
import com.westreicher.birdsim.artemis.systems.TranslateMapCoordinates;

import java.util.ArrayList;

/**
 * Created by david on 9/25/15.
 */
public class Artemis extends World {

    public static final ArrayList<Class> LOGIC_SYSTEMS = new ArrayList<Class>();
    public static final String VIRTUAL_CAM_TAG = "virtualcam";
    public static final String CHUNKMANAGER_TAG = "chunkmanager";
    public static final String PLAYER_GROUP = "players";

    private Artemis(WorldConfiguration config) {
        super(config);
    }

    public static Artemis init() {
        Config.IS_DESKTOP = Gdx.app.getType() == Application.ApplicationType.Desktop;
        LOGIC_SYSTEMS.clear();
        WorldConfiguration config = new WorldConfiguration();
        config.setManager(UberFactory.class);
        config.setManager(TagManager.class);
        config.setManager(GroupManager.class);
        config.setManager(ModelManager.class);
        config.setManager(ShaderManager.class);
        config.setManager(TextureManager.class);
        config.setManager(InputManager.class);


        config.setSystem(HandlePause.class);
        //LOGIC
        addLogic(config, HandleGameInput.class);
        addLogic(config, MovementSystem.class);
        addLogic(config, TranslateMapCoordinates.class);
        addLogic(config, RegenerateMeshesAndSpawn.class);
        //addLogic(config, CollideTerrain.class);
        addLogic(config, DeleteEntities.class);
        addLogic(config, PositionCam.class);

        //RENDERING
        config.setSystem(Interpolate.class);
        config.setSystem(AdjustHeight.class);
        config.setSystem(StartRendering.class);
        config.setSystem(RenderChunks.class);
        config.setSystem(RenderModels.class);
        config.setSystem(RenderGui.class);
        //config.setSystem(TextRendering.class);

        Artemis a = new Artemis(config);
        a.setInvocationStrategy(new FixedTimestepStrategy(a));
        addCamAndViewport(a);
        addChunkManager(a);
        return a;
    }

    private static void addLogic(WorldConfiguration config, Class<? extends BaseSystem> clss) {
        LOGIC_SYSTEMS.add(clss);
        config.setSystem(clss);
    }


    private static void addCamAndViewport(Artemis a) {
        CameraComponent camcomp = UberFactory.createCam(a);
        camcomp.cam.near = 1f;
        camcomp.cam.far = 200f;
        camcomp.cam.position.set(0, 0, 200);
        camcomp.cam.update();
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
        this.getManager(InputManager.class).resize();
    }
}
