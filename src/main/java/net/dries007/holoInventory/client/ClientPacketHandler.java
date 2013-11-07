package net.dries007.holoInventory.client;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.util.Helper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public class ClientPacketHandler implements IPacketHandler
{
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        System.out.println("Got!");
        try
        {
            ByteArrayInputStream streambyte = new ByteArrayInputStream(packet.data);
            DataInputStream stream = new DataInputStream(streambyte);

            HoloInventory.instance.clientHandler.renderer.read(Helper.readNBTTagCompound(stream));

            stream.close();
            streambyte.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
