package com.westreicher.birdsim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import com.westreicher.birdsim.util.InputHelper;
import com.westreicher.birdsim.util.MaxArray;
import com.westreicher.birdsim.util.SoundPlayer;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by david on 9/4/15.
 */
public class Entity {


    int lives;
    private int id;

    private enum Type {ENEMY, BULLET, ITEM, PLAYER}

    ;

    private static final Specialty[] specialties = Specialty.values();

    private enum Specialty {
        FASTER, SLOWMO, INVISIBLLE;
    }

    ;

    private static final GunSpecialty[] gunspecialties = GunSpecialty.values();

    private enum GunSpecialty {
        DAMAGE, SPEED, FREQUENCY;
    }

    ;

    public static class Slot<T> {
        private T type;
        private int multiplier = 1;

        public Slot(T type) {
            this.type = type;
        }

        public Slot() {
            type = null;
        }

        public int getMultiplier(T t) {
            if (type == t)
                return multiplier + 1;
            else
                return 1;
        }

        public void set(Slot<T> otherslot) {
            type = otherslot.type;
            multiplier = otherslot.multiplier;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (type == null)
                sb.append("empty");
            else
                sb.append(type.getClass().getSimpleName() + ":" + type.toString() + ": " + multiplier);
            return sb.toString();
        }
    }


    private static final GunType[] guntypes = GunType.values();

    private enum GunType {
        ROCKETGUN(5, 10, 1, 25, 2.5f, 100), MACHINEGUN(1, 0, 2, 5, 1, 0);

        private final int damage;
        private final float speed;
        private final float scale;
        private final int worlddamage;
        private final int frequency;
        private final float maxdistance;

        GunType(int damage, int worlddamage, float speed, int frequency, float scale, float maxdistance) {
            this.damage = damage;
            this.worlddamage = worlddamage;
            this.speed = speed;
            this.frequency = frequency;
            this.scale = scale;
            this.maxdistance = maxdistance;
        }
    }

    ;
    private Slot<Specialty> specialitySlot = new Slot<Specialty>();
    private Slot<GunType> gunSlot = new Slot<GunType>(GunType.MACHINEGUN);
    private Slot<GunSpecialty> gunSpecialitySlot = new Slot<GunSpecialty>();

    public enum ColorAttr {
        RED(new Color(1, 0, 0, 1)), VIOLET(new Color(1, 0, 1, 1)), YELLOW(new Color(0.9f, 1, 0, 1)), TEAL(new Color(1, 0.2f, 0.2f, 1)), GOLD(new Color(1, 0.5f, 0.5f, 1)), BLUE(new Color(0, 0, 1, 1));
        private final ColorAttribute attr;

        ColorAttr(Color col) {
            this.attr = new ColorAttribute(ColorAttribute.Diffuse, col);
        }
    }

    ;

    private static final float EDGE = Config.TILES_PER_CHUNK * Config.CHUNKNUMS / 2;
    private static final ColorAttr[] colors = ColorAttr.values();
    private static final Vector3 TMP_VEC3 = new Vector3();
    private static final Vector3 TMP_VEC2 = new Vector3();
    private static final float ENEMY_VISIBILITY = 150;
    private static Model playerModel;
    private static Model itemModel;
    private static Model bulletModel;
    private Vector3 speed = new Vector3();
    public Vector3 pos = new Vector3();
    public Vector3 oldpos = new Vector3();
    private float scale = 1f;
    private boolean visible;
    private ModelInstance modelInstance;
    private boolean dead = false;
    private Type type;
    private ColorAttr col;
    private float radiant;
    private static ArrayList<Entity> aliveents = new ArrayList<Entity>();
    private Entity parent;
    private float size = 1;
    private MaxArray.MaxArrayEntity collisions = new MaxArray.MaxArrayEntity(100);
    public static MaxArray.MaxArrayEntity tmpplayersarr = new MaxArray.MaxArrayEntity(4);
    public static MaxArray.MaxArrayEntity aliveplayers = new MaxArray.MaxArrayEntity(4);
    public static MaxArray.MaxArrayEntity players = new MaxArray.MaxArrayEntity(4);
    private static Pool<Entity> ents = new Pool<Entity>() {
        @Override
        protected Entity newObject() {
            return new Entity();
        }
    };

    public static void spawn(float x, float y, Random rand) {
        if (!Config.SPAWN_STUFF || rand.nextDouble() > 0.3)
            return;
        Entity e = ents.obtain();
        e.init(rand.nextDouble() > 0.5 ? Type.ITEM : Type.ENEMY, x, y, ColorAttr.YELLOW, 0, 0, null);
    }

    public static Entity spawnPlayer(int id) {
        Entity e = ents.obtain();
        e.init(Type.PLAYER, 0, 0, colors[id], 0, 0, null);
        e.id = id;
        aliveplayers.add(e);
        players.add(e);
        return e;
    }

    public static void shoot(float x, float y, float xspeed, float yspeed, ColorAttr col, Entity parent) {
        Entity e = Entity.ents.obtain();
        e.init(Entity.Type.BULLET, x, y, col, xspeed, yspeed, parent);
    }

    public static void init() {
        playerModel = new ObjLoader().loadModel(Gdx.files.internal("player.obj"));
        itemModel = new ModelBuilder().createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(1, 1, 1, 0)), VertexAttributes.Usage.Position);
        bulletModel = new ModelBuilder().createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(1, 1, 1, 0)), VertexAttributes.Usage.Position);
    }

    public static void dispose() {
        aliveents.clear();
        ents.clear();
        playerModel.dispose();
        itemModel.dispose();
        bulletModel.dispose();
        aliveplayers.reset();
        tmpplayersarr.reset();
    }

    public void init(Type type, float posx, float posy, ColorAttr col, float xspeed, float yspeed, Entity parent) {
        specialitySlot.type = null;
        gunSlot.type = null;
        gunSpecialitySlot.type = null;
        this.dead = false;
        this.type = type;
        this.pos.set(posx, posy, 0);
        this.oldpos.set(pos);
        this.col = col;
        this.parent = parent;
        this.lives = 10;
        Model m = null;
        //if (ChunkManager.isStuck(pos, 1)) type = Type.ITEM;
        switch (type) {
            case ENEMY:
                this.speed.set((float) Math.random() - 0.5f, (float) Math.random() - 0.5f, 0);
                m = playerModel;
                size = 8;
                scale = 1f;
                gunSlot.type = guntypes[(int) (Math.random() * guntypes.length)];
                break;
            case BULLET:
                gunSlot.type = parent.gunSlot.type;
                gunSpecialitySlot.set(parent.gunSpecialitySlot);
                this.speed.set(xspeed, yspeed, 0);
                scale = gunSlot.type.scale * gunSpecialitySlot.getMultiplier(GunSpecialty.DAMAGE);
                size = Math.max(1, (int) scale);
                m = bulletModel;
                MyGdxGame.single.playSound(SoundPlayer.Sounds.SHOT1, pos);
                //radiant = -(float) Math.atan2(speed.y, speed.x);
                break;
            case ITEM:
                this.speed.set(0, 0, 0);
                scale = 1f;
                size = 3;
                m = itemModel;
                break;
            case PLAYER:
                this.speed.set(0, 0, 0);
                scale = 1f;
                size = 8;
                m = playerModel;
                gunSlot.type = GunType.MACHINEGUN;
                gunSlot.multiplier = 1;
                // specialitySlot.type = Specialty.SLOWMO;
                // specialitySlot.multiplier = 1000;
                break;
        }
        //TODO don't allocate new modelinstances
        this.modelInstance = new ModelInstance(m);
        modelInstance.materials.get(0).set(col.attr);
        aliveents.add(this);
        updateLogic(0);
    }

    private void updateLogic(long tick) {
        oldpos.set(pos);
        ChunkManager chunkManager = MyGdxGame.single.chunkManager;
        int freq = gunSpecialitySlot.getMultiplier(GunSpecialty.FREQUENCY);
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
                    for (int i = 0; i < aliveplayers.size(); i++) {
                        Entity player = aliveplayers.arr[i];
                        float dist = player.pos.dst(pos);
                        if (player.specialitySlot.type == Specialty.INVISIBLLE)
                            continue;
                        if (dist < shortestDist) {
                            shortestDist = dist;
                            closestplayer = player;
                        }
                    }
                    if (closestplayer != null) {
                        TMP_VEC3.set(closestplayer.pos).sub(pos);
                        TMP_VEC3.nor();
                        TMP_VEC3.scl(gunSpecialitySlot.getMultiplier(GunSpecialty.SPEED));
                        shoot(pos.x, pos.y, TMP_VEC3.x, TMP_VEC3.y, col, this);
                    }
                }
                break;
            case BULLET:
                pos.mulAdd(speed, gunSlot.type.speed);
                if (chunkManager.getVal(pos) > 0) {
                    dead = true;
                    MyGdxGame.single.playSound(SoundPlayer.Sounds.SHOT2, pos);
                    if (gunSlot.type.worlddamage > 0)
                        chunkManager.explode2(pos, gunSlot.type.worlddamage * gunSpecialitySlot.getMultiplier(GunSpecialty.DAMAGE));
                }
                break;
            case PLAYER:
                InputHelper firstPointer = InputHelper.players.get(id).firstPointer;
                InputHelper secondPointer = InputHelper.players.get(id).secondPointer;
                if (firstPointer.update()) {
                    float rad = firstPointer.getRadiant();
                    this.radiant = rad;
                    float faster = specialitySlot.type == Specialty.FASTER ? 1.5f : 1;
                    tryToMove((float) Math.cos(rad) * Config.MOVE_SPEED * faster, -(float) Math.sin(rad) * Config.MOVE_SPEED * faster, false);
                }
                //Gdx.app.log("game", "" +"");
                if (secondPointer.update()) {
                    if (tick % (gunSlot.type.frequency / freq) == 0) {
                        float radiant = secondPointer.getRadiant();
                        float tmpradiant = radiant;
                        for (int i = 0; i < gunSlot.multiplier; i++) {
                            float speed = gunSpecialitySlot.getMultiplier(GunSpecialty.SPEED);
                            float xspeed = (float) Math.cos(tmpradiant) * speed;
                            float yspeed = (float) Math.sin(tmpradiant) * speed;
                            Entity.shoot(pos.x, pos.y, xspeed, yspeed, Entity.ColorAttr.RED, this);
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

    private void updateZ(Vector3 cam) {
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


    private void resolveCollisions() {
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
            if (!respawnOnePlayer(pos)) {
                //TODO hacky stuff
                collideItemPlayer(item, player);
                return;
            }
        } else if (poss == 2) {
            Specialty randomSpecialty = specialties[(int) (Math.random() * specialties.length)];
            Gdx.app.log("item", randomSpecialty.toString());
            if (player.specialitySlot.type == randomSpecialty) {
                player.specialitySlot.multiplier += 1000;
            } else {
                player.specialitySlot.type = randomSpecialty;
                player.specialitySlot.multiplier = 1000;
            }
        } else if (poss == 1) {
            GunSpecialty randomSpecialty = gunspecialties[(int) (Math.random() * gunspecialties.length)];
            Gdx.app.log("item", randomSpecialty.toString());
            if (player.gunSpecialitySlot.type == randomSpecialty) {
                player.gunSpecialitySlot.multiplier++;
            } else {
                player.gunSpecialitySlot.type = randomSpecialty;
                player.gunSpecialitySlot.multiplier = 1;
            }
        } else {
            GunType randomgun = guntypes[(int) (Math.random() * guntypes.length)];
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

    private static boolean respawnOnePlayer(Vector3 pos) {
        tmpplayersarr.reset();
        for (int i = 0; i < players.size(); i++) {
            if (players.arr[i].dead) {
                tmpplayersarr.add(players.arr[i]);
                break;
            }
        }
        if (tmpplayersarr.size() > 0) {
            tmpplayersarr.arr[(int) (Math.random() * tmpplayersarr.size())].respawn(pos);
            return true;
        }
        return false;
    }

    private void respawn(Vector3 pos) {
        aliveplayers.add(this);
        init(Type.PLAYER, pos.x, pos.y, colors[(int) (Math.random() * colors.length)], 0, 0, null);
    }

    private static void collideBulletEnemy(Entity bullet, Entity victim) {
        boolean bothplayers = bullet.parent.type == Type.PLAYER && victim.type == Type.PLAYER;
        if (bullet.parent != victim && !bothplayers) {
            float damagemul = bullet.gunSpecialitySlot.getMultiplier(GunSpecialty.DAMAGE);
            int damage = (int) (bullet.gunSlot.type.damage * damagemul);
            victim.lives -= damage;
            if (victim.lives <= 0) {
                victim.dead = true;
                if (victim.type == Type.PLAYER) aliveplayers.remove(victim);
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

    public static void render(ModelBatch mb, float interp) {
        //TODO Use custom rendering instead of modelinstances?
        for (int i = 0; i < aliveents.size(); i++) {
            Entity ent = aliveents.get(i);
            ent.setModelInstance(interp);
            if (ent.visible)
                mb.render(ent.modelInstance);
        }
    }

    private void setModelInstance(float interp) {
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

    public static void updateall(long tick, Vector3 cam) {

        //DEBUG RESPAWN
        if (Gdx.input.isKeyPressed(Input.Keys.R) || InputHelper.players.get(0).thirdPointer.update()) {
            respawnOnePlayer(cam);
        }

        MaxArray.MaxArrayEntity slowmoplayers = tmpplayersarr;
        slowmoplayers.reset();
        for (int i = 0; i < aliveplayers.size(); i++) {
            Entity player = aliveplayers.arr[i];
            if (player.specialitySlot.type == Specialty.SLOWMO)
                slowmoplayers.add(player);
        }
        for (int i = 0; i < aliveents.size(); i++) {
            Entity ent = aliveents.get(i);
            boolean belongsToSlowMoPlayer = slowmoplayers.contains(ent) || (ent.type == Type.BULLET && slowmoplayers.contains(ent.parent));
            if (slowmoplayers.size() > 0 && !belongsToSlowMoPlayer) {
                if (tick % 2 == 0)
                    ent.updateLogic(tick / 2);
            } else ent.updateLogic(tick);

            ent.updateZ(cam);
            ent.collisions.reset();
        }
        for (int i = 0; i < aliveents.size() - 1; i++) {
            Entity one = aliveents.get(i);
            for (int j = i + 1; j < aliveents.size(); j++) {
                Entity two = aliveents.get(j);
                double xdist = one.pos.x - two.pos.x;
                double ydist = one.pos.y - two.pos.y;
                double dist = Math.sqrt(xdist * xdist + ydist * ydist);
                if (dist < (one.size + two.size) / 2.0)
                    one.collisions.add(two);
            }
        }
        for (int i = 0; i < aliveents.size(); i++) {
            aliveents.get(i).resolveCollisions();
        }
        for (int i = 0; i < aliveents.size(); i++) {
            Entity e = aliveents.get(i);
            if (e.dead) {
                aliveents.remove(i);
                if (e.type != Type.PLAYER)
                    ents.free(e);
            }
        }
    }


    public static void translateAll(float x, float y) {
        for (int i = 0; i < aliveents.size(); i++) {
            aliveents.get(i).translate(x, y);
        }
    }

    private void translate(float x, float y) {
        this.pos.add(x, y, 0);
        this.oldpos.add(x, y, 0);
    }

}
