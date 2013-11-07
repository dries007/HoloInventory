package net.dries007.holoInventory.client;

import net.dries007.holoInventory.util.Coord;
import net.dries007.holoInventory.util.TileData;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.ForgeSubscribe;

import java.util.HashMap;

public class Renderer
{
    public HashMap<Coord, ItemStack[]> temp = new HashMap<Coord, ItemStack[]>();

    @ForgeSubscribe
    public void renderEvent(RenderGameOverlayEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE)
        {
            Coord coord = new Coord(mc.theWorld.provider.dimensionId, mc.objectMouseOver);
            if (temp.containsKey(coord))
            {
                for (ItemStack itemStack : temp.get(coord))
                {
                    System.out.println(itemStack);

                    //TODO: Render the hologram
                }
            }
        }
    }

    public void read(NBTTagCompound tag)
    {
        Coord coord = new Coord(tag.getCompoundTag("coord"));
        NBTTagList list = tag.getTagList("list");
        ItemStack[] itemStacks = new ItemStack[list.tagCount()];
        for (int i = 0; i < list.tagCount(); i ++)
        {
            itemStacks[i] = ItemStack.loadItemStackFromNBT((NBTTagCompound) list.tagAt(i));
            System.out.println(itemStacks[i]); //TODO: Dev code, remove.
        }
        temp.put(coord, itemStacks);
    }
}
