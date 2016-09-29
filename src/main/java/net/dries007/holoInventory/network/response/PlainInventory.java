package net.dries007.holoInventory.network.response;

import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import net.dries007.holoInventory.client.renderers.InventoryRenderer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.IItemHandler;

public class PlainInventory extends ResponseMessage
{
    private String name;
    private ItemStack[] stacks;

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

    private void scan(IItemHandler ii)
    {
        stacks = new ItemStack[ii.getSlots()];
        for (int i = 0; i < stacks.length; i++)
        {
            stacks[i] = ii.getStackInSlot(i);
        }
    }

    private void scan(IInventory ii)
    {
        name = ii.getName();
        stacks = new ItemStack[ii.getSizeInventory()];
        for (int i = 0; i < stacks.length; i++)
        {
            stacks[i] = ii.getStackInSlot(i);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        super.fromBytes(buf);
        name = ByteBufUtils.readUTF8String(buf);
        stacks = new ItemStack[buf.readInt()];
        for (int i = 0; i < stacks.length; i++) stacks[i] = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        super.toBytes(buf);
        ByteBufUtils.writeUTF8String(buf, Strings.nullToEmpty(name));
        buf.writeInt(stacks.length);
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
