/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 - 2017 Dries K. Aka Dries007
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

package net.dries007.holoInventory;

import net.minecraft.block.BlockJukebox;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.HashSet;

public class Helper
{
    public static boolean showOnSneak;
    public static boolean showOnSprint;
    public static HashSet<String> banned;

    private Helper()
    {
    }

    public static boolean accept(TileEntity te)
    {
        return te != null && (te instanceof IInventory || te instanceof BlockJukebox.TileEntityJukebox || te instanceof TileEntityEnderChest || te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));
    }

    public static boolean accept(Entity entity)
    {
        return entity != null && (entity instanceof IInventory || entity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));
    }

    public enum Type
    {
        TILE, ENTITY
    }
}
