package net.dries007.holoInventory.server;

import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ServerHandler
{
    public TickHandler tickHandler;

    public ServerHandler()
    {

    }

    public void init()
    {
        if (tickHandler == null)
        {
            tickHandler = new TickHandler();
            TickRegistry.registerTickHandler(tickHandler, Side.SERVER);
        }
        else
            tickHandler.clear();
    }
}
