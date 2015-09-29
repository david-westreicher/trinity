package com.westreicher.birdsim.artemis.components;

import com.artemis.PooledComponent;

/**
 * Created by david on 9/29/15.
 */
public class Health extends PooledComponent {
    public int health = 10;

    @Override
    protected void reset() {
        health = 10;
    }
}
