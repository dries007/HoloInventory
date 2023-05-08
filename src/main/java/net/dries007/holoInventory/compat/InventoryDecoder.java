package net.dries007.holoInventory.compat;

import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagList;

/**
 * @author Dries007
 */
public abstract class InventoryDecoder {

    private final Class<?> targetClass;

    public InventoryDecoder(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public boolean matches(IInventory inv) {
        return targetClass.isAssignableFrom(inv.getClass());
    }

    public abstract NBTTagList toNBT(IInventory inv);
}
