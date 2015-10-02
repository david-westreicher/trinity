package com.westreicher.birdsim.artemis;

import com.artemis.BaseSystem;
import com.artemis.InvocationStrategy;
import com.artemis.World;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by david on 9/26/15.
 */
public class FixedTimestepStrategy extends InvocationStrategy {
    private static final FPSLogger FPS_LOGGER = new FPSLogger();
    private static final int MAX_FRAME_SKIPS = 5;
    private final Map<Class<? extends BaseSystem>, Long> map = new HashMap<Class<? extends BaseSystem>, Long>();
    private World world;
    private long nextTick;
    private float interpolation;
    public long currenttick;
    private float fps;
    private long skipTicks;
    public boolean isPaused;

    public FixedTimestepStrategy(World world) {
        nextTick = System.nanoTime(); //System.currentTimeMillis() * 1000000L;//
        interpolation = 0;
        currenttick = 0;
        this.world = world;
        setFPS(Config.LOGIC_FPS);
        for (BaseSystem bs : world.getSystems()) {
            map.put(bs.getClass(), 0L);
        }
    }

    private void setFPS(int fps) {
        this.fps = Math.max(fps, 1);
        skipTicks = (long) (1000000000f / fps);
    }

    @Override
    protected void process(Bag<BaseSystem> systems) {
        if (!isPaused) {
            updateLogicIfNecc(systems);
            interpolation = (float) (System.nanoTime() + skipTicks - nextTick) / skipTicks;
            world.setDelta(interpolation);
        }
        process(systems, false);
    }

    private void updateLogicIfNecc(Bag<BaseSystem> systems) {
        int loops = 0;
        long currentTime = System.nanoTime();
        //TODO GWT compat??
        //long currentTime = System.currentTimeMillis() * 1000000L;
        while (currentTime > nextTick && loops < MAX_FRAME_SKIPS) {
            process(systems, true);
            FPS_LOGGER.log();
            nextTick += skipTicks;
            loops++;
            currenttick++;
            if (Config.PROFILE && currenttick % 600 == 0)
                logProfiler();
            //TODO GWT compat??
            currentTime = System.nanoTime();
            //currentTime = System.currentTimeMillis() * 1000000L;
        }
        if (loops > 1 && Config.DEBUG)
            Gdx.app.log("FRAMESKIP", "" + (loops - 1));

    }

    private void logProfiler() {
        long sum = 0;
        for (Map.Entry<Class<? extends BaseSystem>, Long> e : map.entrySet()) {
            sum += e.getValue() / 1000;
        }
        if (sum <= 0)
            return;
        for (Map.Entry<Class<? extends BaseSystem>, Long> e : Util.sortByValue(map).entrySet()) {
            Gdx.app.log(String.format("%.2f", ((double) (e.getValue() / 1000)) / sum) + "\t", e.getKey().getSimpleName());
        }
        for (Class<? extends BaseSystem> e : map.keySet()) {
            map.put(e, 0L);
        }
    }

    private void process(Bag<BaseSystem> systems, boolean logics) {
        Object[] systemsData = systems.getData();
        for (int i = 0, s = systems.size(); s > i; i++) {
            updateEntityStates();

            BaseSystem system = (BaseSystem) systemsData[i];
            Class clss = system.getClass();
            boolean shouldrun = false;
            if (logics) {
                if (Artemis.LOGIC_SYSTEMS.contains(clss)) {
                    shouldrun = true;
                }
            } else if (!Artemis.LOGIC_SYSTEMS.contains(clss)) {
                shouldrun = true;
            }
            if (shouldrun) {
                long start = System.nanoTime();
                system.process();
                long duration = System.nanoTime() - start;
                long current = map.get(clss);
                map.put(clss, current + duration);
            }
        }
    }

    public void pauseLogic() {
        isPaused = true;
    }

    public void continueLogic() {
        nextTick = System.nanoTime();
        isPaused = false;
    }
}
