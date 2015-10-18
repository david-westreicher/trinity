package com.westreicher.birdsim.artemis;

import com.artemis.BaseSystem;
import com.artemis.InvocationStrategy;
import com.artemis.World;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.UI.Settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by david on 9/26/15.
 */
public class FixedTimestepStrategy extends InvocationStrategy {
    private static final FPSLogger FPS_LOGGER = new FPSLogger();
    private static final int MAX_FRAME_SKIPS = 5;
    private static final Comparator<ProfileInfo> PROFILE_COMP = new Comparator<ProfileInfo>() {
        @Override
        public int compare(ProfileInfo p1, ProfileInfo p2) {
            return -Double.compare(p1.percent, p2.percent);
        }
    };
    private final Map<Class<? extends BaseSystem>, ProfileInfo> profileCounterMap = new HashMap<Class<? extends BaseSystem>, ProfileInfo>();
    private World world;
    private long nextTick;
    private float interpolation;
    public long currenttick;
    private float fps;
    private long skipTicks;
    public boolean isPaused;
    private List<ProfileInfo> profileVals = new ArrayList<ProfileInfo>();

    private static final Settings settings = new Settings();

    public FixedTimestepStrategy(World world) {
        nextTick = System.nanoTime(); //System.currentTimeMillis() * 1000000L;//
        interpolation = 0;
        currenttick = 0;
        this.world = world;
        setFPS(Config.LOGIC_FPS);
        for (BaseSystem bs : world.getSystems()) {
            Class<? extends BaseSystem> clss = bs.getClass();
            ProfileInfo pi = new ProfileInfo(clss);
            profileCounterMap.put(clss, pi);
            profileVals.add(pi);
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
        if ((currentTime - nextTick) / 1000000000 >= 5) {
            //probably debugging??
            Gdx.app.log("fixedtimestep", "reset nexttick");
            nextTick = currentTime - 1;
        }
        //TODO GWT compat??
        //long currentTime = System.currentTimeMillis() * 1000000L;
        while (currentTime > nextTick && loops < MAX_FRAME_SKIPS) {
            process(systems, true);
            nextTick += skipTicks;
            loops++;
            currenttick++;
            if (settings.isDebug()) {
                if (currenttick % 600 == 0)
                    logProfiler();
                FPS_LOGGER.log();
            }
            //TODO GWT compat??
            currentTime = System.nanoTime();
            //currentTime = System.currentTimeMillis() * 1000000L;
        }
        //if (loops > 1 && Config.PROFILE)
        //    Gdx.app.log("FRAMESKIP", "" + (loops - 1));
    }

    private void logProfiler() {
        long sum = 0;
        for (ProfileInfo pi : profileVals) {
            sum += pi.val / 1000;
        }
        if (sum <= 0)
            return;
        for (ProfileInfo pi : profileVals)
            pi.percent = (double) (pi.val / 1000) / sum;
        for (ProfileInfo pi : profileVals) {
            pi.val = 0;
        }
        Collections.sort(profileVals, PROFILE_COMP);
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
                profileCounterMap.get(clss).val += duration;
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

    public List<ProfileInfo> getProfileMap() {
        return profileVals;
    }

    public static class ProfileInfo {
        public Class<? extends BaseSystem> clss;
        public long val;
        public double percent;

        public ProfileInfo(Class<? extends BaseSystem> clss) {
            this.clss = clss;
        }
    }
}
