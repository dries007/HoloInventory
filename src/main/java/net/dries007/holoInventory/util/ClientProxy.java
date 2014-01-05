package net.dries007.holoInventory.util;

import com.jadarstudios.developercapes.DevCapesUtil;
import net.dries007.holoInventory.client.ClientHandler;

import static net.dries007.holoInventory.util.Data.CAPES;

public class ClientProxy extends CommonProxy
{
    private ClientHandler clientHandler;

    @Override
    public void preInit()
    {
        super.preInit();

        DevCapesUtil.addFileUrl(CAPES);
        clientHandler = new ClientHandler();
    }

    @Override
    public void init()
    {
        super.init();

        clientHandler.init();
    }
}
