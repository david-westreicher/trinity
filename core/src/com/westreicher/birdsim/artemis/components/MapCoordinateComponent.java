package com.westreicher.birdsim.artemis.components;

import com.artemis.PooledComponent;

/**
 * Created by david on 9/28/15.
 */
public class MapCoordinateComponent extends PooledComponent {
    public float x;
    public float y;

    @Override
    protected void reset() {
        x = 0;
        y = 0;
    }
}
