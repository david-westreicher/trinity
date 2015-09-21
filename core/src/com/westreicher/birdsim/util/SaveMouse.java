package com.westreicher.birdsim.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by juanolon on 21/09/15.
 */
public class SaveMouse extends InputHelper {
    private final int index;
    private final Viewport v;

    public SaveMouse(int index, Viewport v) {
        this.index = index;
        this.v = v;
    }

    public boolean update() {
        int width = v.getScreenWidth();
        int height = v.getScreenHeight();
        if (Gdx.input.isTouched(index)) {
            if (!super.isDown()) {
                int currentx = Gdx.input.getX(index);
                int currenty = Gdx.input.getY(index);
                if (Math.abs(currentx - width / 2) < width / 2 - 100
                        && Math.abs(currenty - height / 2) < height / 2 - 100) {
                    startX = currentx;
                    startY = currenty;
                }
            }
            super.setDown(true);
        } else {
            super.setDown(false);
        }
        return super.isDown();
    }

    public int relx() {
        return (Gdx.input.getX(index) - startX);
    }

    public int rely() {
        return (Gdx.input.getY(index) - startY);
    }

}
