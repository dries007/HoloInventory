package net.dries007.holoInventory.network;

import static net.dries007.holoInventory.util.NBTKeys.*;

import net.dries007.holoInventory.client.Renderer;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Server -> Client
 */
public class RemoveInventoryMessage implements IMessage {

    NBTTagCompound data;

    @SuppressWarnings("unused")
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
                switch (message.data.getByte(NBT_KEY_TYPE)) {
                    case 0:
                        Renderer.tileInventoryMap.remove(message.data.getInteger(NBT_KEY_ID));
                    case 1:
                        Renderer.entityMap.remove(message.data.getInteger(NBT_KEY_ID));
                    case 2:
                        Renderer.merchantMap.remove(message.data.getInteger(NBT_KEY_ID));
                }
            }

            return null;
        }
    }
}
