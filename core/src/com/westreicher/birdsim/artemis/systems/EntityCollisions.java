package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.annotations.Wire;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.westreicher.birdsim.SlotSystem;
import com.westreicher.birdsim.artemis.components.*;

/**
 * Created by david on 10/1/15.
 */
@Wire
public class EntityCollisions extends BaseEntitySystem {

    protected ComponentMapper<MapCoordinateComponent> mMapCoordinate;
    protected ComponentMapper<CollidableComponent> mCollidable;
    protected ComponentMapper<EntityTypeComponent> mEntityType;
    protected ComponentMapper<HealthComponent> mHealth;
    protected ComponentMapper<SlotComponent> mSlotComponent;
    protected ComponentMapper<AnimationComponent> mAnimationComponent;
    protected ComponentMapper<ModelComponent> mModelComponent;

    public EntityCollisions() {
        super(Aspect.all(MapCoordinateComponent.class, CollidableComponent.class, EntityTypeComponent.class));
    }

    @Override
    protected void setWorld(World world) {
        super.setWorld(world);
    }

    @Override
    protected void processSystem() {
        IntBag ents = getSubscription().getEntities();
        int numents = ents.size();
        int[] entities = ents.getData();
        for (int i = 0; i < numents - 1; i++) {
            int one = entities[i];
            MapCoordinateComponent coord1 = mMapCoordinate.get(one);
            CollidableComponent coll1 = mCollidable.get(one);
            for (int j = i + 1; j < numents; j++) {
                int two = entities[j];
                MapCoordinateComponent coord2 = mMapCoordinate.get(two);
                CollidableComponent coll2 = mCollidable.get(two);
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
        EntityTypeComponent.Types oneType = mEntityType.get(one).type;
        EntityTypeComponent.Types twoType = mEntityType.get(two).type;
        if (oneType.ordinal() > twoType.ordinal()) {
            EntityTypeComponent.Types tmp = oneType;
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
                        playerItem(one, two);
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

    private void playerItem(int player, int item) {
        mHealth.get(item).health = 0;
//        SlotComponent playerSlot = mSlotComponent.get(player);

        int poss = (int) (Math.random() * 5);
        if (poss == 4) {
            mHealth.get(player).health++;
            Gdx.app.log("item", "lives");
        } else if (poss == 3) {
            Gdx.app.log("item", "respawn");
            //if (!manager.respawnOnePlayer(pos)) {
            //TODO hacky stuff + implement
            playerItem(player, item);
            return;
            //}
        } /*else if (poss == 2) {
            SlotSystem.Specialty randomSpecialty = SlotSystem.randomSpecialty();
            Gdx.app.log("item", randomSpecialty.toString());
            if (playerSlot.special.type == randomSpecialty) {
                playerSlot.special.multiplier += 1000;
            } else {
                playerSlot.special.type = randomSpecialty;
                playerSlot.special.multiplier = 1000;
            }
        } else if (poss == 1) {
            SlotSystem.GunSpecialty randomSpecialty = SlotSystem.randomGunSpecialty();
            Gdx.app.log("item", randomSpecialty.toString());
            if (playerSlot.gunSpecial.type == randomSpecialty) {
                playerSlot.gunSpecial.multiplier++;
            } else {
                playerSlot.gunSpecial.type = randomSpecialty;
                playerSlot.gunSpecial.multiplier = 1;
            }
        } else {
            SlotSystem.GunType randomgun = SlotSystem.randomGun();
            Gdx.app.log("item", randomgun.toString());
            if (playerSlot.gunType.type == randomgun) {
                playerSlot.gunType.multiplier++;
            } else {
                playerSlot.gunType.type = randomgun;
                playerSlot.gunType.multiplier = 1;
            }
        }
        Gdx.app.log("item", playerSlot.toString());
        */
    }

    private void bulletEnemy(int bullet, int enemy) {
        SlotComponent slot = mSlotComponent.get(bullet);
//        int damage = slot.gunType.type.damage * slot.gunSpecial.getMultiplier(SlotSystem.GunSpecialty.DAMAGE);
        // TODO old from slot
        int damage = 1;
        mHealth.get(enemy).health -= damage;
        mHealth.get(bullet).health = 0;
        Gdx.app.log("damage", "" + damage);
        Gdx.input.vibrate(10 * damage);
        AnimationComponent anim = mAnimationComponent.create(enemy);
        anim.duration = 10;
        anim.type = AnimationComponent.Types.WHITE;
        anim.savedcol = mModelComponent.get(enemy).col;
    }
}
