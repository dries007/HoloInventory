package net.dries007.holoInventory.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.dries007.holoInventory.Config;
import net.dries007.holoInventory.client.Renderer;
import net.dries007.holoInventory.util.NamedData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class BlockInventoryMessage implements IMessage {
    NBTTagCompound data;

    public BlockInventoryMessage(NBTTagCompound inventoryData) {
        data = inventoryData;
    }

    public BlockInventoryMessage() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        data = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, data);
    }

    public static class Handler implements IMessageHandler<BlockInventoryMessage, IMessage> {
        @Override
        public IMessage onMessage(BlockInventoryMessage message, MessageContext ctx) {
            if (message == null || message.data == null) return null; // hun?
            if (ctx.side.isClient()) {
                NBTTagList list = message.data.getTagList("list", 10);
                ItemStack[] itemStacks = new ItemStack[list.tagCount()];
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound tag = list.getCompoundTagAt(i);
                    itemStacks[i] = ItemStack.loadItemStackFromNBT(tag);
                    if (itemStacks[i] != null) itemStacks[i].stackSize = tag.getInteger("Count");
                }
                NamedData<ItemStack[]> data;
                if (message.data.hasKey("class"))
                    data = new NamedData<>(message.data.getString("name"), message.data.getString("class"), itemStacks);
                else data = new NamedData<>(message.data.getString("name"), itemStacks);
                if (Config.enableStacking) {
                    List<ItemStack> stacks = new ArrayList<>();
                    for (ItemStack stackToAdd : data.data) {
                        boolean f = false;
                        for (ItemStack stackInList : stacks) {
                            if (stackInList == null) continue;
                            if (stackToAdd.isItemEqual(stackInList)
                                    && ItemStack.areItemStackTagsEqual(stackToAdd, stackInList)) {
                                stackInList.stackSize += stackToAdd.stackSize;
                                f = true;
                                break;
                            }
                        }
                        if (!f) stacks.add(stackToAdd.copy());
                    }
                    data.data = stacks.toArray(new ItemStack[0]);
                }
                Renderer.tileMap.put(message.data.getInteger("id"), data);
            }

            return null;
        }
    }
}
