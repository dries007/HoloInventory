/*
 * Copyright (c) 2014. Dries K. Aka Dries007
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

package net.dries007.holoInventory.util;

import com.google.common.base.Strings;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.compat.DecoderRegistry;
import net.dries007.holoInventory.network.BlockInventoryMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;

public class InventoryData
{
    public int id;
    public IInventory te;
    public HashMap<EntityPlayer, NBTTagCompound> playerSet = new HashMap<EntityPlayer, NBTTagCompound>();
    public String name;
    public String type;

    public InventoryData(IInventory te, int id)
    {
        this.id = id;
        this.te = te;
        this.name = Strings.nullToEmpty(te.getInventoryName());
        this.type = te.getClass().getCanonicalName();
        if (type == null) type = te.getClass().getName();
    }

    public void sendIfOld(EntityPlayerMP player)
    {
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("id", this.id);
        data.setString("name", name);
        data.setTag("list", DecoderRegistry.toNBT(te));

        if (!playerSet.containsKey(player) || !playerSet.get(player).equals(data))
        {
            playerSet.put(player, data);
            HoloInventory.getSnw().sendTo(new BlockInventoryMessage(data), player);
        }
    }

    public void update(IInventory inventory)
    {
        te = inventory;
    }

    public String getType()
    {
        return type;
    }
}
