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

package net.dries007.holoInventory.server;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import net.dries007.holoInventory.util.Coord;
import net.dries007.holoInventory.util.TileData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;

import java.util.EnumSet;
import java.util.HashMap;

import static net.dries007.holoInventory.util.Data.MODID;

public class TickHandler implements ITickHandler
{
    public HashMap<Coord, TileData> temp = new HashMap<Coord, TileData>();

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {

    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        EntityPlayerMP player = (EntityPlayerMP) tickData[0];
        WorldServer world = player.getServerForPlayer();
        if (world == null) return;
        MovingObjectPosition mo = getPlayerLookingSpot(player);
        if (mo != null && mo.typeOfHit == EnumMovingObjectType.TILE)
        {
            Coord coord = new Coord(world.provider.dimensionId, mo);
            TileEntity te = world.getBlockTileEntity(coord.x, coord.y, coord.z);
            if (te != null && te instanceof IInventory)
            {
                boolean empty = idEmpty((IInventory) te);
                if (empty && !temp.containsKey(coord)) return;
                TileData tileData = temp.get(coord);
                if (tileData == null || tileData.isOld(world, player) || empty)
                {
                    tileData = new TileData(te, coord);
                    tileData.send(player);
                    temp.put(coord, tileData);
                }
                if (empty && temp.containsKey(coord)) temp.remove(coord);
            }
        }
    }

    private boolean idEmpty(IInventory te)
    {
        for (int i = 0; i < te.getSizeInventory(); i++)
            if (te.getStackInSlot(i) != null) return false;

        return true;
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.PLAYER);
    }

    @Override
    public String getLabel()
    {
        return MODID + "_ServerTickHandler";
    }

    public static MovingObjectPosition getPlayerLookingSpot(EntityPlayer player)
    {
        float var4 = 1.0F;
        float var5 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * var4;
        float var6 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * var4;
        double var7 = player.prevPosX + (player.posX - player.prevPosX) * var4;
        double var9 = player.prevPosY + (player.posY - player.prevPosY) * var4 + 1.62D - player.yOffset;
        double var11 = player.prevPosZ + (player.posZ - player.prevPosZ) * var4;
        Vec3 var13 = player.worldObj.getWorldVec3Pool().getVecFromPool(var7, var9, var11);
        float var14 = MathHelper.cos(-var6 * 0.017453292F - (float) Math.PI);
        float var15 = MathHelper.sin(-var6 * 0.017453292F - (float) Math.PI);
        float var16 = -MathHelper.cos(-var5 * 0.017453292F);
        float var17 = MathHelper.sin(-var5 * 0.017453292F);
        float var18 = var15 * var16;
        float var20 = var14 * var16;
        double var21 = 5D;
        if (player instanceof EntityPlayerMP)
        {
            var21 = ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance();
        }
        Vec3 var23 = var13.addVector(var18 * var21, var17 * var21, var20 * var21);
        return player.worldObj.rayTraceBlocks_do_do(var13, var23, false, !true);
    }

    public void clear()
    {
        temp.clear();
    }
}
