package com.westreicher.birdsim;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.entities.EntityManager;
import com.westreicher.birdsim.util.InputHelper;
import com.westreicher.birdsim.util.ManagedRessources;
import com.westreicher.birdsim.util.RenderToTexture.DownSampler;
import com.westreicher.birdsim.util.SoundPlayer;

public class MyGdxGame extends ApplicationAdapter {
    public static boolean isDesktop;
    private Camera cam;
    ModelBatch mb;
    Viewport viewport;
    public ChunkManager chunkManager;
    private FPSLogger fps = new FPSLogger();
    private SpriteBatch spritebatch;
    private Texture thumbTex;
    private DownSampler downs = null;
    public static MyGdxGame single = null;
    private Artemis artemis;

    private SoundPlayer soundplayer;
    private GameLoop gameloop;
    public com.westreicher.birdsim.entities.EntityManager entitymanager;

    private enum State {
        PAUSE,
        RUN
    }

    private State state = State.RUN;

    @Override
    public void resize(int width, int height) {
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
        cam = new PerspectiveCamera();
        cam.position.set(0, 0, 25 * (Config.DEBUG ? 10 : 1.25f));
        cam.near = 10f;
        cam.far = 500f;
        viewport = new ScreenViewport(cam);
        spritebatch = new SpriteBatch();
        thumbTex = new Texture(Gdx.files.internal("thumb.png"));
        mb = new ModelBatch();
        chunkManager = new ChunkManager();
        entitymanager = new EntityManager();
        soundplayer = new SoundPlayer();
        InputHelper.init(isDesktop, viewport);
        gameloop = new GameLoop(cam, chunkManager);
        artemis = Artemis.init();
    }


    @Override
    public void render() {
        //fps.log();

        switch (state) {
            case RUN:
                gameloop.tick();
                break;
            case PAUSE:
                gameloop.resetInterp();
                break;
            default:
                break;
        }

        float interp = gameloop.getInterp();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        //Gdx.gl.glClearColor(0.251f, 0.643f, 0.875f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if (isDesktop) downs.begin();
        mb.begin(cam);
        entitymanager.render(mb, interp);
        mb.end();
        chunkManager.render(cam);
        if (isDesktop) {
            downs.end();
            downs.draw(viewport.getScreenWidth(), viewport.getScreenHeight());
        } else { // TODO isTouch?
            drawThumbs(viewport.getScreenWidth(), viewport.getScreenHeight());
        }

        if (!InputHelper.players.get(0).firstPointer.update() && !InputHelper.players.get(0).secondPointer.update()) {
            setGameState(State.PAUSE);
        } else if (state != State.RUN) {
            setGameState(State.RUN);
        }

        drawLives();
        artemis.process();
    }

    private void drawLives() {
        spritebatch.begin();
        int size = 50;
        for (int i = 0; i < entitymanager.aliveplayers.size(); i++) {
            com.westreicher.birdsim.entities.Entity ent = entitymanager.aliveplayers.arr[i];
            for (int j = 0; j < ent.lives; j++) {
                spritebatch.draw(thumbTex, size * j, size * i, size * 2, size * 2);
            }
        }
        spritebatch.end();
    }

    public void setGameState(State s) {
        this.state = s;
    }

    private void drawThumbs(int w, int h) {
        InputHelper firstPointer = InputHelper.players.get(0).firstPointer;
        InputHelper secondPointer = InputHelper.players.get(0).secondPointer;
        InputHelper thirdPointer = InputHelper.players.get(0).thirdPointer;

        int size = 50;

        spritebatch.begin();
        spritebatch.draw(thumbTex, firstPointer.getStartX(w) - size, h - firstPointer.getStartY(h) - size, size * 2, size * 2, 0, 0, 1, 1);
        spritebatch.draw(thumbTex, secondPointer.getStartX(w) - size, h - secondPointer.getStartY(h) - size, size * 2, size * 2, 0, 0, 1, 1);
        if (thirdPointer.isDown()) {
            spritebatch.draw(thumbTex, thirdPointer.getStartX(w) - size, h - thirdPointer.getStartY(h) - size, size * 2, size * 2, 0, 0, 1, 1);
        }
        spritebatch.end();
    }

    @Override
    public void dispose() {
        ManagedRessources.dispose();
        entitymanager.dispose();
        mb.dispose();
        Gdx.app.log("game", "dispose");
        chunkManager.dispose();
        soundplayer.dispose();
        artemis.dispose();
    }

    public void playSound(SoundPlayer.Sounds s, Vector3 pos) {
        soundplayer.play(s, pos);
    }
}
