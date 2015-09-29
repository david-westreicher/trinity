package com.westreicher.birdsim.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by juanolon on 21/09/15.
 */
public class SaveMouse extends InputHelper {
    private final int index;
    private final Viewport v;

    private final boolean isFix;

    public SaveMouse(int index, Viewport v, int x, int y) {
        this.index = index;
        this.v = v;
        this.isFix = (x != 0 && y != 0);

        startX = x;
        startY = y;
    }

    //TODO choose closest finger
    public boolean update() {
        int width = v.getScreenWidth();
        int height = v.getScreenHeight();
        if (Gdx.input.isTouched(index)) {
            if (!super.isDown()) {
                if (!isFix) {
                    int currentx = Gdx.input.getX(index);
                    int currenty = Gdx.input.getY(index);
                    if (Math.abs(currentx - width / 2) < width / 2 - 100
                            && Math.abs(currenty - height / 2) < height / 2 - 100) {
                        startX = currentx;
                        startY = currenty;
                    }
                }
            }
            super.setDown(true);
        } else {
            super.setDown(false);
        }
        return super.isDown();
    }

    public int relx() {
        return (Gdx.input.getX(index) - getStartX(v.getScreenWidth()));
    }

    public int rely() {
        return (Gdx.input.getY(index) - getStartY(v.getScreenHeight()));
    }

}
