package com.westreicher.birdsim.artemis.managers;

import com.artemis.Manager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

/**
 * Created by david on 9/29/15.
 */
public class TextureManager extends Manager {

    public enum Textures {
        THUMB("thumb.png");
        private final String file;
        private Texture texture;

        Textures(String file) {
            this.file = file;
        }

        void reload() {
            this.texture = new Texture(Gdx.files.internal(file));
        }
    }

    @Override
    protected void initialize() {
        for (Textures t : Textures.values())
            t.reload();
    }

    @Override
    protected void dispose() {
        for (Textures t : Textures.values())
            t.texture.dispose();
    }


    public Texture get(Textures t) {
        return t.texture;
    }

}
