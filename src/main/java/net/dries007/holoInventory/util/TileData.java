package net.dries007.holoInventory.util;

import cpw.mods.fml.common.network.PacketDispatcher;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.server.ServerPacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import static net.dries007.holoInventory.util.Data.MODID;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

public class TileData
{
    public Coord coord;
    public TileEntity te;
    public HashMap<EntityPlayer, Long> playerSet = new HashMap<>();

    public TileData(TileEntity te, Coord coord)
    {
        this.coord = coord;
        this.te = te;
    }

    public boolean isOld(World world, EntityPlayer player)
    {
        return world.getTotalWorldTime() > playerSet.get(player) + 20 * 10;
    }

    public Packet getPacket()
    {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
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
        root.setCompoundTag("coord", this.coord.toNBT());
        NBTTagList list = new NBTTagList();
        IInventory inventory = ((IInventory)te);
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            if (inventory.getStackInSlot(i) != null)
            {
                list.appendTag(inventory.getStackInSlot(i).writeToNBT(new NBTTagCompound()));
            }
        }
        root.setTag("list", list);
        return root;
    }

    public void send(EntityPlayerMP player)
    {
        playerSet.put(player, player.worldObj.getTotalWorldTime());
        ServerPacketHandler.INSTANCE.send(player, this);
    }
}
