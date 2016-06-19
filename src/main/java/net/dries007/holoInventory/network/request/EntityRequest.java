package net.dries007.holoInventory.network.request;

import io.netty.buffer.ByteBuf;
import net.dries007.holoInventory.network.response.PlainInventory;
import net.dries007.holoInventory.network.response.ResponseMessage;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.CapabilityItemHandler;

public class EntityRequest extends RequestMessage
{
    private int id;

    @SuppressWarnings("unused") // netty needs this
    public EntityRequest()
    {
        super();
    }

    public EntityRequest(int dimension, int id)
    {
        super(dimension);
        this.id = id;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        super.fromBytes(buf);
        id = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(id);
    }

    public static class Handler implements IMessageHandler<EntityRequest, ResponseMessage>
    {
        @Override
        public ResponseMessage onMessage(EntityRequest message, MessageContext ctx)
        {
            World world = DimensionManager.getWorld(message.dim);
            if (world == null) return null;
            Entity entity = world.getEntityByID(message.id);
            if (entity == null) return null;

            if (entity instanceof IInventory) return new PlainInventory(message.id, (IInventory) entity);
            else if (entity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
            {
                return new PlainInventory(message.id, entity.getName(), entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));
            }

            return null;
        }
    }
}
