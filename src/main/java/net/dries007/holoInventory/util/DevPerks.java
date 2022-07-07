/*
 * Copyright (c) 2014, Dries007.net
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of the project nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.dries007.holoInventory.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameData;
import java.net.URL;
import java.nio.charset.Charset;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.apache.commons.io.IOUtils;

/**
 * Something other than capes for once
 *
 * @author Dries007
 */
public class DevPerks {
    private static final String PERKS_URL = "http://doubledoordev.net/perks.json";
    private JsonObject perks = new JsonObject();
    private boolean debug;

    public DevPerks(boolean debug) {
        this.debug = debug;
        try {
            perks = new JsonParser()
                    .parse(IOUtils.toString(new URL(PERKS_URL), Charset.forName("UTF-8")))
                    .getAsJsonObject();
        } catch (Exception e) {
            if (debug) e.printStackTrace();
        }
    }

    /**
     * Something other than capes for once
     */
    @SubscribeEvent
    public void nameFormatEvent(PlayerEvent.NameFormat event) {
        try {
            if (debug)
                perks = new JsonParser()
                        .parse(IOUtils.toString(new URL(PERKS_URL), Charset.forName("UTF-8")))
                        .getAsJsonObject();
            if (perks.has(event.username)) {
                JsonObject perk = perks.getAsJsonObject(event.username);
                if (perk.has("displayname"))
                    event.displayname = perk.get("displayname").getAsString();
                if (perk.has("hat")
                        && (event.entityPlayer.inventory.armorInventory[3] == null
                                || event.entityPlayer.inventory.armorInventory[3].stackSize == 0)) {
                    JsonObject hat = perk.getAsJsonObject("hat");
                    String name = hat.get("name").getAsString();
                    int meta = hat.has("meta") ? hat.get("meta").getAsInt() : 0;
                    event.entityPlayer.inventory.armorInventory[3] =
                            new ItemStack(GameData.getItemRegistry().getObject(name), 0, meta);
                }
            }
        } catch (Exception e) {
            if (debug) e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void nameFormatEvent(PlayerEvent.Clone event) {
        try {
            if (debug)
                perks = new JsonParser()
                        .parse(IOUtils.toString(new URL(PERKS_URL), Charset.forName("UTF-8")))
                        .getAsJsonObject();
            if (perks.has(event.original.getCommandSenderName())) {
                JsonObject perk = perks.getAsJsonObject(event.original.getCommandSenderName());
                if (perk.has("hat")
                        && (event.entityPlayer.inventory.armorInventory[3] == null
                                || event.entityPlayer.inventory.armorInventory[3].stackSize == 0)) {
                    JsonObject hat = perk.getAsJsonObject("hat");
                    String name = hat.get("name").getAsString();
                    int meta = hat.has("meta") ? hat.get("meta").getAsInt() : 0;
                    event.entityPlayer.inventory.armorInventory[3] =
                            new ItemStack(GameData.getItemRegistry().getObject(name), 0, meta);
                }
            }
        } catch (Exception e) {
            if (debug) e.printStackTrace();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void deathEvent(PlayerDropsEvent event) {
        try {
            if (debug)
                perks = new JsonParser()
                        .parse(IOUtils.toString(new URL(PERKS_URL), Charset.forName("UTF-8")))
                        .getAsJsonObject();
            if (perks.has(event.entityPlayer.getCommandSenderName())) {
                JsonObject perk = perks.getAsJsonObject(event.entityPlayer.getCommandSenderName());
                if (perk.has("drop")) {
                    JsonObject drop = perk.getAsJsonObject("drop");
                    String name = drop.get("name").getAsString();
                    int meta = drop.has("meta") ? drop.get("meta").getAsInt() : 0;
                    int size = drop.has("size") ? drop.get("size").getAsInt() : 1;
                    event.drops.add(new EntityItem(
                            event.entityPlayer.getEntityWorld(),
                            event.entityPlayer.posX,
                            event.entityPlayer.posY,
                            event.entityPlayer.posZ,
                            new ItemStack(GameData.getItemRegistry().getObject(name), size, meta)));
                }
            }
        } catch (Exception e) {
            if (debug) e.printStackTrace();
        }
    }
}
