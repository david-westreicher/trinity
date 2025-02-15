package com.westreicher.birdsim.artemis.systems;

import com.artemis.BaseSystem;
import com.artemis.Entity;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.CameraComponent;
import com.westreicher.birdsim.artemis.components.HealthComponent;
import com.westreicher.birdsim.artemis.managers.TextureManager;

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
        Viewport v = world.getSystem(TagManager.class).getEntity(Artemis.VIRTUAL_CAM_TAG).getComponent(CameraComponent.class).viewport;
        spritebatch.begin();
        if (!Config.IS_DESKTOP)
            drawThumbs(v.getScreenWidth(), v.getScreenHeight());
        drawSlotSelect(v.getScreenWidth(), v.getScreenHeight());
        drawLives();
        spritebatch.end();
    }

    /**
     * Draw Joysticks left and right
     * @param w Screen width
     * @param h Screen height
     */
    private void drawThumbs(int w, int h) {
        Texture thumbTex = world.getSystem(TextureManager.class).get(TextureManager.Textures.THUMB);
        int size = 50;
        float y = h * 0.3f;
        spritebatch.draw(thumbTex, w * 0.15f - size, y - size, size * 2, size * 2, 0, 0, 1, 1);
        spritebatch.draw(thumbTex, w * 0.85f - size, y - size, size * 2, size * 2, 0, 0, 1, 1);
    }

    /**
     * Draw Slot Selection - Weapons
     * @param w Screen width
     * @param h Screen height
     */
    private void drawSlotSelect(int w, int h){
        Texture thumbTex = world.getSystem(TextureManager.class).get(TextureManager.Textures.THUMB);
        int size = 50; // TODO: scale to display size
        float y = h * 0.85f;
        spritebatch.draw(thumbTex, w * 0.85f - size, y - size, size * 2, size * 2, 0, 0, 1, 1);
        spritebatch.draw(thumbTex, w * 0.75f - size, y - size, size * 2, size * 2, 0, 0, 1, 1);
        spritebatch.draw(thumbTex, w * 0.65f - size, y - size, size * 2, size * 2, 0, 0, 1, 1);
    }

    private void drawLives() {
        int size = 50;
        ImmutableBag<Entity> players = world.getSystem(GroupManager.class).getEntities(Artemis.PLAYER_GROUP);
        Texture thumbTex = world.getSystem(TextureManager.class).get(TextureManager.Textures.THUMB);
        for (int i = 0; i < players.size(); i++) {
            Entity player = players.get(i);
            for (int j = 0; j < player.getComponent(HealthComponent.class).health; j++) {
                spritebatch.draw(thumbTex, size * j, size * i, size * 2, size * 2);
            }
        }
    }
}
