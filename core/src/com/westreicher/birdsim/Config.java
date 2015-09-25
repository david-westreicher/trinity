package com.westreicher.birdsim;

import com.badlogic.gdx.math.Vector3;

/**
 * Created by david on 9/17/15.
 */
public class Config {
    public static final int LOGIC_FPS = 50;
    public static final boolean POST_PROCESSING = true;
    public static final boolean DEBUG = true;
    public static final int TILES_PER_CHUNK = 40;
    public static final int CHUNKNUMS = 4 * 2 + 1;
    public static final float MOVE_SPEED = 1f;
    public static final float TERRAIN_HEIGHT = 15f;
    public static final boolean SPAWN_STUFF = true;
    public static final Vector3 UPAXIS = new Vector3(0, 0, 1);
}
