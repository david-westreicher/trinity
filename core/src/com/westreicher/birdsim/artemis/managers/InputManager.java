package com.westreicher.birdsim.artemis.managers;

import com.artemis.Manager;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.artemis.factories.UberFactory;
import com.westreicher.birdsim.input.AbstractInput;
import com.westreicher.birdsim.input.Gamepad;
import com.westreicher.birdsim.input.Keyboard;
import com.westreicher.birdsim.input.TouchInput;

import java.util.ArrayList;

/**
 * Created by david on 9/30/15.
 */
public class InputManager extends Manager {
    public ArrayList<AbstractInput> players;

    @Override
    protected void initialize() {
        players = new ArrayList<AbstractInput>();
        if (Config.IS_DESKTOP) {
            for (Controller ctrl : Controllers.getControllers())
                players.add(new Gamepad(ctrl));
            if (players.size() == 0)
                players.add(new Keyboard());
        } else
            players.add(new TouchInput());
        int id = 0;
        for (AbstractInput pi : players)
            UberFactory.createPlayer(world, id++);
    }

    public void resize() {
        for (AbstractInput pi : players)
            pi.resize(world);
    }

    @Override
    protected void dispose() {
        players.clear();
    }
}
