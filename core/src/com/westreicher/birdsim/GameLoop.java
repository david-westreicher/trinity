package com.westreicher.birdsim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.westreicher.birdsim.entities.EntityManager;
import com.westreicher.birdsim.util.InputHelper;
import com.westreicher.birdsim.util.InterpVec3;

/**
 * Created by david on 9/21/15.
 */
public class GameLoop {
    private final ChunkManager chunkManager;
    private final EntityManager manager;
    private Camera cam;
    private Vector3 playermids = new Vector3();
    private long skipTicks = 33333333;
    private float interpolation;
    private long nextTick;
    private int MAX_FRAME_SKIPS = 5;
    private long currenttick;
    private float fps;
    public InterpVec3 virtualcam = new InterpVec3();

    public GameLoop(Camera cam, ChunkManager chunkManager) {
        this.cam = cam;
        this.chunkManager = chunkManager;
        nextTick = System.nanoTime(); //System.currentTimeMillis() * 1000000L;//
        interpolation = 0;
        currenttick = 0;
        setFPS(Config.LOGIC_FPS);
        Gdx.app.log("player", "" + InputHelper.players.size());
        manager = MyGdxGame.single.entitymanager;
        for (int i = 0; i < InputHelper.players.size(); i++) {
            manager.spawnPlayer(i);
        }
    }

    public void setFPS(float fps) {
        this.fps = Math.max(fps, 1);
        skipTicks = (long) (1000000000f / fps);
    }

    public void tick() {
        int loops = 0;
        long currentTime = System.nanoTime();
        //TODO GWT compat??
        //long currentTime = System.currentTimeMillis() * 1000000L;
        while (currentTime > nextTick && loops < MAX_FRAME_SKIPS) {
            logic();
            nextTick += skipTicks;
            loops++;
            currenttick++;
            //TODO GWT compat??
            currentTime = System.nanoTime();
            //currentTime = System.currentTimeMillis() * 1000000L;
        }
        interpolation = (float) (currentTime + skipTicks - nextTick) / skipTicks;
        Vector3 interpvirtual = virtualcam.getInterppos(interpolation);
        cam.position.set(interpvirtual.x, interpvirtual.y, cam.position.z);
        cam.update();
    }

    private void logic() {
        int dx = 0;
        int dy = 0;
        if (Math.abs(virtualcam.pos.x) > Config.TILES_PER_CHUNK / 2.0)
            dx = (int) Math.signum(virtualcam.pos.x);
        if (Math.abs(virtualcam.pos.y) > Config.TILES_PER_CHUNK / 2.0)
            dy = (int) Math.signum(virtualcam.pos.y);
        virtualcam.pos.x -= dx * Config.TILES_PER_CHUNK;
        virtualcam.pos.y -= dy * Config.TILES_PER_CHUNK;
        manager.translateAll(-dx * Config.TILES_PER_CHUNK, -dy * Config.TILES_PER_CHUNK);
        chunkManager.updateDirection(dx, dy);
        manager.updateall(currenttick, virtualcam.pos);

        playermids.set(0, 0, 0);
        for (int i = 0; i < manager.aliveplayers.size(); i++) {
            com.westreicher.birdsim.entities.Entity player = manager.aliveplayers.arr[i];
            playermids.add(player.pos);
        }
        if (manager.aliveplayers.size() > 0)
            playermids.scl(1f / manager.aliveplayers.size());

        virtualcam.resetOldPos();
        virtualcam.pos.x += (playermids.x - virtualcam.pos.x) / 10.0f;
        virtualcam.pos.y += (playermids.y - virtualcam.pos.y) / 10.0f;
        cam.far = 250;
        cam.position.z += (cam.far - 1 - cam.position.z) / 10.0f;

        //chunkManager.explode2(playerTransform.position, isDesktop ? 15 : 7);

        //cam.position.set(virtualcam.x, virtualcam.y - 50, cam.position.z);
        //cam.lookAt(cam.position.x, cam.position.y + 200, 0);
    }

    private float getInterpolation() {
        return interpolation;
    }

    public float getInterp() {
        return interpolation;
    }
}
