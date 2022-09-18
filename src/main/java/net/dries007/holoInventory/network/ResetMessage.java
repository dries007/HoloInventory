package net.dries007.holoInventory.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.dries007.holoInventory.client.Renderer;

public class ResetMessage implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<ResetMessage, IMessage> {
        @Override
        public IMessage onMessage(ResetMessage message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                Renderer.entityMap.clear();
                Renderer.requestMap.clear();
                Renderer.tileMap.clear();
                Renderer.merchantMap.clear();
            }
            return null;
        }
    }
}
