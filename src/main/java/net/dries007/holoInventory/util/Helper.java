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

import com.google.common.collect.HashMultimap;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.packet.EntityInventoryPacket;
import net.dries007.holoInventory.packet.MerchantInventoryPacket;
import net.dries007.holoInventory.packet.PacketPipeline;
import net.dries007.holoInventory.packet.RemoveInventoryPacket;
import net.minecraft.block.BlockJukebox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.DimensionManager;

public class Helper
{
    public static boolean weWant(Object o)
    {
        return o != null && (o instanceof IInventory || o instanceof IMerchant || o instanceof TileEntityEnderChest || o instanceof BlockJukebox.TileEntityJukebox);
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
                    NBTTagCompound root = new NBTTagCompound();
                    root.setByte("type", (byte) 1);
                    root.setInteger("id", entityId);
                    PacketPipeline.PIPELINE.sendTo(new RemoveInventoryPacket(root), (EntityPlayerMP) player);
                }
                return;
            }

            map.put(entityId, player.getDisplayName());

            if (entity instanceof IInventory)
            {
                IInventory inventory = (IInventory) entity;
                NBTTagCompound root = new NBTTagCompound();
                root.setInteger("id", entityId);
                root.setString("name", inventory.getInventoryName());
                root.setString("class", entity.getClass().getCanonicalName());
                NBTTagList list = new NBTTagList();
                for (int i = 0; i < inventory.getSizeInventory(); i++)
                {
                    if (inventory.getStackInSlot(i) != null)
                    {
                        list.appendTag(inventory.getStackInSlot(i).writeToNBT(new NBTTagCompound()));
                    }
                }
                root.setTag("list", list);

                PacketPipeline.PIPELINE.sendTo(new EntityInventoryPacket(root), (EntityPlayerMP) player);
            }
            else
            {
                NBTTagCompound tag = ((IMerchant) entity).getRecipes(player).getRecipiesAsTags();
                tag.setInteger("id", entityId);
                tag.setString("name", entity.getCommandSenderName());
                tag.setString("class", entity.getClass().getCanonicalName());

                PacketPipeline.PIPELINE.sendTo(new MerchantInventoryPacket(tag), (EntityPlayerMP) player);
            }
        }
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
        return par2EntityPlayer.worldObj.rayTraceBlocks(vec3, vec31);
    }
}
