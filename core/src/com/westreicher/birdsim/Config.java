package com.westreicher.birdsim;

/**
 * Created by david on 9/17/15.
 */
public class Config {
    public static final boolean POST_PROCESSING = true;
    public static final boolean DEBUG = true;
    public static final int TILES_PER_CHUNK = 32;
    public static final int CHUNKNUMS = (MyGdxGame.isDesktop ? 5 : 4) * 2 + 1;
    public static final float MOVE_SPEED = 75f;
    public static final float TERRAIN_HEIGHT = 15f;
}
