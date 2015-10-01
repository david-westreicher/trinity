package com.westreicher.birdsim.artemis.components;

import com.artemis.PooledComponent;

/**
 * Created by david on 10/1/15.
 */
public class Collidable extends PooledComponent {
    public float scale = 1;

    @Override
    protected void reset() {
        scale = 1;
    }
}
