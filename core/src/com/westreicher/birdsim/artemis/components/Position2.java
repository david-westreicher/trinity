package com.westreicher.birdsim.artemis.components;

import com.artemis.Component;

/**
 * Created by david on 9/25/15.
 */
public class Position2 extends Component {
    public float x;
    public float y;

    public Position2() {
    }

    public Position2(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
