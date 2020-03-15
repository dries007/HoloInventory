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

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.dries007.holoInventory.compat.DecoderRegistry;
import net.dries007.holoInventory.items.HoloGlasses;
import net.dries007.holoInventory.network.*;
import net.dries007.holoInventory.server.CommandHoloInventory;
import net.dries007.holoInventory.util.CommonProxy;
import net.minecraft.item.Item;
import org.apache.logging.log4j.Logger;

import static net.dries007.holoInventory.util.Data.MODID;

@Mod(modid = MODID, name = MODID, acceptableRemoteVersions = "*", dependencies="after:Baubles;after:TConstruct")
public class HoloInventory {

	@Mod.Instance(value = MODID)
    private static HoloInventory instance;
    public static Item holoGlasses;
    private Config config;

    @Mod.Metadata
    private ModMetadata metadata;

    @SidedProxy(serverSide = "net.dries007.holoInventory.util.CommonProxy", clientSide = "net.dries007.holoInventory.util.ClientProxy")
    public static CommonProxy proxy;
    private SimpleNetworkWrapper snw;
    private Logger logger;

    public static boolean isBaublesLoaded, isTinkersLoaded = false;

    @Mod.EventHandler()
    public void preInit(FMLPreInitializationEvent event) {

    	isBaublesLoaded = Loader.isModLoaded("Baubles");
    	isTinkersLoaded = Loader.isModLoaded("TConstruct");

        logger = event.getModLog();
        config = new Config(event.getSuggestedConfigurationFile());

        holoGlasses = new HoloGlasses("Hologlasses");
        GameRegistry.registerItem(holoGlasses, "Hologlasses", MODID);

        int id = 0;
        snw = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        snw.registerMessage(BlockInventoryMessage.Handler.class, BlockInventoryMessage.class, id++, Side.CLIENT);
        snw.registerMessage(EntityInventoryMessage.Handler.class, EntityInventoryMessage.class, id++, Side.CLIENT);
        snw.registerMessage(EntityRequestMessage.Handler.class, EntityRequestMessage.class, id++, Side.SERVER);
        snw.registerMessage(MerchantInventoryMessage.Handler.class, MerchantInventoryMessage.class, id++, Side.CLIENT);
        snw.registerMessage(ReloadMessage.Handler.class, ReloadMessage.class, id++, Side.CLIENT);
        snw.registerMessage(RemoveInventoryMessage.Handler.class, RemoveInventoryMessage.class, id++, Side.CLIENT);
        snw.registerMessage(RenameMessage.Handler.class, RenameMessage.class, id++, Side.CLIENT);
        snw.registerMessage(ResetMessage.Handler.class, ResetMessage.class, id++, Side.CLIENT);

        proxy.preInit();

        DecoderRegistry.init();
    }

    @Mod.EventHandler()
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    @Mod.EventHandler()
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }

    @Mod.EventHandler()
    public void fmlEvent(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandHoloInventory());

        proxy.serverStarting();
    }

    public static String getVersion() {
        return getInstance().metadata.version;
    }

    public static Config getConfig() {
        return instance.config;
    }

    public static HoloInventory getInstance() {
        return instance;
    }

    public static SimpleNetworkWrapper getSnw() {
        return instance.snw;
    }

    public static Logger getLogger() {
        return getInstance().logger;
    }
}
