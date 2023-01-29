package net.dries007.holoInventory.compat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;
import cpw.mods.fml.common.Loader;

/**
 * @author Dries007
 */
public class DecoderRegistry {

    private static final Map<Class<? extends IInventory>, InventoryDecoder> CACHE_MAP = new HashMap<>();
    private static final List<InventoryDecoder> REGISTERED_INVENTORY_DECODERS = new ArrayList<>();
    private static final InventoryDecoder VANILLA = new InventoryDecoder(IInventory.class) {

        @Override
        public NBTTagList toNBT(IInventory te) {
            NBTTagList list = new NBTTagList();
            for (int i = 0; i < te.getSizeInventory(); i++) {
                ItemStack stack = te.getStackInSlot(i);
                if (stack != null) {
                    NBTTagCompound tag = stack.writeToNBT(new NBTTagCompound());
                    tag.setInteger("Count", stack.stackSize);
                    list.appendTag(tag);
                }
            }
            return list;
        }
    };

    private DecoderRegistry() {}

    public static void init() {
        if (Loader.isModLoaded("StorageDrawers")) {
            REGISTERED_INVENTORY_DECODERS.add(new InventoryDecoder(IDrawerGroup.class) {

                @Override
                public NBTTagList toNBT(IInventory te) {
                    IDrawerGroup drawerGroup = ((IDrawerGroup) te);
                    NBTTagList list = new NBTTagList();
                    for (int i = 0; i < drawerGroup.getDrawerCount(); i++) {
                        ItemStack stack = drawerGroup.getDrawer(i).getStoredItemCopy();
                        if (stack != null) {
                            NBTTagCompound tag = stack.writeToNBT(new NBTTagCompound());
                            tag.setInteger("Count", stack.stackSize);
                            list.appendTag(tag);
                        }
                    }
                    return list;
                }
            });
        }
    }

    public static NBTTagList toNBT(IInventory te) {
        Class<? extends IInventory> teClass = te.getClass();
        InventoryDecoder decoder = CACHE_MAP.get(teClass);

        if (decoder == null) {
            for (InventoryDecoder possibleDecoder : REGISTERED_INVENTORY_DECODERS) {
                if (possibleDecoder.targetClass.isAssignableFrom(teClass)) {
                    decoder = possibleDecoder;
                    break;
                }
            }

            if (decoder == null) decoder = VANILLA;

            CACHE_MAP.put(teClass, decoder);
        }
        return decoder.toNBT(te);
    }
}
