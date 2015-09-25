package com.westreicher.birdsim.artemis;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.westreicher.birdsim.artemis.systems.*;

/**
 * Created by david on 9/25/15.
 */
public class Artemis {
    private final World world;

    public Artemis() {
        WorldConfiguration config = new WorldConfiguration();
        config.setSystem(com.westreicher.birdsim.artemis.systems.MovementSystem.class);
        config.setSystem(TextRendering.class);
        world = new World(config);
    }

    public void tick() {
        //world.setDelta(0.1123f);
        //world.process();
    }

    public void dispose() {
        world.dispose();
    }
}
