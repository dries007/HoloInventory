package net.dries007.holoInventory.network.response;

import io.netty.buffer.ByteBuf;
import net.dries007.holoInventory.Helper;
import net.dries007.holoInventory.client.ClientEventHandler;
import net.dries007.holoInventory.client.renderers.IRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public abstract class ResponseMessage implements IMessage
{
    private Helper.Type type;
    private int id;
    private BlockPos pos;

    @SuppressWarnings("unused") // netty again
    public ResponseMessage()
    {
    }

    public ResponseMessage(int id)
    {
        this.type = Helper.Type.ENTITY;
        this.id = id;
    }

    public ResponseMessage(BlockPos pos)
    {
        this.type = Helper.Type.TILE;
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        type = Helper.Type.values()[buf.readByte()];
        switch (type)
        {
            case TILE: pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()); break;
            case ENTITY: id = buf.readInt(); break;
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(type.ordinal());
        switch (type)
        {
            case TILE:
                buf.writeInt(pos.getX());
                buf.writeInt(pos.getY());
                buf.writeInt(pos.getZ());
                break;
            case ENTITY:
                buf.writeInt(id);
                break;
        }
    }

    public final Helper.Type getType()
    {
        return type;
    }

    public final BlockPos getPos()
    {
        return pos;
    }

    public final int getId()
    {
        return id;
    }

    public static void handle(ResponseMessage message, IRenderer renderer)
    {
        switch (message.getType())
        {
            case TILE: ClientEventHandler.cache(message.getPos(), renderer); break;
            case ENTITY: ClientEventHandler.cache(message.getId(), renderer); break;
        }
    }
}
