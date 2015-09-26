package com.westreicher.birdsim.artemis;

import com.artemis.BaseSystem;
import com.artemis.InvocationStrategy;
import com.artemis.World;
import com.artemis.utils.Bag;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.util.Util;

/**
 * Created by david on 9/26/15.
 */
public class FixedTimestepStrategy extends InvocationStrategy {
    private static final int MAX_FRAME_SKIPS = 5;
    private long nextTick;
    private float interpolation;
    private long currenttick;
    private float fps;
    private long skipTicks;

    public FixedTimestepStrategy() {
        nextTick = System.nanoTime(); //System.currentTimeMillis() * 1000000L;//
        interpolation = 0;
        currenttick = 0;
        setFPS(Config.LOGIC_FPS);
    }

    private void setFPS(int fps) {
        this.fps = Math.max(fps, 1);
        skipTicks = (long) (1000000000f / fps);
    }

    @Override
    protected void process(Bag<BaseSystem> systems) {
        int loops = 0;
        long currentTime = System.nanoTime();
        //TODO GWT compat??
        //long currentTime = System.currentTimeMillis() * 1000000L;
        while (currentTime > nextTick && loops < MAX_FRAME_SKIPS) {
            process(systems, true);
            nextTick += skipTicks;
            loops++;
            currenttick++;
            //TODO GWT compat??
            currentTime = System.nanoTime();
            //currentTime = System.currentTimeMillis() * 1000000L;
        }
        interpolation = (float) (currentTime + skipTicks - nextTick) / skipTicks;
        world.setDelta(interpolation);
        process(systems, false);
    }

    private void process(Bag<BaseSystem> systems, boolean logics) {
        Object[] systemsData = systems.getData();
        for (int i = 0, s = systems.size(); s > i; i++) {
            updateEntityStates();

            BaseSystem system = (BaseSystem) systemsData[i];
            if (!system.isPassive()) {
                Class clss = system.getClass();
                if (logics) {
                    if (Util.contains(Artemis.LOGIC_SYSTEMS, clss)) system.process();
                } else if (!Util.contains(Artemis.LOGIC_SYSTEMS, clss)) system.process();
            }
        }
    }
}
