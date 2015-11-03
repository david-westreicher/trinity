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
import com.westreicher.birdsim.UI.Settings;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.components.Game;
import com.westreicher.birdsim.artemis.factories.UberFactory;
import com.westreicher.birdsim.artemis.managers.InputManager;
import com.westreicher.birdsim.artemis.managers.ModelManager;
import com.westreicher.birdsim.artemis.managers.PostProcessingShaders;
import com.westreicher.birdsim.artemis.managers.ShaderManager;
import com.westreicher.birdsim.artemis.managers.TextureManager;
import com.westreicher.birdsim.artemis.systems.AdjustHeight;
import com.westreicher.birdsim.artemis.systems.AnimateParticles;
import com.westreicher.birdsim.artemis.systems.Animation;
import com.westreicher.birdsim.artemis.systems.CollideTerrain;
import com.westreicher.birdsim.artemis.systems.DeleteEntities;
import com.westreicher.birdsim.artemis.systems.EntityCollisions;
import com.westreicher.birdsim.artemis.systems.GameUI;
import com.westreicher.birdsim.artemis.systems.HandleGameInput;
import com.westreicher.birdsim.artemis.systems.HandlePause;
import com.westreicher.birdsim.artemis.systems.Interpolate;
import com.westreicher.birdsim.artemis.systems.MenuUI;
import com.westreicher.birdsim.artemis.systems.MovementSystem;
import com.westreicher.birdsim.artemis.systems.PositionCam;
import com.westreicher.birdsim.artemis.systems.RegenerateChunks;
import com.westreicher.birdsim.artemis.systems.RenderChunks;
import com.westreicher.birdsim.artemis.systems.RenderGui;
import com.westreicher.birdsim.artemis.systems.RenderModels;
import com.westreicher.birdsim.artemis.systems.RenderParticles;
import com.westreicher.birdsim.artemis.systems.RenderProfiler;
import com.westreicher.birdsim.artemis.systems.StartRendering;
import com.westreicher.birdsim.artemis.systems.TranslateMapAndSpawn;
import com.westreicher.birdsim.artemis.systems.UpdateSlotSystem;

import java.util.ArrayList;

/**
 * Created by david on 9/25/15.
 */
public class Artemis extends World {

    public static final ArrayList<Class> LOGIC_SYSTEMS = new ArrayList<Class>();
    public static final String VIRTUAL_CAM_TAG = "virtualcam";
    public static final String CHUNKMANAGER_TAG = "chunkmanager";
    public static final String GAME_TAG = "game";
    public static final String PLAYER_GROUP = "players";
    public static final String PARTICLE_SYS_TAG = "particlesystem";
    public static final Settings settings = new Settings();

    private Artemis(WorldConfiguration config) {
        super(config);
    }

    public static Artemis init() {
        Config.IS_DESKTOP = Gdx.app.getType() == Application.ApplicationType.Desktop;
        LOGIC_SYSTEMS.clear();
        WorldConfiguration config = new WorldConfiguration();
        //MANAGERS
        config.setSystem(UberFactory.class);
        config.setSystem(TagManager.class);
        config.setSystem(GroupManager.class);
        config.setSystem(ModelManager.class);
        config.setSystem(ShaderManager.class);
        config.setSystem(TextureManager.class);
        config.setSystem(InputManager.class);
        config.setSystem(PostProcessingShaders.class);

        config.setSystem(HandlePause.class);
        //LOGIC
        addLogic(config, UpdateSlotSystem.class);
        addLogic(config, HandleGameInput.class);
        addLogic(config, MovementSystem.class);
        addLogic(config, TranslateMapAndSpawn.class);
        addLogic(config, RegenerateChunks.class);
        addLogic(config, CollideTerrain.class);
        addLogic(config, EntityCollisions.class);
        addLogic(config, DeleteEntities.class);
        addLogic(config, Animation.class);
        addLogic(config, PositionCam.class);
        addLogic(config, AnimateParticles.class);

        //RENDERING
        config.setSystem(Interpolate.class);
        config.setSystem(AdjustHeight.class);
        config.setSystem(StartRendering.class);
        config.setSystem(RenderParticles.class);
        config.setSystem(RenderModels.class);
        config.setSystem(RenderChunks.class);
        //config.setSystem(RenderModelsGlow.class);
        config.setSystem(GameUI.class);
        config.setSystem(RenderGui.class);

        config.setSystem(RenderProfiler.class);

        config.setSystem(MenuUI.class);

        Artemis a = new Artemis(config);
        a.setInvocationStrategy(new FixedTimestepStrategy(a));
        addCamAndViewport(a);
        addChunkManager(a);
        addGameComponent(a);
        return a;
    }

    private static void addLogic(WorldConfiguration config, Class<? extends BaseSystem> clss) {
        LOGIC_SYSTEMS.add(clss);
        config.setSystem(clss);
    }


    private static void addCamAndViewport(Artemis a) {
        CameraComponent camcomp = UberFactory.createCam(a);
        camcomp.cam.near = 1f;
        camcomp.cam.position.set(0, 0, Config.FIRST_PERSON ? 160 : 200);
        camcomp.cam.far = camcomp.cam.position.z + (Config.POST_PROCESSING ? -1 : 1);
        camcomp.cam.update();
    }

    private static void addChunkManager(Artemis a) {
        new EntityBuilder(a)
                .with(new ChunkManager())
                .tag(CHUNKMANAGER_TAG)
                .build();
    }

    private static void addGameComponent(Artemis a) {
        new EntityBuilder(a)
                .with(new Game())
                .tag(GAME_TAG)
                .build();
    }

    @Override
    public void dispose() {
        super.dispose();
        ChunkManager cm = this.getSystem(TagManager.class).getEntity(CHUNKMANAGER_TAG).getComponent(ChunkManager.class);
        cm.dispose();
    }

    public void resize(int width, int height) {
        ChunkManager cm = this.getSystem(TagManager.class).getEntity(CHUNKMANAGER_TAG).getComponent(ChunkManager.class);
        cm.resize(width, height);
        Viewport viewport = this.getSystem(TagManager.class).getEntity(VIRTUAL_CAM_TAG).getComponent(CameraComponent.class).viewport;
        viewport.update(width, height);
        this.getSystem(PostProcessingShaders.class).resize(width, height);
        this.getSystem(InputManager.class).resize();
    }
}
