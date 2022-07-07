package net.dries007.holoInventory.compat;

import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagList;

/**
 * @author Dries007
 */
public abstract class InventoryDecoder {
    public final Class<?> targetClass;

    public InventoryDecoder(Class targetClass) {
        this.targetClass = targetClass;
    }

    public abstract NBTTagList toNBT(IInventory te);
}
