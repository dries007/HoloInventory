package net.dries007.holoInventory.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.dries007.holoInventory.client.Renderer;
import net.dries007.holoInventory.util.NamedData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class EntityInventoryMessage implements IMessage
{
    NBTTagCompound data;

    public EntityInventoryMessage()
    {

    }

    public EntityInventoryMessage(NBTTagCompound inventoryData)
    {
        data = inventoryData;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        data = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeTag(buf, data);
    }

    public static class Handler implements IMessageHandler<EntityInventoryMessage, IMessage>
    {
        @Override
        public IMessage onMessage(EntityInventoryMessage message, MessageContext ctx)
        {
            if (ctx.side.isClient())
            {
                NBTTagList list = message.data.getTagList("list", 10);
                ItemStack[] itemStacks = new ItemStack[list.tagCount()];
                for (int i = 0; i < list.tagCount(); i++)
                {
                    NBTTagCompound tag = list.getCompoundTagAt(i);
                    itemStacks[i] = ItemStack.loadItemStackFromNBT(tag);
                    if (itemStacks[i] != null) itemStacks[i].stackSize = tag.getInteger("Count");
                }
                if (message.data.hasKey("class")) Renderer.entityMap.put(message.data.getInteger("id"), new NamedData<ItemStack[]>(message.data.getString("name"), message.data.getString("class"), itemStacks));
                else Renderer.entityMap.put(message.data.getInteger("id"), new NamedData<ItemStack[]>(message.data.getString("name"), itemStacks));
            }

            return null;
        }
    }
}
