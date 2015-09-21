package com.westreicher.birdsim;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
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
import com.westreicher.birdsim.util.SoundPlayer;

public class MyGdxGame extends ApplicationAdapter {
    public static boolean isDesktop;
    public static final Vector3 UPAXIS = new Vector3(0, 0, 1);
    private PerspectiveCamera cam;
    ModelBatch mb;
    Viewport viewport;
    private float rat = 1;
    private ModelInstance player;
    private ModelInstance gun;
    private RelInput firstPointer;
    private RelInput secondPointer;
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
        // player = new ModelInstance(new ModelBuilder().createSphere(2, 2, 2, 10, 10, new Material(ColorAttribute.createDiffuse(1, 1, 1, 1)), VertexAttributes.Usage.Position));
        player = new ModelInstance(new ObjLoader().loadModel(Gdx.files.internal("player.obj")));
        player.materials.get(0).set(ColorAttribute.createDiffuse(Color.RED));
        //player = new ModelInstance(new ModelBuilder().createBox(1f, 1f, 1f, new Material(ColorAttribute.createDiffuse(1, 0, 0, 0)), VertexAttributes.Usage.Position));
        gun = new ModelInstance(new ModelBuilder().createBox(0.2f, 0.5f, 0.2f, new Material(ColorAttribute.createDiffuse(1, 0, 0, 0)), VertexAttributes.Usage.Position));
        firstPointer = isDesktop ? new Keyboard(0) : new SaveMouse(0, viewport);
        secondPointer = isDesktop ? new Keyboard(1) : new SaveMouse(1, viewport);
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
        Entity.translateAll(-dx * Config.TILES_PER_CHUNK, -dy * Config.TILES_PER_CHUNK);
        chunkManager.updateDirection(dx, dy);
        Entity.updateall(delta);
        playerTransform.position.add(-dx * Config.TILES_PER_CHUNK, -dy * Config.TILES_PER_CHUNK, 0);

        if (firstPointer.update()) {
            int mousex = firstPointer.relx();
            int mousey = firstPointer.rely();
            float rad = firstPointer.getRadiant();
            playerTransform.radiant = rad;
            //movePlayer(mousex * delta * Config.MOVE_SPEED, mousey * delta * Config.MOVE_SPEED);
            movePlayer((float) Math.cos(rad) * delta * Config.MOVE_SPEED, -(float) Math.sin(rad) * delta * Config.MOVE_SPEED);
        }
        //Gdx.app.log("game", "" +"");
        virtualcam.x += (playerTransform.position.x - virtualcam.x) / 5.0f;
        virtualcam.y += (playerTransform.position.y - virtualcam.y) / 5.0f;
        if (secondPointer.update()) {
            //Gdx.app.log("game", secondPointer.rely() + "," + secondPointer.relx());
            int relx = isDesktop ? 0 : secondPointer.relx();
            int rely = isDesktop ? 1 : secondPointer.rely();
            float radiant = secondPointer.getRadiant();
            gun.transform.setToRotationRad(UPAXIS, radiant + (float) Math.PI / 2);
            gun.transform.setTranslation(playerTransform.position);
            gun.transform.translate(0, -0.4f, 0);//secondPointer.relx()*rat,secondPointer.rely()*rat,0);
            if (Gdx.graphics.getFrameId() % 10 == 0) {
                Entity.shoot(playerTransform.position.x, playerTransform.position.y, (float) Math.cos(radiant), (float) Math.sin(radiant), Entity.ColorAttr.RED, null);
            }
        } else {
            gun.transform.setToRotation(UPAXIS, 0);
            gun.transform.translate(-dx * Config.TILES_PER_CHUNK, -dy * Config.TILES_PER_CHUNK, 0);
        }
        cam.position.z += ((thirdPointer.update() ? 200 : 250) - cam.position.z) / 10.0f;

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
        }
        drawThumbs();
    }

    private void drawThumbs() {
        spritebatch.begin();
        int size = 50;
        if (firstPointer.isDown())
            spritebatch.draw(thumbTex, firstPointer.getStartX() - size, viewport.getScreenHeight() - firstPointer.getStartY() - size, size * 2, size * 2, 0, 0, 1, 1);
        if (secondPointer.isDown())
            spritebatch.draw(thumbTex, secondPointer.getStartX() - size, viewport.getScreenHeight() - secondPointer.getStartY() - size, size * 2, size * 2, 0, 0, 1, 1);
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
        soundplayer.dispose();
    }

    public void playSound(SoundPlayer.Sounds s, Vector3 pos) {
        soundplayer.play(s, pos);
    }

    public static interface RelInput {
        public boolean update();

        public int relx();

        public int rely();

        public boolean isDown();

        public float getRadiant();

        int getStartY();

        int getStartX();
    }

    public static class Keyboard implements RelInput {
        private static final int KEYS[][] = new int[][]{
                new int[]{Input.Keys.A, Input.Keys.LEFT},//LEFT
                new int[]{Input.Keys.W, Input.Keys.UP},//UP
                new int[]{Input.Keys.D, Input.Keys.RIGHT},//RIGHT
                new int[]{Input.Keys.S, Input.Keys.DOWN},//DOWN
        };
        private static final int DELTAS[][] = new int[][]{
                new int[]{-1, 0},//LEFT
                new int[]{0, 1},//UP
                new int[]{1, 0},//UP
                new int[]{0, -1},//DOWN
        };
        private final int controls;
        private int deltay;
        private int deltax;
        private boolean isDown;

        public Keyboard(int controls) {
            this.controls = controls;
        }

        @Override
        public boolean update() {
            deltax = 0;
            deltay = 0;
            for (int i = 0; i < KEYS.length; i++)
                if (Gdx.input.isKeyPressed(KEYS[i][controls])) {
                    deltax += DELTAS[i][0];
                    deltay += DELTAS[i][1];
                }
            isDown = deltax != 0 || deltay != 0;
            return isDown;
        }

        @Override
        public int relx() {
            return deltax;
        }

        @Override
        public int rely() {
            return -deltay;
        }

        @Override
        public boolean isDown() {
            return isDown;
        }

        @Override
        public float getRadiant() {
            return (float) Math.atan2(-rely(), relx());
        }

        @Override
        public int getStartY() {
            return -100;
        }

        @Override
        public int getStartX() {
            return -100;
        }
    }

    public static class SaveMouse implements RelInput {
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


        @Override
        public boolean isDown() {
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

        @Override
        public int getStartY() {
            return starty;
        }

        @Override
        public int getStartX() {
            return startx;
        }

    }

    public static class Transform {
        public Vector3 position = new Vector3();
        public float scale = 1;
        public float radiant;

        public void transform(ModelInstance player) {
            player.transform.setToTranslation(position);
            player.transform.scl(scale);
            player.transform.rotateRad(UPAXIS, radiant);
        }
    }
}