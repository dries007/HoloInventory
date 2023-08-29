package net.dries007.holoInventory.compat;

import static net.dries007.holoInventory.util.NBTKeys.*;

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
import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;

/**
 * @author Dries007
 */
public class InventoryDecoderRegistry {

    private static final Map<Class<? extends IInventory>, InventoryDecoder> CACHE_MAP = new HashMap<>();
    private static final List<InventoryDecoder> REGISTERED_INVENTORY_DECODERS = new ArrayList<>();
    private static final InventoryDecoder DEFAULT = new InventoryDecoder(IInventory.class) {

        @Override
        public NBTTagList toNBT(IInventory inv) {
            NBTTagList list = new NBTTagList();
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack != null) {
                    NBTTagCompound tag = stack.writeToNBT(new NBTTagCompound());
                    tag.setInteger(NBT_KEY_COUNT, stack.stackSize);
                    list.appendTag(tag);
                }
            }
            return list;
        }
    };

    private InventoryDecoderRegistry() {}

    public static void init() {
        if (Loader.isModLoaded("StorageDrawers")) {
            REGISTERED_INVENTORY_DECODERS.add(new InventoryDecoder(IDrawerGroup.class) {

                @Override
                public NBTTagList toNBT(IInventory inv) {
                    IDrawerGroup drawerGroup = ((IDrawerGroup) inv);
                    NBTTagList list = new NBTTagList();
                    for (int i = 0; i < drawerGroup.getDrawerCount(); i++) {
                        ItemStack stack = drawerGroup.getDrawer(i).getStoredItemCopy();
                        if (stack != null) {
                            NBTTagCompound tag = stack.writeToNBT(new NBTTagCompound());
                            tag.setInteger(NBT_KEY_COUNT, stack.stackSize);
                            list.appendTag(tag);
                        }
                    }
                    return list;
                }
            });
        }
        if (Loader.isModLoaded("JABBA")) {
            REGISTERED_INVENTORY_DECODERS.add(new InventoryDecoder(TileEntityBarrel.class) {

                @Override
                public NBTTagList toNBT(IInventory inv) {
                    NBTTagList list = new NBTTagList();
                    ItemStack stack = inv.getStackInSlot(1);
                    if (stack != null) {
                        NBTTagCompound tag = stack.writeToNBT(new NBTTagCompound());
                        int item_amount = ((TileEntityBarrel) inv).getStorage().getAmount();
                        tag.setInteger(NBT_KEY_COUNT, item_amount);
                        list.appendTag(tag);
                    }
                    return list;
                }
            });
        }
    }

    public static NBTTagList toNBT(IInventory inv) {
        Class<? extends IInventory> teClass = inv.getClass();
        InventoryDecoder decoder = CACHE_MAP.get(teClass);

        if (decoder == null) {
            for (InventoryDecoder possibleDecoder : REGISTERED_INVENTORY_DECODERS) {
                if (possibleDecoder.matches(inv)) {
                    decoder = possibleDecoder;
                    break;
                }
            }

            if (decoder == null) decoder = DEFAULT;

            CACHE_MAP.put(teClass, decoder);
        }
        return decoder.toNBT(inv);
    }
}
