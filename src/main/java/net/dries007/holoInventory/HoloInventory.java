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

package net.dries007.holoInventory;

import net.dries007.holoInventory.client.ClientEventHandler;
import net.dries007.holoInventory.client.ConfigGuiFactory;
import net.dries007.holoInventory.network.request.EntityRequest;
import net.dries007.holoInventory.network.request.TileRequest;
import net.dries007.holoInventory.network.response.PlainInventory;
import net.dries007.holoInventory.network.response.ResponseMessage;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import static net.dries007.holoInventory.HoloInventory.GUI_FACTORY;
import static net.dries007.holoInventory.HoloInventory.MODID;
import static net.dries007.holoInventory.HoloInventory.URL;

@Mod(modid = MODID, name = MODID, canBeDeactivated = true, updateJSON = URL + "update.json", guiFactory = GUI_FACTORY)
public class HoloInventory
{
    public static final String MODID = "HoloInventory";
    public static final String URL = "https://dries007.net/holoinventory/";
    public static final String GUI_FACTORY = "net.dries007.holoInventory.client.ConfigGuiFactory";

    @Mod.Instance(value = MODID)
    private static HoloInventory instance;

    @Mod.Metadata
    private ModMetadata metadata;

    private SimpleNetworkWrapper snw;
    private Logger logger;
    private Configuration config;

    @Mod.EventHandler
    public void disableEvent(FMLModDisabledEvent event)
    {
        HoloInventory.getLogger().info("Mod disabled via Mods list.");
    }

    @Mod.EventHandler
    public void fmlEvent(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        config = new Configuration(event.getSuggestedConfigurationFile());
        updateConfig();

        int id = 0;
        snw = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

        // Request packets (client -> server)
        snw.registerMessage(EntityRequest.Handler.class, EntityRequest.class, id++, Side.SERVER);
        snw.registerMessage(TileRequest.Handler.class, TileRequest.class, id++, Side.SERVER);

        // Response packets (server -> client)
        snw.registerMessage(PlainInventory.Handler.class, PlainInventory.class, id++, Side.CLIENT);

        if (event.getSide().isClient())
        {
            //new VersionCheck(); -- replaced by forge system for now
            ClientEventHandler.init();
        }

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void updateConfig(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(MODID)) updateConfig();
    }

    private void updateConfig()
    {
        Helper.showOnSneak = config.get(MODID, "showOnSneak", false).setRequiresWorldRestart(false).setRequiresMcRestart(false).getBoolean();
        Helper.showOnSprint = config.get(MODID, "showOnSprint", false).setRequiresWorldRestart(false).setRequiresMcRestart(false).getBoolean();

        if (config.hasChanged()) config.save();
    }

    public static String getVersion()
    {
        return instance.metadata.version;
    }

    public static HoloInventory getInstance()
    {
        return instance;
    }

    public static SimpleNetworkWrapper getSnw()
    {
        return instance.snw;
    }

    public static Logger getLogger()
    {
        return instance.logger;
    }

    public static Configuration getConfig()
    {
        return instance.config;
    }
}
