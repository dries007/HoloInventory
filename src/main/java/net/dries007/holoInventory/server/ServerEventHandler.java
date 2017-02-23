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

package net.dries007.holoInventory.server;

import net.dries007.holoInventory.Helper;
import net.dries007.holoInventory.HoloInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ServerEventHandler
{
    public static final ServerEventHandler I = new ServerEventHandler();

    private ServerEventHandler()
    {

    }

    public static Type catchNext = Type.NONE;

    public enum Type
    {
        NONE, BAN, UNBAN
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRightClick(PlayerInteractEvent.RightClickBlock event)
    {
        if (catchNext == Type.NONE) return;
        boolean ban = catchNext == Type.BAN;
        catchNext = Type.NONE;
        event.setCanceled(true);

        TileEntity te = event.getWorld().getTileEntity(event.getPos());

        if (te == null)
        {
            event.getEntityPlayer().sendMessage(new TextComponentString("That block does not have a TileEntity.").setStyle(new Style().setColor(TextFormatting.RED)));
            return;
        }

        if (ban)
        {
            if (Helper.banned.add(te.getClass().getCanonicalName()))
                event.getEntityPlayer().sendMessage(new TextComponentString("Banned " + te.getClass().getCanonicalName()).setStyle(new Style().setColor(TextFormatting.GREEN)));
            else
                event.getEntityPlayer().sendMessage(new TextComponentString(te.getClass().getCanonicalName() + " is already banned.").setStyle(new Style().setColor(TextFormatting.RED)));
        }
        else
        {
            boolean wasBanned = Helper.banned.remove(te.getClass().getCanonicalName());
            if (wasBanned)
                event.getEntityPlayer().sendMessage(new TextComponentString("Unbanned " + te.getClass().getCanonicalName()).setStyle(new Style().setColor(TextFormatting.GREEN)));
            else
                event.getEntityPlayer().sendMessage(new TextComponentString(te.getClass().getCanonicalName() + " is not banned. Perhaps it is banned on the " + (FMLCommonHandler.instance().getSide().isClient() ? "server" : "client") + "?").setStyle(new Style().setColor(TextFormatting.RED)));
        }

        HoloInventory.getInstance().saveBanned();
    }
}
