package net.dries007.holoInventory.network;

import static net.dries007.holoInventory.util.NBTKeys.*;

import net.dries007.holoInventory.client.Renderer;
import net.dries007.holoInventory.util.NamedData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Server -> Client
 */
public class EntityInventoryMessage implements IMessage {

    NBTTagCompound data;

    public EntityInventoryMessage(NBTTagCompound inventoryData) {
        data = inventoryData;
    }

    @SuppressWarnings("unused") // used by FML reflection
    public EntityInventoryMessage() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        data = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, data);
    }

    public static class Handler implements IMessageHandler<EntityInventoryMessage, IMessage> {

        @Override
        public IMessage onMessage(EntityInventoryMessage message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                NBTTagList list = message.data.getTagList(NBT_KEY_LIST, Constants.NBT.TAG_COMPOUND);
                ItemStack[] itemStacks = new ItemStack[list.tagCount()];
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound tag = list.getCompoundTagAt(i);
                    itemStacks[i] = ItemStack.loadItemStackFromNBT(tag);
                    if (itemStacks[i] != null) itemStacks[i].stackSize = tag.getInteger(NBT_KEY_COUNT);
                }
                if (message.data.hasKey(NBT_KEY_CLASS)) {
                    Renderer.entityMap.put(
                            message.data.getInteger(NBT_KEY_ID),
                            new NamedData<>(
                                    message.data.getString(NBT_KEY_NAME),
                                    message.data.getString(NBT_KEY_CLASS),
                                    itemStacks));
                } else {
                    Renderer.entityMap.put(
                            message.data.getInteger(NBT_KEY_ID),
                            new NamedData<>(message.data.getString(NBT_KEY_NAME), itemStacks));
                }
            }

            return null;
        }
    }
}
