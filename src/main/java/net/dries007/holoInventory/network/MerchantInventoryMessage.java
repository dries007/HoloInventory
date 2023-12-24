package net.dries007.holoInventory.network;

import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_CLASS;
import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_ID;
import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_NAME;

import net.dries007.holoInventory.client.Renderer;
import net.dries007.holoInventory.util.NamedData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.village.MerchantRecipeList;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Server -> Client
 */
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

                if (message.data.hasKey(NBT_KEY_CLASS)) {
                    Renderer.merchantMap.put(
                            message.data.getInteger(NBT_KEY_ID),
                            new NamedData<>(
                                    message.data.getString(NBT_KEY_NAME),
                                    message.data.getString(NBT_KEY_CLASS),
                                    list));
                } else {
                    Renderer.merchantMap.put(
                            message.data.getInteger(NBT_KEY_ID),
                            new NamedData<>(message.data.getString(NBT_KEY_NAME), list));
                }
            }

            return null;
        }
    }
}
