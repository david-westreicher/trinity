package com.westreicher.birdsim.artemis.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.westreicher.birdsim.ChunkManager;
import com.westreicher.birdsim.Config;
import com.westreicher.birdsim.artemis.Artemis;
import com.westreicher.birdsim.artemis.components.ParticleComponent;

import java.util.Random;


/**
 * Created by david on 10/5/15.
 */
@Wire
public class AnimateParticles extends IteratingSystem {
    private static final Color TMP_COLOR = new Color();
    private static final Random TMP_RAND = new Random();
    private static final float DAMPING = 0.8f;
    protected ComponentMapper<ParticleComponent> mParticleComponent;
    protected TagManager tm;
    private ChunkManager cm;

    public AnimateParticles() {
        super(Aspect.all(ParticleComponent.class));
    }

    @Override
    protected void begin() {
        cm = tm.getEntity(Artemis.CHUNKMANAGER_TAG).getComponent(ChunkManager.class);
    }

    @Override
    protected void process(int entityId) {
        ParticleComponent particles = mParticleComponent.get(entityId);
        for (int i = 0; i < ParticleComponent.PARTICLE_NUM; i++) {
            int ix = i * 3 + 0;
            int iy = i * 3 + 1;
            int iz = i * 3 + 2;
            if (particles.lives[i]-- <= 0) {
                particles.lives[i] = 0;
                continue;
            }
            particles.pos[ix] += particles.speed[ix];
            particles.pos[iy] += particles.speed[iy];
            particles.pos[iz] += particles.speed[iz];

            ChunkManager.TileResult tr = cm.setTileResult(particles.pos[ix], particles.pos[iy]);
            if (tr == null)
                continue;
            float terrainHeight = Math.max(0, tr.c.getVal(tr.innerx, tr.innery)) * Config.TERRAIN_HEIGHT;

            if (terrainHeight + 1 > particles.pos[iz]) {
                particles.speed[ix] *= DAMPING;
                particles.speed[iy] *= DAMPING;
                particles.speed[iz] *= -DAMPING;
                particles.pos[iz] = terrainHeight + 0.5f;
            } else {
                particles.speed[iz] -= 0.1f;
            }
        }
    }

    public void spawnParticle(float spawnx, float spawny) {
        Entity partSys = world.getSystem(TagManager.class).getEntity(Artemis.PARTICLE_SYS_TAG);
        ParticleComponent particles = partSys.getComponent(ParticleComponent.class);
        int pointer = particles.freeParticlePointer;
        int ix = pointer * 3 + 0;
        int iy = pointer * 3 + 1;
        int iz = pointer * 3 + 2;
        particles.pos[ix] = spawnx;
        particles.pos[iy] = spawny;
        particles.speed[ix] = (float) (TMP_RAND.nextGaussian() / 5);
        particles.speed[iy] = (float) (TMP_RAND.nextGaussian() / 5);
        particles.speed[iz] = TMP_RAND.nextFloat() * 2;
        ChunkManager.TileResult tr = cm.setTileResult(spawnx, spawny);
        if (tr == null)
            return;
        float terrainHeight = Math.max(0, tr.c.getVal(tr.innerx, tr.innery)) * Config.TERRAIN_HEIGHT;
        particles.pos[iz] = terrainHeight + 2;
        TMP_COLOR.set(tr.c.colors[tr.innerx][tr.innery]);
        particles.col[ix] = TMP_COLOR.r;
        particles.col[iy] = TMP_COLOR.g;
        particles.col[iz] = TMP_COLOR.b;
        particles.lives[pointer] = 1000;
        particles.freeParticlePointer = (pointer + 1) % ParticleComponent.PARTICLE_NUM;
    }

    public void translateAll(float transx, float transy) {
        Entity partSys = world.getSystem(TagManager.class).getEntity(Artemis.PARTICLE_SYS_TAG);
        ParticleComponent particles = partSys.getComponent(ParticleComponent.class);
        for (int i = 0; i < ParticleComponent.PARTICLE_NUM; i++) {
            int ix = i * 3 + 0;
            int iy = i * 3 + 1;
            if (particles.lives[i]-- <= 0)
                continue;
            particles.pos[ix] -= transx;
            particles.pos[iy] -= transy;
        }
    }
}
