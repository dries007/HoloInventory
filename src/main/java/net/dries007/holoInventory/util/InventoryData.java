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

package net.dries007.holoInventory.util;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.dries007.holoInventory.HoloInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.HashMap;

import static net.dries007.holoInventory.util.Data.MODID;

public class InventoryData
{
    public int        id;
    public IInventory te;
    public HashMap<EntityPlayer, Long> playerSet = new HashMap<>();

    public InventoryData(IInventory te, int id)
    {
        this.id = id;
        this.te = te;
    }

    public boolean isOld(EntityPlayer player)
    {
        if (!playerSet.containsKey(player)) return true;
        return player.worldObj.getTotalWorldTime() > playerSet.get(player) + 20 * HoloInventory.instance.config.syncFreq;
    }

    public Packet getPacket()
    {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            stream.write(0);
            Helper.writeNBTTagCompound(toNBT(), stream);
            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return PacketDispatcher.getPacket(MODID, streambyte.toByteArray());
    }

    private NBTTagCompound toNBT()
    {
        NBTTagCompound root = new NBTTagCompound();
        root.setInteger("id", this.id);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < te.getSizeInventory(); i++)
        {
            if (te.getStackInSlot(i) != null)
            {
                list.appendTag(te.getStackInSlot(i).writeToNBT(new NBTTagCompound()));
            }
        }
        root.setTag("list", list);
        return root;
    }

    public void send(EntityPlayerMP player)
    {
        playerSet.put(player, player.worldObj.getTotalWorldTime());
        PacketDispatcher.sendPacketToPlayer(this.getPacket(), (Player) player);
    }
}
