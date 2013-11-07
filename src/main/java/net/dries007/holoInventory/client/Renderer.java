/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Dries K. Aka Dries007
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.dries007.holoInventory.client;

import net.dries007.holoInventory.util.Coord;
import net.minecraft.client.Minecraft;
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
        for (int i = 0; i < list.tagCount(); i++)
        {
            itemStacks[i] = ItemStack.loadItemStackFromNBT((NBTTagCompound) list.tagAt(i));
            System.out.println(itemStacks[i]); //TODO: Dev code, remove.
        }
        temp.put(coord, itemStacks);
    }
}
