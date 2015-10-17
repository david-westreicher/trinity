package com.westreicher.birdsim;

/**
 * Created by david on 9/24/15.
 */
public class SlotSystem {


    public enum Specialty {FASTER, SLOWMO, INVISIBLLE}

    public enum GunSpecialty {DAMAGE, SPEED, FREQUENCY}

    public enum GunType {
        ROCKETGUN(5, 10, 1, 50, 5, 100), MACHINEGUN(1, 0, 2, 3, 2, 0);

        public final int damage;
        public final float speed;
        public final float scale;
        public final int worlddamage;
        public final int frequency;
        public final float maxdistance;

        GunType(int damage, int worlddamage, float speed, int frequency, float scale, float maxdistance) {
            this.damage = damage;
            this.worlddamage = worlddamage;
            this.speed = speed;
            this.frequency = frequency;
            this.scale = scale;
            this.maxdistance = maxdistance;
        }
    }

    private static final Specialty[] specialties = Specialty.values();
    private static final GunSpecialty[] gunspecialties = GunSpecialty.values();
    private static final GunType[] guntypes = GunType.values();

    public static GunType randomGun() {
        return guntypes[(int) (Math.random() * guntypes.length)];
    }

    public static GunSpecialty randomGunSpecialty() {
        return gunspecialties[(int) (Math.random() * gunspecialties.length)];
    }

    public static Specialty randomSpecialty() {
        return specialties[(int) (Math.random() * specialties.length)];
    }

    /**
     * Created by david on 9/24/15.
     */
    public static class Slot<T> {
        public T type;
        public int multiplier;

        public Slot(T type) {
            this.type = type;
        }

        public Slot() {
            reset();
        }

        public void reset() {
            type = null;
            multiplier = 1;
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
}
