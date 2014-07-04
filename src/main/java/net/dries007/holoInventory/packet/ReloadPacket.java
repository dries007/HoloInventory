package net.dries007.holoInventory.packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.dries007.holoInventory.HoloInventory;
import net.minecraft.entity.player.EntityPlayer;

public class ReloadPacket extends AbstractPacket
{
    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {

    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {

    }

    @Override
    public void handleClientSide(EntityPlayer player)
    {
        HoloInventory.getConfig().reload();
    }

    @Override
    public void handleServerSide(EntityPlayer player)
    {

    }
}
