package net.dries007.holoInventory.network;

import static net.dries007.holoInventory.util.NBTKeys.*;

import java.util.ArrayList;
import java.util.List;

import net.dries007.holoInventory.Config;
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
public class BlockInventoryMessage implements IMessage {

    NBTTagCompound data;

    public BlockInventoryMessage(NBTTagCompound inventoryData) {
        data = inventoryData;
    }

    @SuppressWarnings("unused")
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
                NBTTagList list = message.data.getTagList(NBT_KEY_LIST, Constants.NBT.TAG_COMPOUND);
                ItemStack[] itemStacks = new ItemStack[list.tagCount()];
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound tag = list.getCompoundTagAt(i);
                    itemStacks[i] = ItemStack.loadItemStackFromNBT(tag);
                    if (itemStacks[i] != null) itemStacks[i].stackSize = tag.getInteger(NBT_KEY_COUNT);
                }
                NamedData<ItemStack[]> data;
                if (message.data.hasKey(NBT_KEY_CLASS)) data = new NamedData<>(
                        message.data.getString(NBT_KEY_NAME),
                        message.data.getString(NBT_KEY_CLASS),
                        itemStacks);
                else data = new NamedData<>(message.data.getString(NBT_KEY_NAME), itemStacks);
                if (Config.enableStacking) {
                    List<ItemStack> stacks = new ArrayList<>();
                    for (ItemStack stackToAdd : data.data) {
                        if (stackToAdd.stackSize == 0) {
                            stacks.add(stackToAdd);
                            continue;
                        }
                        int remainingAmount = stackToAdd.stackSize;
                        for (ItemStack stackInList : stacks) {
                            if (stackInList == null) continue;

                            if (stackToAdd.isItemEqual(stackInList)
                                    && ItemStack.areItemStackTagsEqual(stackToAdd, stackInList)) {
                                int toMerge = Math.min(remainingAmount, Integer.MAX_VALUE - stackInList.stackSize);
                                if (toMerge > 0) {
                                    stackInList.stackSize += toMerge;
                                    remainingAmount -= toMerge;
                                }
                                if (remainingAmount <= 0) break;
                            }
                        }
                        if (remainingAmount != 0) {
                            ItemStack remainingStack = stackToAdd.copy();
                            remainingStack.stackSize = remainingAmount;
                            stacks.add(remainingStack);
                        }
                    }
                    data.data = stacks.toArray(new ItemStack[0]);
                }
                Renderer.tileInventoryMap.put(message.data.getInteger(NBT_KEY_ID), data);
            }

            return null;
        }
    }
}
