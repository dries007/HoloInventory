package net.dries007.holoInventory.server;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import net.dries007.holoInventory.util.TileData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

public class ServerPacketHandler implements IPacketHandler
{
    public static final ServerPacketHandler INSTANCE = new ServerPacketHandler();

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {

    }

    public void send(EntityPlayerMP playerMP, TileData tileData)
    {
        playerMP.playerNetServerHandler.sendPacketToPlayer(tileData.getPacket());
    }
}
