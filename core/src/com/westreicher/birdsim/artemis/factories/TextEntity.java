package com.westreicher.birdsim.artemis.factories;

import com.artemis.EntityFactory;
import com.artemis.annotations.Bind;
import com.westreicher.birdsim.artemis.components.Position2;
import com.westreicher.birdsim.artemis.components.Speed2;
import com.westreicher.birdsim.artemis.components.StaticText;

/**
 * Created by david on 9/25/15.
 */
@Bind({Position2.class, Speed2.class, StaticText.class})
public interface TextEntity extends EntityFactory<TextEntity> {
    TextEntity position2(float x, float y);

    TextEntity speed2(float x, float y);

    TextEntity staticText(String text);
}
