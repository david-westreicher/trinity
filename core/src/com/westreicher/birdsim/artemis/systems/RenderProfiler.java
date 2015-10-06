package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.managers.TagManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.compression.lzma.Base;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.FixedTimestepStrategy;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.components.Position2;
import com.westreicher.birdsim.artemis.components.Speed2;
import com.westreicher.birdsim.artemis.components.StaticText;
import com.westreicher.birdsim.artemis.factories.TextEntity;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by david on 10/4/15.
 */
public class RenderProfiler extends BaseSystem {

    private Set<Class<? extends BaseSystem>> systemlist;
    private final Map<Class<? extends BaseSystem>, String> systemliststrings = new HashMap<Class<? extends BaseSystem>, String>();
    private SpriteBatch spritebatch;
    private BitmapFont font;
    private ShapeRenderer shapes;

    @Override
    protected void initialize() {
        spritebatch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        for (BaseSystem bs : world.getSystems()) {
            Class<? extends BaseSystem> clss = bs.getClass();
            systemliststrings.put(clss, clss.getSimpleName());
        }
        systemlist = systemliststrings.keySet();
        shapes = new ShapeRenderer();
    }

    @Override
    protected void processSystem() {
        Viewport viewport = world.getSystem(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG).getComponent(CameraComponent.class).viewport;
        float height = viewport.getScreenHeight();
        List<FixedTimestepStrategy.ProfileInfo> profiles = ((FixedTimestepStrategy) world.getInvocationStrategy()).getProfileMap();

        int i = 0;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.3f, 0.3f, 0.3f, 1);
        for (FixedTimestepStrategy.ProfileInfo pi : profiles) {
            if (pi.percent > 0.009)
                shapes.rect(150, height - (i++ + 1) * 20, (float) (pi.percent * 100), 20);
        }
        shapes.rectLine(150, height, 150, height - i * 20, 1);
        shapes.rectLine(250, height, 250, height - i * 20, 1);
        shapes.end();

        spritebatch.begin();
        i = 0;
        for (FixedTimestepStrategy.ProfileInfo pi : profiles) {
            font.draw(spritebatch, systemliststrings.get(pi.clss), 0, height - (i++) * 20);
        }
        spritebatch.end();
    }

    @Override
    protected void dispose() {
        font.dispose();
        spritebatch.dispose();
        shapes.dispose();
    }
}
