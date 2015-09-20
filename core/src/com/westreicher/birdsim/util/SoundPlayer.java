package com.westreicher.birdsim.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector3;

import java.util.HashMap;

/**
 * Created by david on 9/19/15.
 */
public class SoundPlayer {
    private HashMap<Sounds, Sound> map = new HashMap<Sounds, Sound>();


    public enum Sounds {
        SHOT1("shot.wav"), SHOT2("shot2.wav"), SHOT3("shot3.wav");

        private final String file;

        Sounds(String filename) {
            this.file = filename;
        }
    }

    public SoundPlayer() {
        for (Sounds s : Sounds.values()) {
            map.put(s, Gdx.audio.newSound(Gdx.files.internal(s.file)));
        }
    }

    public void play(Sounds s, Vector3 pos) {
        float dst = pos.dst(0, 0, 0);
        //map.get(s).play(Math.max(0, Math.min(1, 1 - (dst / 1000))));
    }

    public void dispose() {
        for (Sounds s : Sounds.values()) {
            map.get(s).dispose();
        }
        map.clear();
    }
}
