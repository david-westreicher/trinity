package com.westreicher.birdsim.artemis.components;

import com.artemis.PooledComponent;

/**
 * Created by david on 9/11/16.
 */
public class ShipComponent extends PooledComponent {
    public int playerid;
    public int size;
    public float dir;
    public float movx;
    public float movy;
    public float speed;
    public float todir;

    @Override
    protected void reset() {
    }
}
