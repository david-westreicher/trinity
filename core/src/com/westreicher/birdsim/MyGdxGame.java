package com.westreicher.birdsim;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.westreicher.birdsim.util.InputHelper;
import com.westreicher.birdsim.util.ManagedRessources;
import com.westreicher.birdsim.util.RenderToTexture.DownSampler;
import com.westreicher.birdsim.util.SoundPlayer;

public class MyGdxGame extends ApplicationAdapter {
    public static boolean isDesktop;
    private PerspectiveCamera cam;
    ModelBatch mb;
    Viewport viewport;
    private float rat = 1;
    private ModelInstance player;
    public ChunkManager chunkManager;
    private FPSLogger fps = new FPSLogger();
    private SpriteBatch spritebatch;
    private Texture thumbTex;
    public static Transform playerTransform;
    private DownSampler downs = null;
    public static MyGdxGame single = null;
    public Vector3 virtualcam = new Vector3();

    private SoundPlayer soundplayer;
    private GameLoop gameloop;


    @Override
    public void resize(int width, int height) {
        rat = 1.0f / Math.min(width, height);
        viewport.update(width, height);
        spritebatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        if (downs != null)
            downs.dispose();
        downs = new DownSampler(width, height);
        chunkManager.resize(width, height);
    }

    @Override
    public void create() {
        isDesktop = Gdx.app.getType() == Application.ApplicationType.Desktop || Gdx.app.getType() == Application.ApplicationType.WebGL;
        single = this;
        ManagedRessources.init();
        Gdx.app.log("game", "create");
        Gdx.app.log("game", "GL ES 3.0 supported: " + (Gdx.gl30 != null));
        //DefaultShader.defaultCullFace = 0;
        cam = new PerspectiveCamera();
        cam.position.set(0, 0, 25 * (Config.DEBUG ? 10 : 1.25f));
        cam.near = 10f;
        cam.far = 500f;
        viewport = new ScreenViewport(cam);
        spritebatch = new SpriteBatch();
        thumbTex = new Texture(Gdx.files.internal("thumb.png"));
        mb = new ModelBatch();
        chunkManager = new ChunkManager();
        player = new ModelInstance(new ObjLoader().loadModel(Gdx.files.internal("player.obj")));
        player.materials.get(0).set(ColorAttribute.createDiffuse(Color.RED));
        //player = new ModelInstance(new ModelBuilder().createBox(1f, 1f, 1f, new Material(ColorAttribute.createDiffuse(1, 0, 0, 0)), VertexAttributes.Usage.Position));
        playerTransform = new Transform();
        Entity.init();
        soundplayer = new SoundPlayer();
        InputHelper.init(isDesktop, viewport);
        gameloop = new GameLoop(virtualcam, cam, chunkManager);
    }


    @Override
    public void render() {
        fps.log();
        gameloop.tick();
        playerTransform.transform(player);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        //Gdx.gl.glClearColor(0.251f, 0.643f, 0.875f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        if (isDesktop) downs.begin();
        mb.begin(cam);
        mb.render(player);
        Entity.render(mb);
        mb.end();
        chunkManager.render(cam, virtualcam);
        if (isDesktop) {
            downs.end();
            downs.draw(viewport.getScreenWidth(), viewport.getScreenHeight());
        }
        drawThumbs();
    }

    private void drawThumbs() {
        InputHelper firstPointer = InputHelper.firstPointer;
        InputHelper secondPointer = InputHelper.secondPointer;
        InputHelper thirdPointer = InputHelper.thirdPointer;
        spritebatch.begin();
        int size = 50;
        if (firstPointer.isDown())
            spritebatch.draw(thumbTex, firstPointer.getStartX() - size, viewport.getScreenHeight() - firstPointer.getStartY() - size, size * 2, size * 2, 0, 0, 1, 1);
        if (secondPointer.isDown())
            spritebatch.draw(thumbTex, secondPointer.getStartX() - size, viewport.getScreenHeight() - secondPointer.getStartY() - size, size * 2, size * 2, 0, 0, 1, 1);
        if (thirdPointer.isDown()) {
            spritebatch.draw(thumbTex, thirdPointer.getStartX() - size, viewport.getScreenHeight() - thirdPointer.getStartY() - size, size * 2, size * 2, 0, 0, 1, 1);
        }
        spritebatch.end();
    }

    @Override
    public void dispose() {
        ManagedRessources.dispose();
        Entity.dispose();
        mb.dispose();
        Gdx.app.log("game", "dispose");
        chunkManager.dispose();
        soundplayer.dispose();
    }

    public void playSound(SoundPlayer.Sounds s, Vector3 pos) {
        soundplayer.play(s, pos);
    }

    public static class Transform {
        public Vector3 position = new Vector3();
        public float scale = 1;
        public float radiant;

        public void transform(ModelInstance player) {
            player.transform.setToTranslation(position);
            player.transform.scl(scale);
            player.transform.rotateRad(Config.UPAXIS, radiant);
        }
    }
}