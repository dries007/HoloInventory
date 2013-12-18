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

import net.dries007.holoInventory.HoloInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public class ServerEventHandler
{
    public List<String> banUsers = new ArrayList<String>();

    @ForgeSubscribe()
    public void event(PlayerInteractEvent event)
    {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        if (banUsers.contains(event.entityPlayer.getDisplayName()))
        {
            banUsers.remove(event.entityPlayer.getDisplayName());
            event.setCanceled(true);

            TileEntity te = event.entity.worldObj.getBlockTileEntity(event.x, event.y, event.z);
            if (te instanceof IInventory)
            {
                HoloInventory.getConfig().bannedTiles.add(te.getClass().getCanonicalName());
                event.entityPlayer.sendChatToPlayer(ChatMessageComponent.createFromText(te.getClass().getCanonicalName() + " will no longer display a hologram."));
            }
            else
            {
                event.entityPlayer.sendChatToPlayer(ChatMessageComponent.createFromText("That is no inventory. Try again."));
            }

            HoloInventory.getConfig().overrideBannedThings();
        }
    }

    @ForgeSubscribe()
    public void event(EntityInteractEvent event)
    {
        if (banUsers.contains(event.entityPlayer.getDisplayName()))
        {
            banUsers.remove(event.entityPlayer.getDisplayName());
            event.setCanceled(true);

            if (event.target instanceof IInventory)
            {
                HoloInventory.getConfig().bannedEntities.add(event.target.getClass().getCanonicalName());
                event.entityPlayer.sendChatToPlayer(ChatMessageComponent.createFromText(event.target.getClass().getCanonicalName() + " will no longer display a hologram."));
            }
            else
            {
                event.entityPlayer.sendChatToPlayer(ChatMessageComponent.createFromText("That is no inventory. Try again."));
            }
            HoloInventory.getConfig().overrideBannedThings();
        }
    }
}
