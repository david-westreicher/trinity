package com.westreicher.birdsim;

import com.badlogic.gdx.math.Vector3;

/**
 * Created by david on 9/17/15.
 */
public class Config {
    public static final int LOGIC_FPS = 50;
    public static final float SPHERE_RADIUS = 140;
    public static final float SPHERE_RADIUS_SQUARED = (float) Math.pow(SPHERE_RADIUS, 2);
    public static final boolean POST_PROCESSING = true;
    public static final boolean DEBUG = true;
    public static final int TILES_PER_CHUNK = 40;
    public static final int CHUNKNUMS = 5 * 2 + 1;
    public static final float MOVE_SPEED = 1f;
    public static final float TERRAIN_HEIGHT = 8;
    public static final boolean SPAWN_STUFF = true;
    public static final Vector3 UPAXIS = new Vector3(0, 0, 1);
    public static final boolean PROFILE = true;
    public static final boolean FIXED_CAM = false;
    public static final boolean DRAW_SHADOWS = false;
    public static final boolean FIRST_PERSON = false;
    //the chunk-state updates per tick, 10 for performant systems (desktop,...?)
    //TODO make gui with slider (1-10)
    public static final int CHUNK_UPDATES_PER_TICK = 1;
    public static final boolean ALLOW_NEGATIVE_CHUNK_DATA = false;
    public static final int CHUNK_DRAW_DISTANCE = 3;
    public static boolean IS_DESKTOP = true;
    public static Chunk.Renderstyle CHUNK_RENDER_STYLE = Chunk.Renderstyle.TERRAIN;
}
