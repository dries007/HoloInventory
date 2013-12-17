package net.dries007.holoInventory.util;

import com.jadarstudios.developercapes.DevCapesUtil;
import net.dries007.holoInventory.client.ClientHandler;
import net.dries007.holoInventory.client.Glasses;

import static net.dries007.holoInventory.util.Data.CAPES;
import static net.dries007.holoInventory.util.Data.GLASSES;

public class ClientProxy extends CommonProxy
{
    private ClientHandler clientHandler;

    @Override
    public void preInit()
    {
        super.preInit();

        DevCapesUtil.addFileUrl(CAPES);
        clientHandler = new ClientHandler();
        Glasses.addFileUrl(GLASSES);
    }

    @Override
    public void init()
    {
        super.init();

        clientHandler.init();
    }
}
