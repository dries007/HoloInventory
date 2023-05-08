package net.dries007.holoInventory.network;

import static net.dries007.holoInventory.util.NBTKeys.*;

import java.util.ArrayList;
import java.util.List;

import net.dries007.holoInventory.client.Renderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Server -> Client
 */
public class BlockFluidHandlerMessage implements IMessage {

    NBTTagCompound data;

    public BlockFluidHandlerMessage(NBTTagCompound data) {
        this.data = data;
    }

    @SuppressWarnings("unused")
    public BlockFluidHandlerMessage() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        data = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, data);
    }

    public static class Handler implements IMessageHandler<BlockFluidHandlerMessage, IMessage> {

        @Override
        public IMessage onMessage(BlockFluidHandlerMessage message, MessageContext ctx) {
            if (message == null || message.data == null || ctx.side.isServer()) return null;
            NBTTagList list = message.data.getTagList(NBT_KEY_TANK, Constants.NBT.TAG_COMPOUND);
            List<FluidTankInfo> tankInfos = new ArrayList<>();
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                FluidStack stack = FluidStack.loadFluidStackFromNBT(tag);
                if (stack != null) {
                    tankInfos.add(new FluidTankInfo(stack, tag.getInteger(NBT_KEY_CAPACITY)));
                }
            }
            Renderer.tileFluidHandlerMap.put(message.data.getInteger(NBT_KEY_ID), tankInfos);

            return null;
        }
    }
}
