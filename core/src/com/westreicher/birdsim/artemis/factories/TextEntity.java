package com.westreicher.birdsim.artemis.factories;

import com.artemis.EntityFactory;
import com.artemis.annotations.Bind;
import com.westreicher.birdsim.artemis.components.Position2Component;
import com.westreicher.birdsim.artemis.components.Speed2Component;
import com.westreicher.birdsim.artemis.components.StaticTextComponent;

/**
 * Created by david on 9/25/15.
 */
@Bind({Position2Component.class, Speed2Component.class, StaticTextComponent.class})
public interface TextEntity extends EntityFactory<TextEntity> {
    TextEntity position2Component(float x, float y);

    TextEntity speed2Component(float x, float y);

    TextEntity staticTextComponent(String text);
}
