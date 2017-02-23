package net.dries007.holoInventory.network.request;

import io.netty.buffer.ByteBuf;
import net.dries007.holoInventory.Helper;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.network.response.PlainInventory;
import net.dries007.holoInventory.network.response.ResponseMessage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockJukebox;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileRequest extends RequestMessage
{
    private BlockPos pos;

    @SuppressWarnings("unused") // required for netty
    public TileRequest()
    {
        super();
    }

    public TileRequest(int dimension, BlockPos blockPos)
    {
        super(dimension);
        this.pos = blockPos;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        super.fromBytes(buf);
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
    }

    public static class Handler implements IMessageHandler<TileRequest, ResponseMessage>
    {
        @Override
        public ResponseMessage onMessage(TileRequest message, MessageContext ctx)
        {
            World world = DimensionManager.getWorld(message.dim);
            if (world == null) return null;
            TileEntity te = world.getTileEntity(message.pos);
            if (te == null) return null;

            if (Helper.banned.contains(te.getClass().getCanonicalName())) return null;
            if (te instanceof ILockableContainer && !ctx.getServerHandler().playerEntity.canOpen(((ILockableContainer) te).getLockCode())) return null;

            if (te instanceof TileEntityEnderChest)
            {
                return new PlainInventory(message.pos, ctx.getServerHandler().playerEntity.getInventoryEnderChest());
            }
            else if (te instanceof BlockJukebox.TileEntityJukebox)
            {
                InventoryBasic ib = new InventoryBasic("minecraft:jukebox", false, 1);
                ib.setInventorySlotContents(0, ((BlockJukebox.TileEntityJukebox) te).getRecord());
                return new PlainInventory(message.pos, ib).setName(Blocks.JUKEBOX.getUnlocalizedName());
            }
            else if (te instanceof TileEntityChest)
            {
                Block b = world.getBlockState(message.pos).getBlock();
                if (b instanceof BlockChest)
                {
                    IInventory i = ((BlockChest) b).getLockableContainer(world, message.pos);
                    if (i != null) return new PlainInventory(message.pos, i);
                    return null;
                }
                return new PlainInventory(message.pos, ((TileEntityChest) te));
            }
            else if (te instanceof IInventory)
            {
                return new PlainInventory(message.pos, (IInventory) te);
            }
            else if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
            {
                IItemHandler iih = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                //noinspection ConstantConditions
                if (iih == null)
                {
                    HoloInventory.getLogger().warn("Error: Block at {} (Class: {} Te: {} Block: {}) returned null after indicating the capability is available.", message.pos, te.getClass().getName(), te, te.getBlockType());
                    return null;
                }
                return new PlainInventory(message.pos, te.getBlockType().getUnlocalizedName(), iih);
            }

            return null;
        }
    }
}
