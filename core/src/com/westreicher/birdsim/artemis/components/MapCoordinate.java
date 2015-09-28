package com.westreicher.birdsim.artemis.components;

import com.artemis.Component;

/**
 * Created by david on 9/28/15.
 */
public class MapCoordinate extends Component {
    public float x;
    public float y;

    public MapCoordinate() {
    }

    public MapCoordinate(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
