/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 - 2017 Dries K. Aka Dries007
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.dries007.holoInventory.network.response;

import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import net.dries007.holoInventory.client.renderers.InventoryRenderer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public class PlainInventory extends ResponseMessage
{
    private String name;
    private List<ItemStack> stacks;

    @SuppressWarnings("unused") // netty needs it
    public PlainInventory()
    {

    }

    public PlainInventory(int id, IInventory ii)
    {
        super(id);
        scan(ii);
    }

    public PlainInventory(BlockPos pos, IInventory ii)
    {
        super(pos);
        scan(ii);
    }

    public PlainInventory(int id, String name, IItemHandler ii)
    {
        super(id);
        scan(ii);
        this.name = name;
    }

    public PlainInventory(BlockPos pos, String name, IItemHandler ii)
    {
        super(pos);
        scan(ii);
        this.name = name;
    }

    private void add(ItemStack s)
    {
        if (s.isEmpty()) return;
        stacks.add(s);
    }

    private void scan(IItemHandler ii)
    {
        int size = ii.getSlots();
        stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) add(ii.getStackInSlot(i));
    }

    private void scan(IInventory ii)
    {
        name = ii.getName();
        int size = ii.getSizeInventory();
        stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) add(ii.getStackInSlot(i));
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        super.fromBytes(buf);
        name = ByteBufUtils.readUTF8String(buf);
        int size = buf.readInt();
        stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) add(ByteBufUtils.readItemStack(buf));
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        super.toBytes(buf);
        ByteBufUtils.writeUTF8String(buf, Strings.nullToEmpty(name));
        buf.writeInt(stacks.size());
        for (ItemStack stack : stacks) ByteBufUtils.writeItemStack(buf, stack);
    }

    public PlainInventory setName(String name)
    {
        this.name = name;
        return this;
    }

    public static class Handler implements IMessageHandler<PlainInventory, IMessage>
    {
        @Override
        public IMessage onMessage(PlainInventory message, MessageContext ctx)
        {
            ResponseMessage.handle(message, new InventoryRenderer(message.name, message.stacks));
            return null;
        }
    }
}
