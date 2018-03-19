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

package net.dries007.holoInventory.network.request;

import io.netty.buffer.ByteBuf;
import net.dries007.holoInventory.Helper;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.api.INamedItemHandler;
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
            if (te instanceof ILockableContainer && !ctx.getServerHandler().player.canOpen(((ILockableContainer) te).getLockCode())) return null;

            if (te instanceof TileEntityEnderChest)
            {
                return new PlainInventory(message.pos, ctx.getServerHandler().player.getInventoryEnderChest());
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
                if (iih == null)
                {
                    HoloInventory.getLogger().warn("Error: Block at {} (Class: {} Te: {} Block: {}) returned null after indicating the capability is available.", message.pos, te.getClass().getName(), te, te.getBlockType());
                    return null;
                }
                if (te instanceof INamedItemHandler) {
                	INamedItemHandler namedHandler = (INamedItemHandler) te;
                	return new PlainInventory(message.pos, namedHandler.getItemHandlerName(), iih);
                }
                return new PlainInventory(message.pos, te.getBlockType().getUnlocalizedName(), iih);
            }

            return null;
        }
    }
}
