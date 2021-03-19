package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.GroupManager;
import com.artemis.utils.ImmutableBag;
import com.artemis.utils.IntBag;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.InputComponent;
import com.westreicher.birdsim.artemis.components.MapCoordinateComponent;
import com.westreicher.birdsim.artemis.components.ShipComponent;
import com.westreicher.birdsim.artemis.components.Speed2Component;

/**
 * Created by david on 9/11/16.
 */
@Wire
public class SwarmSystem extends BaseEntitySystem {
    ComponentMapper<MapCoordinateComponent> coordMapper;
    ComponentMapper<Speed2Component> speedMapper;
    ComponentMapper<ShipComponent> shipMapper;

    public SwarmSystem() {
        super(Aspect.all(MapCoordinateComponent.class, Speed2Component.class, ShipComponent.class));
    }

    @Override
    protected void processSystem() {
        IntBag ships = subscription.getEntities();
        int[] shipids = ships.getData();

        //resetSpeed(shipids, ships.size());
        seperate(shipids, ships.size());
        input(shipids, ships.size());
        move(shipids, ships.size());
        //followLeader(shipids, ships.size(), 0.04f);
    }


    private void resetSpeed(int[] shipids, int size) {
        for (int i = 0; i < size; i++) {
            Speed2Component speed = speedMapper.get(shipids[i]);
            speed.x = 0;
            speed.y = 0;
        }
    }

    private void seperate(int[] shipids, int size) {
        for (int i = 0; i < size - 1; i++) {
            int ship1 = shipids[i];
            MapCoordinateComponent pos1 = coordMapper.get(ship1);
            float size1 = shipMapper.get(ship1).size;

            for (int j = i + 1; j < size; j++) {
                int ship2 = shipids[j];
                MapCoordinateComponent pos2 = coordMapper.get(ship2);
                float size2 = shipMapper.get(ship1).size;

                float xdiff = pos1.x - pos2.x;
                float ydiff = pos1.y - pos2.y;
                float dist = (float) Math.sqrt(xdiff * xdiff + ydiff * ydiff);
                if (dist > (size1 + size2)) continue;

                //too close -> move apart
                float scaleDist = 1.0f - dist / (size1 + size2);
                pos1.x += xdiff * scaleDist * 0.5;
                pos1.y += ydiff * scaleDist * 0.5;
                pos2.x -= xdiff * scaleDist * 0.5;
                pos2.y -= ydiff * scaleDist * 0.5;
            }
        }
    }

    private void followLeader(int[] shipids, int size, float scale) {
        ImmutableBag<Entity> players = world.getSystem(GroupManager.class).getEntities(Artemis.PLAYER_GROUP);
        for (int p = 0; p < players.size(); p++) {
            Entity player = players.get(p);
            MapCoordinateComponent playerpos = player.getComponent(MapCoordinateComponent.class);
            for (int i = 0; i < size; i++) {
                int ship = shipids[i];
                if (shipMapper.get(ship).playerid != p) continue;
                MapCoordinateComponent pos = coordMapper.get(ship);
                Speed2Component speed = speedMapper.get(ship);
                speed.x += (playerpos.x - pos.x) * scale;
                speed.y += (playerpos.y - pos.y) * scale;
            }
        }
    }

    private void input(int[] shipids, int size) {
        ImmutableBag<Entity> players = world.getSystem(GroupManager.class).getEntities(Artemis.PLAYER_GROUP);
        for (int p = 0; p < players.size(); p++) {
            Entity player = players.get(p);
            InputComponent input = player.getComponent(InputComponent.class);
            for (int i = 0; i < size; i++) {
                int ship = shipids[i];
                ShipComponent shipc = shipMapper.get(ship);
                if (shipc.playerid != p) continue;
                if (input.isMoving) {
                    shipc.todir = input.moveRadiant;
                    shipc.speed = 1;
                } else {
                    shipc.speed *= 0.9;
                }
            }
        }
    }

    private void move(int[] shipids, int size) {
        for (int i = 0; i < size; i++) {
            ShipComponent shipc = shipMapper.get(shipids[i]);
            Speed2Component speed = speedMapper.get(shipids[i]);
            shipc.dir = interp_rot(shipc.todir, shipc.dir, 0.5f);
            speed.x = (float) (Math.cos(shipc.dir) * shipc.speed);
            speed.y = (float) (Math.sin(shipc.dir) * shipc.speed);
        }
    }

    private float interp_rot(float dir1, float dir2, float a) {
        dir1 = normalize_rot(dir1);
        dir2 = normalize_rot(dir2);
        float diff1 = dir2 - dir1;
        float diff2 = dir2 - dir1;
    }

    private float normalize_rot(float a) {
        while (a < 0)
            a += Math.PI * 2;
        while (a >= Math.PI * 2)
            a -= Math.PI * 2;
        return a;
    }
}
