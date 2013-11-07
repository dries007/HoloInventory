package net.dries007.holoInventory.server;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import net.dries007.holoInventory.util.Coord;
import net.dries007.holoInventory.util.TileData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import static net.dries007.holoInventory.util.Data.*;

import java.util.EnumSet;
import java.util.HashMap;

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

                TileData tileData = temp.get(coord);
                if (tileData == null || tileData.isOld(world, player))
                {
                    tileData = new TileData(te, coord);
                    tileData.send(player);
                    temp.put(coord, tileData);
                }
            }
        }
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
