package com.westreicher.birdsim.artemis.systems;

import com.artemis.BaseSystem;
import com.artemis.Entity;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.components.Health;
import com.westreicher.birdsim.artemis.managers.TextureManager;
import com.westreicher.birdsim.input.InputHelper;

/**
 * Created by david on 9/29/15.
 */
public class RenderGui extends BaseSystem {
    private SpriteBatch spritebatch;

    @Override
    protected void initialize() {
        this.spritebatch = new SpriteBatch();
    }

    @Override
    protected void processSystem() {
        Viewport v = world.getManager(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG).getComponent(CameraComponent.class).viewport;
        spritebatch.begin();
        if (!Config.IS_DESKTOP)
            drawThumbs(v.getScreenWidth(), v.getScreenHeight());
        drawLives();
        spritebatch.end();
    }

    private void drawThumbs(int w, int h) {
        InputHelper firstPointer = InputHelper.players.get(0).firstPointer;
        InputHelper secondPointer = InputHelper.players.get(0).secondPointer;
        InputHelper thirdPointer = InputHelper.players.get(0).thirdPointer;
        Texture thumbTex = world.getManager(TextureManager.class).get(TextureManager.Textures.THUMB);

        int size = 50;
        spritebatch.draw(thumbTex, firstPointer.getStartX(w) - size, h - firstPointer.getStartY(h) - size, size * 2, size * 2, 0, 0, 1, 1);
        spritebatch.draw(thumbTex, secondPointer.getStartX(w) - size, h - secondPointer.getStartY(h) - size, size * 2, size * 2, 0, 0, 1, 1);
        if (thirdPointer.isDown()) {
            spritebatch.draw(thumbTex, thirdPointer.getStartX(w) - size, h - thirdPointer.getStartY(h) - size, size * 2, size * 2, 0, 0, 1, 1);
        }
    }

    private void drawLives() {
        int size = 50;
        ImmutableBag<Entity> players = world.getManager(GroupManager.class).getEntities(Artemis.PLAYER_GROUP);
        Texture thumbTex = world.getManager(TextureManager.class).get(TextureManager.Textures.THUMB);
        for (int i = 0; i < players.size(); i++) {
            Entity player = players.get(i);
            for (int j = 0; j < player.getComponent(Health.class).health; j++) {
                spritebatch.draw(thumbTex, size * j, size * i, size * 2, size * 2);
            }
        }
    }
}
