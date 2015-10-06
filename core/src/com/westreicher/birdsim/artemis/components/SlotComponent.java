package com.westreicher.birdsim.artemis.components;

import com.artemis.PooledComponent;
import com.westreicher.birdsim.SlotSystem;

/**
 * Created by david on 10/3/15.
 */
public class SlotComponent extends PooledComponent {
    public SlotSystem.Slot<SlotSystem.Specialty> special = new SlotSystem.Slot<SlotSystem.Specialty>();
    public SlotSystem.Slot<SlotSystem.GunSpecialty> gunSpecial = new SlotSystem.Slot<SlotSystem.GunSpecialty>();
    public SlotSystem.Slot<SlotSystem.GunType> gunType = new SlotSystem.Slot<SlotSystem.GunType>();

    @Override
    protected void reset() {
        special.reset();
        gunSpecial.reset();
        gunType.reset();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(special.toString());
        sb.append("\n");
        sb.append(gunSpecial.toString());
        sb.append("\n");
        sb.append(gunType.toString());
        return sb.toString();
    }

    public void set(SlotComponent sc) {
        gunType.set(sc.gunType);
        gunSpecial.set(sc.gunSpecial);
        special.set(sc.special);
    }
}
