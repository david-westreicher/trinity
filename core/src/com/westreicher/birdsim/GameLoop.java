package com.westreicher.birdsim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.westreicher.birdsim.util.InputHelper;

/**
 * Created by david on 9/21/15.
 */
public class GameLoop {
    private final ChunkManager chunkManager;
    private final InputHelper thirdPointer;
    private final MyGdxGame.Transform playerTransform;
    private final InputHelper firstPointer;
    private final InputHelper secondPointer;
    private PerspectiveCamera cam;
    private Vector3 virtualcam;
    private long skipTicks = 33333333;
    private float interpolation;
    private long nextTick;
    private int MAX_FRAME_SKIPS = 5;
    private long currenttick;
    private float fps;

    public GameLoop(Vector3 virtualcam, PerspectiveCamera cam, ChunkManager chunkManager, MyGdxGame.Transform playerTransform, InputHelper firstPointer, InputHelper secondPointer, InputHelper thirdPointer) {
        this.virtualcam = virtualcam;
        this.cam = cam;
        this.chunkManager = chunkManager;
        this.playerTransform = playerTransform;
        this.firstPointer = firstPointer;
        this.secondPointer = secondPointer;
        this.thirdPointer = thirdPointer;
        nextTick = System.nanoTime();
        interpolation = 0;
        currenttick = 0;
        setFPS(45);
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
            currentTime = System.nanoTime();
        }
        interpolation = (float) (currentTime + skipTicks - nextTick) / skipTicks;
    }

    private void logic() {
        float delta = 1f / fps;

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
        Entity.updateall(delta, currenttick);

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
            if (currenttick % 10 == 0) {
                Entity.shoot(playerTransform.position.x, playerTransform.position.y, xspeed, yspeed, Entity.ColorAttr.RED, null);
            }
        }
        virtualcam.x += (playerTransform.position.x + xspeed * 0 - virtualcam.x) / 10.0f;
        virtualcam.y += (playerTransform.position.y + yspeed * 0 - virtualcam.y) / 10.0f;
        cam.far = thirdPointer.update() ? 180 : 250;
        cam.position.z += (cam.far - cam.position.z) / 10.0f;

        //chunkManager.explode2(playerTransform.position, isDesktop ? 15 : 7);
        playerTransform.position.z = chunkManager.getVal(playerTransform.position.x, playerTransform.position.y) * Config.TERRAIN_HEIGHT + 145;

        //cam.position.set(virtualcam.x, virtualcam.y - 50, cam.position.z);
        //cam.lookAt(cam.position.x, cam.position.y + 200, 0);
        cam.position.set(virtualcam.x, virtualcam.y, cam.position.z);
        cam.update();
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
}
