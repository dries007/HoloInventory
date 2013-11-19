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

package net.dries007.holoInventory;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import net.dries007.holoInventory.client.ClientHandler;
import net.dries007.holoInventory.client.ClientPacketHandler;
import net.dries007.holoInventory.client.Glasses;
import net.dries007.holoInventory.lib.developercapes.DevCapesUtil;
import net.dries007.holoInventory.server.ServerHandler;
import net.dries007.holoInventory.server.ServerPacketHandler;
import net.dries007.holoInventory.util.Data;
import org.mcstats.Metrics;

import java.io.IOException;

import static net.dries007.holoInventory.util.Data.*;

@NetworkMod(channels = {MODID},
        clientPacketHandlerSpec = @NetworkMod.SidedPacketHandler(channels = {MODID},
                packetHandler = ClientPacketHandler.class),
        serverPacketHandlerSpec = @NetworkMod.SidedPacketHandler(channels = {MODID},
                packetHandler = ServerPacketHandler.class))
@Mod(modid = MODID, name = MODID)
public class HoloInventory
{
    @Mod.Instance(value = MODID)
    public static HoloInventory instance;

    public Config        config;
    public ClientHandler clientHandler;
    public ServerHandler serverHandler;
    public Metrics       metrics;

    @Mod.Metadata
    private ModMetadata metadata;

    @Mod.EventHandler()
    public void fmlEvent(FMLPreInitializationEvent event)
    {
        config = new Config(event.getSuggestedConfigurationFile());
        DevCapesUtil.addFileUrl(CAPES);
        Glasses.addFileUrl(GLASSES);

        if (event.getSide().isClient()) clientHandler = new ClientHandler();
        serverHandler = new ServerHandler();

        try
        {
            metrics = new Metrics(Data.MODID, event.getModMetadata().version);
            metrics.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Mod.EventHandler()
    public void fmlEvent(FMLInitializationEvent event)
    {
        if (event.getSide().isClient()) clientHandler.init();
        serverHandler.init();
    }

    @Mod.EventHandler()
    public void fmlEvent(FMLServerStartingEvent event)
    {
        serverHandler.init();
    }

    public String getVersion()
    {
        return metadata.version;
    }
}
