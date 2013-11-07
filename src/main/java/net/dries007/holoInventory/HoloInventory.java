package net.dries007.holoInventory;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import net.dries007.holoInventory.client.ClientHandler;
import net.dries007.holoInventory.client.ClientPacketHandler;
import net.dries007.holoInventory.lib.developercapes.DevCapesUtil;
import net.dries007.holoInventory.server.ServerHandler;
import net.dries007.holoInventory.server.ServerPacketHandler;

import static net.dries007.holoInventory.util.Data.*;

@NetworkMod(channels = {MODID},
        clientPacketHandlerSpec = @NetworkMod.SidedPacketHandler(channels = {MODID}, packetHandler = ClientPacketHandler.class ),
        serverPacketHandlerSpec = @NetworkMod.SidedPacketHandler(channels = {MODID}, packetHandler = ServerPacketHandler.class ))
@Mod(modid = MODID, name = MODID)
public class HoloInventory
{
    @Mod.Instance(value = MODID)
    public static HoloInventory instance;

    public Config        config;
    public ClientHandler clientHandler;
    public ServerHandler serverHandler;

    @Mod.EventHandler()
    public void fmlEvent(FMLPreInitializationEvent event)
    {
        config = new Config(event.getSuggestedConfigurationFile());
        DevCapesUtil.addFileUrl(CAPES);

        if (event.getSide().isClient()) clientHandler = new ClientHandler();
        serverHandler = new ServerHandler();
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
}
