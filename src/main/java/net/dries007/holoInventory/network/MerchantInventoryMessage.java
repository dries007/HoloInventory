package net.dries007.holoInventory.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.dries007.holoInventory.client.Renderer;
import net.dries007.holoInventory.util.NamedData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.village.MerchantRecipeList;

public class MerchantInventoryMessage implements IMessage {

    NBTTagCompound data;

    public MerchantInventoryMessage(NBTTagCompound tag) {
        data = tag;
    }

    @SuppressWarnings("unused") // used by FML reflection
    public MerchantInventoryMessage() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        data = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, data);
    }

    public static class Handler implements IMessageHandler<MerchantInventoryMessage, IMessage> {
        @Override
        public IMessage onMessage(MerchantInventoryMessage message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                MerchantRecipeList list = new MerchantRecipeList();
                list.readRecipiesFromTags(message.data);

                if (message.data.hasKey("class")) {
                    Renderer.merchantMap.put(
                            message.data.getInteger("id"),
                            new NamedData<>(message.data.getString("name"), message.data.getString("class"), list));
                } else {
                    Renderer.merchantMap.put(
                            message.data.getInteger("id"), new NamedData<>(message.data.getString("name"), list));
                }
            }

            return null;
        }
    }
}
