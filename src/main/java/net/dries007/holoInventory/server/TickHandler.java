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
import net.dries007.holoInventory.util.Helper;
import net.dries007.holoInventory.util.InventoryData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.WorldServer;

import java.util.EnumSet;
import java.util.HashMap;

import static net.dries007.holoInventory.util.Data.MODID;

public class TickHandler implements ITickHandler
{
    public HashMap<Integer, InventoryData> blockMap = new HashMap<Integer, InventoryData>();

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
        MovingObjectPosition mo = Helper.getPlayerLookingSpot(player);
        if (mo != null)
        {
            switch (mo.typeOfHit)
            {
                case TILE:
                    Coord coord = new Coord(world.provider.dimensionId, mo);
                    TileEntity te = world.getBlockTileEntity(coord.x, coord.y, coord.z);

                    if (te instanceof IInventory)
                    {
                        doStuff(coord.hashCode(), (IInventory) te, player);
                    }
                    else if (te instanceof TileEntityEnderChest)
                    {
                        doStuff(coord.hashCode(), player.getInventoryEnderChest(), player);
                    }
                    break;
                case ENTITY:
                    System.out.println(mo.entityHit.entityId);
                    if (mo.entityHit instanceof IInventory)
                    {

                        doStuff(mo.entityHit.entityId, (IInventory) mo.entityHit, player);
                    }
                    break;
            }
        }
    }

    private void doStuff(int id, IInventory inventory, EntityPlayerMP player)
    {

        boolean empty = idEmpty(inventory);
        if (empty && !blockMap.containsKey(id)) return;
        InventoryData inventoryData = blockMap.get(id);
        if (inventoryData == null || inventoryData.isOld(player) || empty)
        {
            inventoryData = new InventoryData(inventory, id);
            inventoryData.send(player);
            blockMap.put(id, inventoryData);
        }
        if (empty && blockMap.containsKey(id)) blockMap.remove(id);
    }

    private boolean idEmpty(IInventory te)
    {
        for (int i = 0; i < te.getSizeInventory(); i++)
        {
            if (te.getStackInSlot(i) != null) return false;
        }

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

    public void clear()
    {
        blockMap.clear();
    }
}
