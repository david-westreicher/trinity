package com.westreicher.birdsim.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;

/**
 * Created by david on 9/29/15.
 */
public enum ColorAttr {
    RED(new Color(1, 0, 0, 1)), VIOLET(new Color(1, 0, 1, 1)), YELLOW(new Color(0.9f, 1, 0, 1)), TEAL(new Color(1, 0.2f, 0.2f, 1)), GOLD(new Color(1, 0.5f, 0.5f, 1)), BLUE(new Color(0.3f, 0.3f, 1, 1));
    public final ColorAttribute attr;
    private static final ColorAttr[] cols = ColorAttr.values();

    ColorAttr(Color col) {
        this.attr = new ColorAttribute(ColorAttribute.Diffuse, col);
    }

    public static ColorAttr random() {
        return cols[(int) (Math.random() * cols.length)];
    }
}
