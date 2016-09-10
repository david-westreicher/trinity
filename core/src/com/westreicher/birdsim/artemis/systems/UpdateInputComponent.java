package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.westreicher.birdsim.artemis.components.InputComponent;
import com.westreicher.birdsim.artemis.components.MapCoordinateComponent;
import com.westreicher.birdsim.artemis.components.Speed2Component;
import com.westreicher.birdsim.artemis.managers.InputManager;
import com.westreicher.birdsim.input.AbstractInput;

import java.util.ArrayList;

/**
 * Created by david on 9/29/15.
 */
@Wire
public class UpdateInputComponent extends IteratingSystem {
    private ComponentMapper<InputComponent> inputMapper;
    private ArrayList<AbstractInput> players;

    public UpdateInputComponent() {
        super(Aspect.all(InputComponent.class, MapCoordinateComponent.class, Speed2Component.class));
    }

    @Override
    protected void begin() {
        players = world.getSystem(InputManager.class).players;
    }

    @Override
    protected void process(int e) {
        InputComponent input = inputMapper.get(e);
        AbstractInput playerinput = players.get(input.id);
        playerinput.update();
        input.isMoving = playerinput.isMoving();
        input.isShooting = playerinput.isShooting();
        input.moveRadiant = playerinput.getMoveRadiant();
        input.shootRadiant = playerinput.getShootRadiant();
    }
}
