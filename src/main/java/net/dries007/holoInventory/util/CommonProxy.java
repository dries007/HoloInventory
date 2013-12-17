package net.dries007.holoInventory.util;

import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.server.ServerHandler;
import org.mcstats.Metrics;

import java.io.IOException;

public class CommonProxy
{

    private ServerHandler serverHandler;

    public void preInit()
    {
        serverHandler = new ServerHandler();

        try
        {
            new Metrics(Data.MODID, HoloInventory.getVersion()).start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void init()
    {
        serverHandler.init();
    }

    public void serverStarting()
    {
        serverHandler.init();
    }
}
