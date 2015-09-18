package com.westreicher.birdsim;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.westreicher.birdsim.util.ManagedRessources;
import com.westreicher.birdsim.util.RenderToTexture.DownSampler;

public class MyGdxGame extends ApplicationAdapter {
    public static boolean isDesktop;
    public static final Vector3 UPAXIS = new Vector3(0, 0, 1);
    private PerspectiveCamera cam;
    ModelBatch mb;
    Viewport viewport;
    private float rat = 1;
    private ModelInstance player;
    private ModelInstance gun;
    private SaveMouse firstPointer;
    private SaveMouse secondPointer;
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


    @Override
    public void resize(int width, int height) {
        rat = 1.0f / Math.min(width, height);
        viewport.update(width, height);
        spritebatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        if (downs != null)
            downs.dispose();
        downs = new DownSampler(width, height);
    }

    @Override
    public void create() {
        isDesktop = Gdx.app.getType() == Application.ApplicationType.Desktop;
        single = this;
        ManagedRessources.init();
        Gdx.app.log("game", "create");
        Entity.init();
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
        // player = new ModelInstance(new ModelBuilder().createSphere(2, 2, 2, 10, 10, new Material(ColorAttribute.createDiffuse(1, 1, 1, 1)), VertexAttributes.Usage.Position));
        //player = new ModelInstance(new ObjLoader().loadModel(Gdx.files.internal("player.obj")));
        player = new ModelInstance(new ModelBuilder().createBox(1f, 1f, 1f, new Material(ColorAttribute.createDiffuse(1, 0, 0, 0)), VertexAttributes.Usage.Position));
        gun = new ModelInstance(new ModelBuilder().createBox(0.2f, 0.5f, 0.2f, new Material(ColorAttribute.createDiffuse(1, 0, 0, 0)), VertexAttributes.Usage.Position));
        firstPointer = new SaveMouse(0, viewport);
        secondPointer = new SaveMouse(1, viewport);
        thirdPointer = new SaveMouse(2, viewport);
        playerTransform = new Transform();
    }


    public void movePlayer(float x, float y) {
        playerTransform.position.x += x;
        playerTransform.position.y -= y;
        if (chunkManager.getVal(playerTransform.position) > 0 || true || Config.DEBUG)
            return;
        playerTransform.position.y += y;
        if (chunkManager.getVal(playerTransform.position) > 0)
            return;
        playerTransform.position.x -= x;
        playerTransform.position.y -= y;
        if (chunkManager.getVal(playerTransform.position) > 0)
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
        Entity.translateAll(-dx * Config.TILES_PER_CHUNK, -dy * Config.TILES_PER_CHUNK);
        chunkManager.updateDirection(dx, dy);
        Entity.updateall(delta);
        virtualcam.x -= dx * Config.TILES_PER_CHUNK;
        virtualcam.y -= dy * Config.TILES_PER_CHUNK;
        playerTransform.position.add(-dx * Config.TILES_PER_CHUNK, -dy * Config.TILES_PER_CHUNK, 0);

        if (firstPointer.update()) {
            int mousex = firstPointer.relx();
            int mousey = firstPointer.rely();
            float rad = firstPointer.getRadiant();
            playerTransform.radiant = rad;
            movePlayer(mousex * delta * Config.MOVE_SPEED, mousey * delta * Config.MOVE_SPEED);
            //movePlayer((float) Math.cos(rad) * delta * (DEBUG ? 400 : 10), -(float) Math.sin(rad) * delta * (DEBUG ? 400 : 10));
        }
        //Gdx.app.log("game", "" +"");
        virtualcam.x += (playerTransform.position.x - virtualcam.x) / 5.0f;
        virtualcam.y += (playerTransform.position.y - virtualcam.y) / 5.0f;
        if (secondPointer.update() || !isDesktop) {
            //Gdx.app.log("game", secondPointer.rely() + "," + secondPointer.relx());
            int relx = isDesktop ? 0 : secondPointer.relx();
            int rely = isDesktop ? 1 : secondPointer.rely();
            float radiant = secondPointer.getRadiant();
            gun.transform.setToRotationRad(UPAXIS, radiant + (float) Math.PI / 2);
            gun.transform.setTranslation(playerTransform.position);
            gun.transform.translate(0, -0.4f, 0);//secondPointer.relx()*rat,secondPointer.rely()*rat,0);
            if (Gdx.graphics.getFrameId() % 5 == 0) {
                //Entity.shoot(playerTransform.position.x, playerTransform.position.y, (float) Math.cos(radiant), (float) Math.sin(radiant));
            }
        } else {
            gun.transform.setToRotation(UPAXIS, 0);
            gun.transform.translate(-dx * Config.TILES_PER_CHUNK, -dy * Config.TILES_PER_CHUNK, 0);
        }

        if (!thirdPointer.update()) {
            cam.position.z += (250 - cam.position.z) / 10.0f;
        } else
            cam.position.z += (25 * (Config.DEBUG ? 5 : 1.8f) - cam.position.z) / 10.0f;

        //chunkManager.explode2(playerTransform.position, 15);
        playerTransform.position.z = chunkManager.getVal(playerTransform.position.x, playerTransform.position.y) + 145;
        playerTransform.transform(player);

        //cam.position.set(virtualcam.x, virtualcam.y - 50, cam.position.z);
        //cam.lookAt(cam.position.x, cam.position.y + 200, 0);
        cam.position.set(virtualcam.x, virtualcam.y, cam.position.z);
        cam.update();

        //Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClearColor(0.251f, 0.643f, 0.875f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        //downs.begin();
        mb.begin(cam);
        mb.render(player);
        //Entity.render(mb);
        mb.end();
        chunkManager.render(cam, virtualcam);
        //downs.end();
        //downs.draw(viewport.getScreenWidth(), viewport.getScreenHeight());
        drawThumbs();
    }

    private void drawThumbs() {
        spritebatch.begin();
        int size = 50;
        if (firstPointer.isDown)
            spritebatch.draw(thumbTex, firstPointer.startx - size, viewport.getScreenHeight() - firstPointer.starty - size, size * 2, size * 2, 0, 0, 1, 1);
        if (secondPointer.isDown)
            spritebatch.draw(thumbTex, secondPointer.startx - size, viewport.getScreenHeight() - secondPointer.starty - size, size * 2, size * 2, 0, 0, 1, 1);
        if (thirdPointer.isDown) {
            spritebatch.draw(thumbTex, thirdPointer.startx - size, viewport.getScreenHeight() - thirdPointer.starty - size, size * 2, size * 2, 0, 0, 1, 1);
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
    }

    public static class SaveMouse {
        private final int index;
        private final Viewport v;
        boolean isDown = false;
        int startx = 0;
        int starty = 0;

        public SaveMouse(int index, Viewport v) {
            this.index = index;
            this.v = v;
        }

        public boolean update() {
            int width = v.getScreenWidth();
            int height = v.getScreenHeight();
            if (Gdx.input.isTouched(index)) {
                if (!isDown) {
                    int currentx = Gdx.input.getX(index);
                    int currenty = Gdx.input.getY(index);
                    if (Math.abs(currentx - width / 2) < width / 2 - 100
                            && Math.abs(currenty - height / 2) < height / 2 - 100) {
                        startx = currentx;
                        starty = currenty;
                    }
                }
                isDown = true;
            } else {
                isDown = false;
            }
            return isDown;
        }

        public int relx() {
            int ret = (Gdx.input.getX(index) - startx);
            return ret;
        }

        public int rely() {
            int ret = (Gdx.input.getY(index) - starty);
            return ret;
        }

        public float getRadiant() {
            return (float) Math.atan2(-rely(), relx());
        }

    }

    public static class Transform {
        public Vector3 position = new Vector3();
        public float scale = 4;
        public float radiant;

        public void transform(ModelInstance player) {
            player.transform.setToTranslation(position);
            player.transform.scl(scale);
            player.transform.rotateRad(UPAXIS, radiant);
        }
    }
}