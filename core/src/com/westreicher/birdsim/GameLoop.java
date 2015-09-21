package com.westreicher.birdsim;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.westreicher.birdsim.util.InputHelper;

/**
 * Created by david on 9/21/15.
 */
public class GameLoop {
    private final ChunkManager chunkManager;
    private final Entity player;
    private PerspectiveCamera cam;
    private Vector3 virtualcam;
    private long skipTicks = 33333333;
    private float interpolation;
    private long nextTick;
    private int MAX_FRAME_SKIPS = 5;
    private long currenttick;
    private float fps;

    public GameLoop(Vector3 virtualcam, PerspectiveCamera cam, ChunkManager chunkManager) {
        this.virtualcam = virtualcam;
        this.cam = cam;
        this.chunkManager = chunkManager;
        nextTick = System.nanoTime();
        interpolation = 0;
        currenttick = 0;
        setFPS(50);
        player = Entity.spawnPlayer();
    }

    public void setFPS(float fps) {
        this.fps = Math.max(fps, 1);
        skipTicks = (long) (1000000000f / fps);
    }

    public void tick() {
        int loops = 0;
        long currentTime = System.nanoTime();
        //TODO GWT compat??
        //long currentTime =  System.currentTimeMillis()*1000000L;
        while (currentTime > nextTick && loops < MAX_FRAME_SKIPS) {
            logic();
            nextTick += skipTicks;
            loops++;
            currenttick++;
            //TODO GWT compat??
            currentTime = System.nanoTime();
        }
        interpolation = (float) (currentTime + skipTicks - nextTick) / skipTicks;
    }

    private void logic() {
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
        Entity.updateall(currenttick);

        virtualcam.x += (player.pos.x - virtualcam.x) / 10.0f;
        virtualcam.y += (player.pos.y - virtualcam.y) / 10.0f;
        cam.far = InputHelper.thirdPointer.update() ? 180 : 250;
        cam.position.z += (cam.far - cam.position.z) / 10.0f;

        //chunkManager.explode2(playerTransform.position, isDesktop ? 15 : 7);

        //cam.position.set(virtualcam.x, virtualcam.y - 50, cam.position.z);
        //cam.lookAt(cam.position.x, cam.position.y + 200, 0);
        cam.position.set(virtualcam.x, virtualcam.y, cam.position.z);
        cam.update();
    }

    private float getInterpolation() {
        return interpolation;
    }
}
