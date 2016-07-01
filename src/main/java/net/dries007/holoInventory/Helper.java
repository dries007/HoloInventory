package net.dries007.holoInventory;

import net.minecraft.block.BlockJukebox;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.HashSet;

public class Helper
{
    public static boolean showOnSneak;
    public static boolean showOnSprint;
    public static HashSet<String> banned;

    private Helper()
    {
    }

    public static boolean accept(TileEntity te)
    {
        return te != null && (te instanceof IInventory || te instanceof BlockJukebox.TileEntityJukebox || te instanceof TileEntityEnderChest || te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));
    }

    public static boolean accept(Entity entity)
    {
        return entity != null && (entity instanceof IInventory || entity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));
    }

    public enum Type
    {
        TILE, ENTITY
    }
}
