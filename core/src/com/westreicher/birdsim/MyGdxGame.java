package com.westreicher.birdsim;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;

public class MyGdxGame extends ApplicationAdapter {
    public static final Vector3 UPAXIS = new Vector3(0, 0, 1);
    PerspectiveCamera cam;
    ModelBatch mb;
    Viewport viewport;
    static float SIZE = 20;
    private float rat = 1;
    private int frame = 0;
    private static boolean isDesktop;
    public static boolean DEBUG = false;
    private ModelInstance player;
    private Vector3 playertrans = new Vector3();
    private ModelInstance gun;
    private SaveMouse firstPointer;
    private SaveMouse secondPointer;
    private ArrayList<Bullet> alivebullets = new ArrayList<Bullet>();
    private Pool<Bullet> bullets = new Pool<Bullet>() {
        @Override
        protected Bullet newObject() {
            return new Bullet();
        }
    };
    private Vector3 enemy = new Vector3();
    private ChunkManager chunkManager;


    @Override
    public void resize(int width, int height) {
        rat = 1.0f / Math.min(width, height);
        viewport.update(width, height);
    }

    @Override
    public void create() {
        Gdx.app.log("game", "GL ES 3.0 supported: " + (Gdx.gl30 != null));
        isDesktop = Gdx.app.getType() == Application.ApplicationType.Desktop;
        DefaultShader.defaultCullFace = 0;
        cam = new PerspectiveCamera();
        cam.position.set(0, 0, 25 * (DEBUG ? 4 : 1));
        cam.near = 0.1f;
        cam.far = 100f;
        viewport = new ScreenViewport(cam);
        mb = new ModelBatch();
        chunkManager = new ChunkManager();
        player = new ModelInstance(new ModelBuilder().createSphere(2, 2, 2, 10, 10, new Material(ColorAttribute.createDiffuse(1, 1, 1, 1)), VertexAttributes.Usage.Position));
        gun = new ModelInstance(new ModelBuilder().createBox(0.5f, 2f, 0.5f, new Material(ColorAttribute.createDiffuse(1, 0, 0, 0)), VertexAttributes.Usage.Position));
        firstPointer = new SaveMouse(0);
        secondPointer = new SaveMouse(1);
    }


    public void movePlayer(float x, float y) {
        playertrans.x += x;
        playertrans.y -= y;
        if (!chunkManager.isStuck(playertrans, 1) || DEBUG) {
            player.transform.setTranslation(playertrans);
            return;
        }
        playertrans.y += y;
        if (!chunkManager.isStuck(playertrans, 1)) {
            player.transform.setTranslation(playertrans);
            return;
        }
        playertrans.x -= x;
        playertrans.y -= y;
        if (!chunkManager.isStuck(playertrans, 1)) {
            player.transform.setTranslation(playertrans);
            return;
        }
    }

    @Override
    public void render() {

        int dx = 0;
        int dy = 0;
        if (Math.abs(cam.position.x) > SIZE / 2)
            dx = (int) Math.signum(cam.position.x);
        if (Math.abs(cam.position.y) > SIZE / 2)
            dy = (int) Math.signum(cam.position.y);
        cam.position.x -= dx * SIZE;
        cam.position.y -= dy * SIZE;
        player.transform.translate(-dx * SIZE, -dy * SIZE, 0);
        for (Bullet b : alivebullets)
            b.position.add(-dx * SIZE, -dy * SIZE, 0);

        playertrans = player.transform.getTranslation(playertrans);
        if (firstPointer.update()) {
            int mousex = firstPointer.relx();
            int mousey = firstPointer.rely();
            movePlayer(mousex * rat * 1.0f, mousey * rat * 1.0f);
        }
        //Gdx.app.log("game", "" +"");
        cam.position.x += (playertrans.x - cam.position.x) / 5.0f;
        cam.position.y += (playertrans.y - cam.position.y) / 5.0f;

        if (secondPointer.update() || isDesktop) {
            //Gdx.app.log("game", secondPointer.rely() + "," + secondPointer.relx());
            int relx = isDesktop ? 0 : secondPointer.relx();
            int rely = isDesktop ? 1 : secondPointer.rely();

            double radiant = Math.atan2(-rely, relx);
            float degree = (float) (radiant * 180 / Math.PI) + 90;
            gun.transform.setToRotation(UPAXIS, degree);
            gun.transform.setTranslation(playertrans);
            gun.transform.translate(0, -1.1f, 0);//secondPointer.relx()*rat,secondPointer.rely()*rat,0);
            if (frame % 5 == 0) {
                Bullet newb = bullets.obtain();
                newb.init(playertrans.x, playertrans.y, (float) Math.cos(radiant), (float) Math.sin(radiant), degree);
                alivebullets.add(newb);
            }
        } else {
            gun.transform.translate(-dx * SIZE, -dy * SIZE, 0);
        }

        for (int i = 0; i < alivebullets.size(); i++) {
            Bullet b = alivebullets.get(i);
            b.update(0);
            boolean dead = false;
            if (chunkManager.isStuck(b.position, 0)) {
                dead = true;
                chunkManager.explode(b.position, false);
            }
            if (dead || Math.abs(b.position.x) > SIZE * 3 || Math.abs(b.position.y) > SIZE * 3) {
                alivebullets.remove(i);
                bullets.free(b);
            }
        }
        //Chunk.explodes(chunks, enemy, true);

        chunkManager.updateDirection(dx, dy);

        cam.update();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        mb.begin(cam);
        mb.render(player);
        mb.render(gun);
        for (Bullet b : alivebullets)
            mb.render(b.modelInstance);
        chunkManager.render(mb);
        mb.end();

    }

    @Override
    public void dispose() {
        mb.dispose();
        chunkManager.dispose();
        super.dispose();
    }

    public static class SaveMouse {
        private final int index;
        boolean isDown = false;
        int startx = 0;
        int starty = 0;
        boolean saver = true;

        public SaveMouse(int index) {
            this.index = index;
        }

        public boolean update() {
            if (Gdx.input.isTouched(index)) {
                if (!isDown) {
                    startx = Gdx.input.getX(index);
                    starty = Gdx.input.getY(index);
                }
                isDown = true;
            } else {
                isDown = false;
            }
            return isDown;
        }

        public int relx() {
            int ret = (Gdx.input.getX(index) - startx);
            if (!saver) {
                startx = Gdx.input.getX(index);
            }
            return ret;
        }

        public int rely() {
            int ret = (Gdx.input.getY(index) - starty);
            if (!saver)
                starty = Gdx.input.getY(index);
            return ret;
        }

    }
}