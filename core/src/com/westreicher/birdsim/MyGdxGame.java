package com.westreicher.birdsim;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
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
import com.westreicher.birdsim.util.CController;
import com.westreicher.birdsim.util.InputHelper;
import com.westreicher.birdsim.util.Keyboard;
import com.westreicher.birdsim.util.ManagedRessources;
import com.westreicher.birdsim.util.RenderToTexture.DownSampler;
import com.westreicher.birdsim.util.SaveMouse;
import com.westreicher.birdsim.util.SoundPlayer;

public class MyGdxGame extends ApplicationAdapter {
    public static boolean isDesktop;
    private PerspectiveCamera cam;
    ModelBatch mb;
    Viewport viewport;
    private float rat = 1;
    private ModelInstance player;
    private InputHelper firstPointer;
    private InputHelper secondPointer;
    public ChunkManager chunkManager;
    private FPSLogger fps = new FPSLogger();
    private SpriteBatch spritebatch;
    private Texture thumbTex;
    public static Transform playerTransform;
    private float delta;
    private SaveMouse thirdPointer;
    private DownSampler downs = null;
    public static MyGdxGame single = null;
    public Vector3 virtualcam = new Vector3();

    private SoundPlayer soundplayer;


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
        isDesktop = Gdx.app.getType() == Application.ApplicationType.Desktop;
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

        Controller ctrl = null;
        for (Controller controller : Controllers.getControllers()) {
            ctrl = controller;
            break;
        }

        if (isDesktop) {
            if (ctrl != null) {
                firstPointer = new CController(0, ctrl);
                secondPointer = new CController(1, ctrl);
                Gdx.app.log("game", "Gamepad found: " + ctrl.getName());
            } else {
                firstPointer = new Keyboard(0);
                secondPointer = new Keyboard(1);
            }
        } else {
            firstPointer = new SaveMouse(0, viewport);
            secondPointer = new SaveMouse(1, viewport);
        }

        thirdPointer = new SaveMouse(2, viewport);
        playerTransform = new Transform();
        Entity.init();
        soundplayer = new SoundPlayer();
    }


    public void movePlayer(float x, float y) {
        playerTransform.position.x += x;
        playerTransform.position.y -= y;
        if (chunkManager.getVal(playerTransform.position) <= 0)
            return;
        playerTransform.position.y += y;
        if (chunkManager.getVal(playerTransform.position) <= 0)
            return;
        playerTransform.position.x -= x;
        playerTransform.position.y -= y;
        if (chunkManager.getVal(playerTransform.position) <= 0)
            return;
        playerTransform.position.y += y;
    }

    @Override
    public void render() {
        this.delta = Gdx.graphics.getDeltaTime();
        fps.log();
        int dx = 0;
        int dy = 0;
        if (Math.abs(virtualcam.x) > Config.TILES_PER_CHUNK / 2.0)
            dx = (int) Math.signum(virtualcam.x);
        if (Math.abs(virtualcam.y) > Config.TILES_PER_CHUNK / 2.0)
            dy = (int) Math.signum(virtualcam.y);
        virtualcam.x -= dx * Config.TILES_PER_CHUNK;
        virtualcam.y -= dy * Config.TILES_PER_CHUNK;
        playerTransform.position.add(-dx * Config.TILES_PER_CHUNK, -dy * Config.TILES_PER_CHUNK, 0);
        Entity.translateAll(-dx * Config.TILES_PER_CHUNK, -dy * Config.TILES_PER_CHUNK);
        chunkManager.updateDirection(dx, dy);
        Entity.updateall(delta);

        if (firstPointer.update()) {
            int mousex = firstPointer.relx();
            int mousey = firstPointer.rely();
            float rad = firstPointer.getRadiant();
            playerTransform.radiant = rad;
            //movePlayer(mousex * delta * Config.MOVE_SPEED, mousey * delta * Config.MOVE_SPEED);
            movePlayer((float) Math.cos(rad) * delta * Config.MOVE_SPEED, -(float) Math.sin(rad) * delta * Config.MOVE_SPEED);
        }
        //Gdx.app.log("game", "" +"");
        float xspeed = 0;
        float yspeed = 0;
        if (secondPointer.update()) {
            float radiant = secondPointer.getRadiant();
            xspeed = (float) Math.cos(radiant);
            yspeed = (float) Math.sin(radiant);
            if (Gdx.graphics.getFrameId() % 10 == 0) {
                Entity.shoot(playerTransform.position.x, playerTransform.position.y, xspeed, yspeed, Entity.ColorAttr.RED, null);
            }
        }
        virtualcam.x += (playerTransform.position.x + xspeed * 0 - virtualcam.x) / 10.0f;
        virtualcam.y += (playerTransform.position.y + yspeed * 0 - virtualcam.y) / 10.0f;
        cam.far = thirdPointer.update() ? 180 : 250;
        cam.position.z += (cam.far - cam.position.z) / 10.0f;

        //chunkManager.explode2(playerTransform.position, isDesktop ? 15 : 7);
        playerTransform.position.z = chunkManager.getVal(playerTransform.position.x, playerTransform.position.y) * Config.TERRAIN_HEIGHT + 145;
        playerTransform.transform(player);

        //cam.position.set(virtualcam.x, virtualcam.y - 50, cam.position.z);
        //cam.lookAt(cam.position.x, cam.position.y + 200, 0);
        cam.position.set(virtualcam.x, virtualcam.y, cam.position.z);
        cam.update();

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
        } else { // TODO isTouch?
            drawThumbs();
        }
    }

    private void drawThumbs() {
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