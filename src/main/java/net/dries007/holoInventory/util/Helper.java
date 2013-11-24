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

import com.google.common.collect.HashMultimap;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.client.Renderer;
import net.dries007.holoInventory.server.ServerPacketHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.common.DimensionManager;

import java.io.*;

import static net.dries007.holoInventory.util.Data.MODID;

public class Helper
{
    public static void writeNBTTagCompound(NBTTagCompound par0NBTTagCompound, DataOutput par1DataOutput) throws IOException
    {
        if (par0NBTTagCompound == null)
        {
            par1DataOutput.writeShort(-1);
        }
        else
        {
            byte[] abyte = CompressedStreamTools.compress(par0NBTTagCompound);
            par1DataOutput.writeShort((short) abyte.length);
            par1DataOutput.write(abyte);
        }
    }

    public static NBTTagCompound readNBTTagCompound(DataInput par0DataInput) throws IOException
    {
        short short1 = par0DataInput.readShort();

        if (short1 < 0)
        {
            return null;
        }
        else
        {
            byte[] abyte = new byte[short1];
            par0DataInput.readFully(abyte);
            return CompressedStreamTools.decompress(abyte);
        }
    }

    public static void readTile(NBTTagCompound tag)
    {
        NBTTagList list = tag.getTagList("list");
        ItemStack[] itemStacks = new ItemStack[list.tagCount()];
        for (int i = 0; i < list.tagCount(); i++)
        {
            itemStacks[i] = ItemStack.loadItemStackFromNBT((NBTTagCompound) list.tagAt(i));
        }
        Renderer.tileMap.put(tag.getInteger("id"), itemStacks);
    }

    public static void readEntity(NBTTagCompound tag)
    {
        NBTTagList list = tag.getTagList("list");
        ItemStack[] itemStacks = new ItemStack[list.tagCount()];
        for (int i = 0; i < list.tagCount(); i++)
        {
            itemStacks[i] = ItemStack.loadItemStackFromNBT((NBTTagCompound) list.tagAt(i));
        }
        Renderer.entityMap.put(tag.getInteger("id"), itemStacks);
    }

    public static void readMerchant(NBTTagCompound tag)
    {
        MerchantRecipeList list = new MerchantRecipeList();
        list.readRecipiesFromTags(tag);

        Renderer.merchantMap.put(tag.getInteger("id"), list);
    }

    public static void readRemove(NBTTagCompound tag)
    {
        switch (tag.getByte("type"))
        {
            case 0:
                Renderer.tileMap.remove(tag.getInteger("id"));
            case 1:
                Renderer.entityMap.remove(tag.getInteger("id"));
            case 2:
                Renderer.merchantMap.remove(tag.getInteger("id"));
        }
    }

    public static HashMultimap<Integer, String> map = HashMultimap.create();

    public static void respond(int dim, int entityId, EntityPlayer player)
    {
        Entity entity = DimensionManager.getWorld(dim).getEntityByID(entityId);

        if (entity instanceof IInventory || entity instanceof IMerchant)
        {
            if (HoloInventory.getConfig().bannedEntities.contains(entity.getClass().getCanonicalName()))
            {
                if (map.containsEntry(entityId, player.getDisplayName()))
                {
                    map.remove(entityId, player.getDisplayName());
                    ServerPacketHandler.INSTANCE.sendRemove((Player) player, (byte) 1, entityId);
                }
                return;
            }

            map.put(entityId, player.getDisplayName());

            if (entity instanceof IInventory)
            {
                IInventory inventory = (IInventory) entity;
                NBTTagCompound root = new NBTTagCompound();
                root.setInteger("id", entityId);
                NBTTagList list = new NBTTagList();
                for (int i = 0; i < inventory.getSizeInventory(); i++)
                {
                    if (inventory.getStackInSlot(i) != null)
                    {
                        list.appendTag(inventory.getStackInSlot(i).writeToNBT(new NBTTagCompound()));
                    }
                }
                root.setTag("list", list);

                ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
                DataOutputStream stream = new DataOutputStream(streambyte);
                try
                {
                    stream.write(1);
                    Helper.writeNBTTagCompound(root, stream);
                    stream.close();
                    streambyte.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                PacketDispatcher.sendPacketToPlayer(PacketDispatcher.getPacket(MODID, streambyte.toByteArray()), (Player) player);
            }
            else
            {
                NBTTagCompound tag = ((IMerchant) entity).getRecipes(player).getRecipiesAsTags();
                tag.setInteger("id", entityId);
                ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
                DataOutputStream stream = new DataOutputStream(streambyte);
                try
                {
                    stream.write(3);
                    Helper.writeNBTTagCompound(tag, stream);
                    stream.close();
                    streambyte.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                PacketDispatcher.sendPacketToPlayer(PacketDispatcher.getPacket(MODID, streambyte.toByteArray()), (Player) player);
            }
        }
    }

    public static void request(int dim, int entityId)
    {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(streambyte);
        try
        {
            stream.write(1);

            stream.writeInt(dim);
            stream.writeInt(entityId);

            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(MODID, streambyte.toByteArray()));
    }

    public static MovingObjectPosition getPlayerLookingSpot(EntityPlayer par2EntityPlayer)
    {
        float f = 1.0F;
        float f1 = par2EntityPlayer.prevRotationPitch + (par2EntityPlayer.rotationPitch - par2EntityPlayer.prevRotationPitch) * f;
        float f2 = par2EntityPlayer.prevRotationYaw + (par2EntityPlayer.rotationYaw - par2EntityPlayer.prevRotationYaw) * f;
        double d0 = par2EntityPlayer.prevPosX + (par2EntityPlayer.posX - par2EntityPlayer.prevPosX) * (double) f;
        double d1 = par2EntityPlayer.prevPosY + (par2EntityPlayer.posY - par2EntityPlayer.prevPosY) * (double) f + (double) (par2EntityPlayer.worldObj.isRemote ? par2EntityPlayer.getEyeHeight() - par2EntityPlayer.getDefaultEyeHeight() : par2EntityPlayer.getEyeHeight()); // isRemote check to revert changes to ray trace position due to adding the eye height clientside and player yOffset differences
        double d2 = par2EntityPlayer.prevPosZ + (par2EntityPlayer.posZ - par2EntityPlayer.prevPosZ) * (double) f;
        Vec3 vec3 = par2EntityPlayer.worldObj.getWorldVec3Pool().getVecFromPool(d0, d1, d2);
        float f3 = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI);
        float f4 = MathHelper.sin(-f2 * 0.017453292F - (float) Math.PI);
        float f5 = -MathHelper.cos(-f1 * 0.017453292F);
        float f6 = MathHelper.sin(-f1 * 0.017453292F);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        double d3 = 5.0D;
        if (par2EntityPlayer instanceof EntityPlayerMP)
        {
            d3 = ((EntityPlayerMP) par2EntityPlayer).theItemInWorldManager.getBlockReachDistance();
        }
        Vec3 vec31 = vec3.addVector((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
        return par2EntityPlayer.worldObj.rayTraceBlocks_do_do(vec3, vec31, false, true);
    }

    public static IInventory getInventory(final ItemStack... itemStacks)
    {
        return new IInventory()
        {
            @Override
            public int getSizeInventory()
            {
                return itemStacks.length;
            }

            @Override
            public ItemStack getStackInSlot(int i)
            {
                return itemStacks[i];
            }

            @Override
            public ItemStack decrStackSize(int i, int j)
            {
                return null;
            }

            @Override
            public ItemStack getStackInSlotOnClosing(int i)
            {
                return null;
            }

            @Override
            public void setInventorySlotContents(int i, ItemStack itemstack)
            {
            }

            @Override
            public String getInvName()
            {
                return "";
            }

            @Override
            public boolean isInvNameLocalized()
            {
                return false;
            }

            @Override
            public int getInventoryStackLimit()
            {
                return 64;
            }

            @Override
            public void onInventoryChanged()
            {
            }

            @Override
            public boolean isUseableByPlayer(EntityPlayer entityplayer)
            {
                return false;
            }

            @Override
            public void openChest()
            {
            }

            @Override
            public void closeChest()
            {
            }

            @Override
            public boolean isItemValidForSlot(int i, ItemStack itemstack)
            {
                return false;
            }
        };
    }
}
