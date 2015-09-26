package com.westreicher.birdsim.artemis;

import com.artemis.BaseSystem;
import com.artemis.InvocationStrategy;
import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.utils.Bag;
import com.westreicher.birdsim.artemis.systems.*;

/**
 * Created by david on 9/25/15.
 */
public class Artemis extends World {

    public static final Class[] LOGIC_SYSTEMS = new Class[]{MovementSystem.class};

    private Artemis(WorldConfiguration config) {
        super(config);
    }

    public static Artemis init() {
        WorldConfiguration config = new WorldConfiguration();
        config.setSystem(MovementSystem.class);
        config.setSystem(TextRendering.class);
        Artemis a = new Artemis(config);
        a.setInvocationStrategy(new FixedTimestepStrategy());
        return a;
    }

}
