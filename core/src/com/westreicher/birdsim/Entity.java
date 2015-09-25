package com.westreicher.birdsim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.westreicher.birdsim.util.InputHelper;
import com.westreicher.birdsim.util.MaxArray;
import com.westreicher.birdsim.util.SoundPlayer;

/**
 * Created by david on 9/4/15.
 */
public class Entity {
    private static final float EDGE = Config.TILES_PER_CHUNK * Config.CHUNKNUMS / 2;
    private static final Vector3 TMP_VEC3 = new Vector3();
    private static final Vector3 TMP_VEC2 = new Vector3();
    private static final float ENEMY_VISIBILITY = 150;
    public static EntityManager manager;


    public int lives;
    public int id;


    private Vector3 speed = new Vector3();
    public Vector3 pos = new Vector3();
    public Vector3 oldpos = new Vector3();
    public float scale = 1f;
    public boolean visible;
    public ModelInstance modelInstance;
    public boolean dead = false;
    public EntityManager.Type type;
    public EntityManager.ColorAttr col;
    private float radiant;
    public Entity parent;
    public final MaxArray.MaxArrayEntity collisions = new MaxArray.MaxArrayEntity(100);
    public final SlotSystem.Slot<SlotSystem.Specialty> specialitySlot = new SlotSystem.Slot<SlotSystem.Specialty>();
    private final SlotSystem.Slot<SlotSystem.GunType> gunSlot = new SlotSystem.Slot<SlotSystem.GunType>();
    private final SlotSystem.Slot<SlotSystem.GunSpecialty> gunSpecialitySlot = new SlotSystem.Slot<SlotSystem.GunSpecialty>();


    public void init(EntityManager.Type type, float posx, float posy, EntityManager.ColorAttr col, float xspeed, float yspeed, Model m, Entity parent) {
        specialitySlot.reset();
        gunSpecialitySlot.reset();
        gunSlot.reset();
        this.dead = false;
        this.type = type;
        this.pos.set(posx, posy, 0);
        this.oldpos.set(pos);
        this.col = col;
        this.parent = parent;
        this.lives = 10;
        //if (ChunkManager.isStuck(pos, 1)) type = Type.ITEM;
        switch (type) {
            case ENEMY:
                this.speed.set((float) Math.random() - 0.5f, (float) Math.random() - 0.5f, 0);
                scale = 4f;
                gunSlot.type = SlotSystem.randomGun();
                break;
            case BULLET:
                gunSlot.type = parent.gunSlot.type;
                gunSpecialitySlot.set(parent.gunSpecialitySlot);
                this.speed.set(xspeed, yspeed, 0);
                scale = gunSlot.type.scale * gunSpecialitySlot.getMultiplier(SlotSystem.GunSpecialty.DAMAGE);
                MyGdxGame.single.playSound(SoundPlayer.Sounds.SHOT1, pos);
                radiant = -(float) Math.atan2(-speed.y, speed.x);
                break;
            case ITEM:
                this.speed.set(0, 0, 0);
                scale = 1f;
                break;
            case PLAYER:
                this.speed.set(0, 0, 0);
                scale = 6f;
                gunSlot.type = SlotSystem.GunType.MACHINEGUN;
                gunSlot.multiplier = 1;
                // specialitySlot.type = Specialty.SLOWMO;
                // specialitySlot.multiplier = 1000;
                break;
        }
        //TODO don't allocate new modelinstances
        this.modelInstance = new ModelInstance(m);
        modelInstance.materials.get(0).set(col.attr);
        updateLogic(0);
    }

    public void updateLogic(long tick) {
        oldpos.set(pos);
        ChunkManager chunkManager = MyGdxGame.single.chunkManager;
        int freq = gunSpecialitySlot.getMultiplier(SlotSystem.GunSpecialty.FREQUENCY);
        switch (type) {
            case ENEMY:
                if (!tryToMove(speed.x, speed.y, true)) {
                    speed.x = (float) (Math.random() - 0.5f);
                    speed.y = (float) (Math.random() - 0.5f);
                }
                radiant = -(float) Math.atan2(speed.y, speed.x);
                if (tick % ((gunSlot.type.frequency * 4) / freq) == 0) {
                    float shortestDist = ENEMY_VISIBILITY;
                    Entity closestplayer = null;
                    for (int i = 0; i < manager.aliveplayers.size(); i++) {
                        Entity player = manager.aliveplayers.arr[i];
                        float dist = player.pos.dst(pos);
                        if (player.specialitySlot.type == SlotSystem.Specialty.INVISIBLLE)
                            continue;
                        if (dist < shortestDist) {
                            shortestDist = dist;
                            closestplayer = player;
                        }
                    }
                    if (closestplayer != null) {
                        TMP_VEC3.set(closestplayer.pos).sub(pos);
                        TMP_VEC3.nor();
                        TMP_VEC3.scl(gunSpecialitySlot.getMultiplier(SlotSystem.GunSpecialty.SPEED));
                        manager.shoot(pos.x, pos.y, TMP_VEC3.x, TMP_VEC3.y, col, this);
                    }
                }
                break;
            case BULLET:
                pos.mulAdd(speed, gunSlot.type.speed);
                if (chunkManager.getVal(pos) > 0) {
                    dead = true;
                    MyGdxGame.single.playSound(SoundPlayer.Sounds.SHOT2, pos);
                    if (gunSlot.type.worlddamage > 0)
                        chunkManager.explode2(pos, gunSlot.type.worlddamage * gunSpecialitySlot.getMultiplier(SlotSystem.GunSpecialty.DAMAGE));
                }
                break;
            case PLAYER:
                InputHelper firstPointer = InputHelper.players.get(id).firstPointer;
                InputHelper secondPointer = InputHelper.players.get(id).secondPointer;
                if (firstPointer.update()) {
                    float rad = firstPointer.getRadiant();
                    this.radiant = rad;
                    float faster = specialitySlot.type == SlotSystem.Specialty.FASTER ? 1.5f : 1;
                    tryToMove((float) Math.cos(rad) * Config.MOVE_SPEED * faster, -(float) Math.sin(rad) * Config.MOVE_SPEED * faster, false);
                }
                //Gdx.app.log("game", "" +"");
                if (secondPointer.update()) {
                    if (tick % (gunSlot.type.frequency / freq) == 0) {
                        float radiant = secondPointer.getRadiant();
                        float tmpradiant = radiant;
                        for (int i = 0; i < gunSlot.multiplier; i++) {
                            float speed = gunSpecialitySlot.getMultiplier(SlotSystem.GunSpecialty.SPEED);
                            float xspeed = (float) Math.cos(tmpradiant) * speed;
                            float yspeed = (float) Math.sin(tmpradiant) * speed;
                            manager.shoot(pos.x, pos.y, xspeed, yspeed, EntityManager.ColorAttr.RED, this);
                            tmpradiant += Math.PI * 2 / gunSlot.multiplier;
                        }
                    }
                }
                if (specialitySlot.type != null) {
                    specialitySlot.multiplier--;
                    if (specialitySlot.multiplier <= 0) {
                        specialitySlot.type = null;
                    }
                }
                break;
        }
        if (Math.abs(pos.x) > EDGE || Math.abs(pos.y) > EDGE)
            dead = true;
    }

    public void updateZ(Vector3 cam) {
        ChunkManager chunkManager = MyGdxGame.single.chunkManager;
        float orig = chunkManager.getVal(pos) * Config.TERRAIN_HEIGHT;
        if (Config.POST_PROCESSING) {
            //TODO optimize Z projection
            float dstx = pos.x - cam.x;
            float dsty = pos.y - cam.y;
            float dstsq = dstx * dstx + dsty * dsty;
            float dstfrac = (dstsq / (140f * 140f));
            visible = dstfrac <= 1;
            pos.z = orig + (1.0f - dstfrac) * 140.0f + 5;
        } else {
            visible = true;
            pos.z = orig + 5;
        }
    }


    public void resolveCollisions() {
        switch (type) {
            case ENEMY:
                for (int i = 0; i < collisions.size(); i++) {
                    Entity other = collisions.arr[i];
                    switch (other.type) {
                        case ENEMY:
                            break;
                        case BULLET:
                            collideBulletEnemy(other, this);
                            break;
                    }
                }
                break;
            case BULLET:
                for (int i = 0; i < collisions.size(); i++) {
                    Entity other = collisions.arr[i];
                    switch (other.type) {
                        case PLAYER:
                        case ENEMY:
                            collideBulletEnemy(this, other);
                            break;
                        case BULLET:
                            break;
                    }
                }
                break;
            case ITEM:
                for (int i = 0; i < collisions.size(); i++) {
                    Entity other = collisions.arr[i];
                    switch (other.type) {
                        case PLAYER:
                            collideItemPlayer(this, other);
                            break;
                    }
                }
                break;
            case PLAYER:
                for (int i = 0; i < collisions.size(); i++) {
                    Entity other = collisions.arr[i];
                    switch (other.type) {
                        case ITEM:
                            collideItemPlayer(other, this);
                            break;
                        case BULLET:
                            collideBulletEnemy(other, this);
                            break;
                    }
                }
                break;
        }
    }

    private void collideItemPlayer(Entity item, Entity player) {
        item.dead = true;
        int poss = (int) (Math.random() * 5);
        if (poss == 4) {
            player.lives++;
            Gdx.app.log("item", "lives");
        } else if (poss == 3) {
            Gdx.app.log("item", "respawn");
            if (!manager.respawnOnePlayer(pos)) {
                //TODO hacky stuff
                collideItemPlayer(item, player);
                return;
            }
        } else if (poss == 2) {
            SlotSystem.Specialty randomSpecialty = SlotSystem.randomSpecialty();
            Gdx.app.log("item", randomSpecialty.toString());
            if (player.specialitySlot.type == randomSpecialty) {
                player.specialitySlot.multiplier += 1000;
            } else {
                player.specialitySlot.type = randomSpecialty;
                player.specialitySlot.multiplier = 1000;
            }
        } else if (poss == 1) {
            SlotSystem.GunSpecialty randomSpecialty = SlotSystem.randomGunSpecialty();
            Gdx.app.log("item", randomSpecialty.toString());
            if (player.gunSpecialitySlot.type == randomSpecialty) {
                player.gunSpecialitySlot.multiplier++;
            } else {
                player.gunSpecialitySlot.type = randomSpecialty;
                player.gunSpecialitySlot.multiplier = 1;
            }
        } else {
            SlotSystem.GunType randomgun = SlotSystem.randomGun();
            Gdx.app.log("item", randomgun.toString());
            if (player.gunSlot.type == randomgun) {
                player.gunSlot.multiplier++;
            } else {
                player.gunSlot.type = randomgun;
                player.gunSlot.multiplier = 1;
            }
        }
        Gdx.app.log("item", lives + "," + player.gunSlot + "," + player.gunSpecialitySlot + "," + player.specialitySlot);
    }


    private static void collideBulletEnemy(Entity bullet, Entity victim) {
        boolean bothplayers = bullet.parent.type == EntityManager.Type.PLAYER && victim.type == EntityManager.Type.PLAYER;
        if (bullet.parent != victim && !bothplayers) {
            float damagemul = bullet.gunSpecialitySlot.getMultiplier(SlotSystem.GunSpecialty.DAMAGE);
            int damage = (int) (bullet.gunSlot.type.damage * damagemul);
            victim.lives -= damage;
            if (victim.lives <= 0) {
                victim.dead = true;
            }
            bullet.dead = true;
        }
    }

    private boolean tryToMove(float x, float y, boolean simple) {
        ChunkManager chunkManager = MyGdxGame.single.chunkManager;
        pos.x += x;
        pos.y -= y;
        if (chunkManager.getVal(pos) <= 0)
            return true;
        pos.y += y;
        if (simple) {
            pos.x -= x;
            return false;
        }
        if (chunkManager.getVal(pos) <= 0)
            return true;
        pos.x -= x;
        pos.y -= y;
        if (chunkManager.getVal(pos) <= 0)
            return true;
        pos.y += y;
        return false;
    }

    public void setModelInstance(float interp) {
        modelInstance.transform.setToTranslation(interpolate(oldpos, pos, interp));
        modelInstance.transform.scl(scale);
        modelInstance.transform.rotateRad(Config.UPAXIS, radiant);
    }

    private Vector3 interpolate(Vector3 oldpos, Vector3 pos, float interp) {
        TMP_VEC3.set(oldpos);
        TMP_VEC2.set(pos).sub(oldpos).scl(interp);
        TMP_VEC3.add(TMP_VEC2);
        return TMP_VEC3;
    }

    public void translate(float x, float y) {
        this.pos.add(x, y, 0);
        this.oldpos.add(x, y, 0);
    }

}
