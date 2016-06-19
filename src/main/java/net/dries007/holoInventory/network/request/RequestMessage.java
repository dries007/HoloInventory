package net.dries007.holoInventory.network.request;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

abstract class RequestMessage implements IMessage
{
    int dim;

    RequestMessage()
    {

    }

    RequestMessage(int dim)
    {
        this.dim = dim;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        dim = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(dim);
    }
}
