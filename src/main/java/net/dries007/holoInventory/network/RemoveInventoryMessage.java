package net.dries007.holoInventory.network;

import net.dries007.holoInventory.client.Renderer;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class RemoveInventoryMessage implements IMessage {

    NBTTagCompound data;

    public RemoveInventoryMessage() {}

    public RemoveInventoryMessage(NBTTagCompound root) {
        data = root;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        data = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, data);
    }

    public static class Handler implements IMessageHandler<RemoveInventoryMessage, IMessage> {

        @Override
        public IMessage onMessage(RemoveInventoryMessage message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                switch (message.data.getByte("type")) {
                    case 0:
                        Renderer.tileMap.remove(message.data.getInteger("id"));
                    case 1:
                        Renderer.entityMap.remove(message.data.getInteger("id"));
                    case 2:
                        Renderer.merchantMap.remove(message.data.getInteger("id"));
                }
            }

            return null;
        }
    }
}
