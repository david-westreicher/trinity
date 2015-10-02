package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.EntitySystem;
import com.artemis.World;
import com.artemis.annotations.Wire;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.westreicher.birdsim.artemis.components.Collidable;
import com.westreicher.birdsim.artemis.components.EntityType;
import com.westreicher.birdsim.artemis.components.Health;
import com.westreicher.birdsim.artemis.components.MapCoordinate;

/**
 * Created by david on 10/1/15.
 */
@Wire
public class EntityCollisions extends EntitySystem {

    protected ComponentMapper<MapCoordinate> mMapCoordinate;
    protected ComponentMapper<Collidable> mCollidable;
    protected ComponentMapper<EntityType> mEntityType;
    protected ComponentMapper<Health> mHealth;

    public EntityCollisions() {
        super(Aspect.all(MapCoordinate.class, Collidable.class, EntityType.class));
    }

    @Override
    protected void setWorld(World world) {
        super.setWorld(world);
    }

    @Override
    protected void processSystem() {
        IntBag ents = super.subscription.getEntities();
        int numents = ents.size();
        int[] entities = ents.getData();
        for (int i = 0; i < numents - 1; i++) {
            int one = entities[i];
            MapCoordinate coord1 = mMapCoordinate.get(one);
            Collidable coll1 = mCollidable.get(one);
            for (int j = i + 1; j < numents; j++) {
                int two = entities[j];
                MapCoordinate coord2 = mMapCoordinate.get(two);
                Collidable coll2 = mCollidable.get(two);
                double xdist = coord1.x - coord2.x;
                double ydist = coord1.y - coord2.y;
                double dist = Math.sqrt(xdist * xdist + ydist * ydist);
                if (dist < (coll1.scale + coll2.scale) / 2f) {
                    collide(one, two);
                    break;
                }
            }
        }
    }

    private void collide(int one, int two) {
        EntityType.Types oneType = mEntityType.get(one).type;
        EntityType.Types twoType = mEntityType.get(two).type;
        if (oneType.ordinal() > twoType.ordinal()) {
            EntityType.Types tmp = oneType;
            oneType = twoType;
            twoType = tmp;
            int tmpe = one;
            one = two;
            two = tmpe;
        }
        //onetype  =< twotype
        switch (oneType) {
            case PLAYER:
                switch (twoType) {
                    case PLAYER:
                        break;
                    case BULLET:
                        break;
                    case ITEM:
                        break;
                    case ENEMY:
                        break;
                }
                break;
            case BULLET:
                switch (twoType) {
                    case BULLET:
                        break;
                    case ITEM:
                        break;
                    case ENEMY:
                        bulletEnemy(one, two);
                        break;
                }
                break;
            case ITEM:
                switch (twoType) {
                    case ITEM:
                        break;
                    case ENEMY:
                        break;
                }
                break;
            case ENEMY:
                switch (twoType) {
                    case ENEMY:
                        break;
                }
                break;
        }
    }

    private void bulletEnemy(int bullet, int enemy) {
        mHealth.get(enemy).health--;
        world.delete(bullet);
        Gdx.input.vibrate(100);
    }
}
