package com.westreicher.birdsim;

/**
 * Created by david on 9/17/15.
 */
public class Config {
    public static final boolean POST_PROCESSING = true;
    public static final boolean DEBUG = true;
    public static final int TILES_PER_CHUNK = 16;
    public static final int CHUNKNUMS = (MyGdxGame.isDesktop ? 11 : 8) * 2 + 1;
    public static final float MOVE_SPEED = 1f;
}
