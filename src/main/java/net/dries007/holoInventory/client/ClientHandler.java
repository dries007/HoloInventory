package net.dries007.holoInventory.client;

import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

public class ClientHandler
{
    public final Renderer renderer;
    public ClientHandler()
    {
        renderer = new Renderer();
    }

    public void init()
    {
        MinecraftForge.EVENT_BUS.register(renderer);
    }
}
