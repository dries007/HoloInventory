package net.dries007.holoInventory.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.dries007.holoInventory.HoloInventory;

public class ReloadMessage implements IMessage
{
    @Override
    public void fromBytes(ByteBuf buf)
    {

    }

    @Override
    public void toBytes(ByteBuf buf)
    {

    }

    public static class Handler implements IMessageHandler<ReloadMessage, IMessage>
    {
        @Override
        public IMessage onMessage(ReloadMessage message, MessageContext ctx)
        {
            if (ctx.side.isClient())
            {
                HoloInventory.getConfig().reload();
            }

            return null;
        }
    }
}
