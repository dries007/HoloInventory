package net.dries007.holoInventory.network;

import com.google.common.collect.HashMultimap;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.dries007.holoInventory.HoloInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.DimensionManager;

public class EntityRequestMessage implements IMessage
{
    private int dim;
    private int entityId;

    public EntityRequestMessage()
    {

    }

    public EntityRequestMessage(int dim, int entityId)
    {
        this.dim = dim;
        this.entityId = entityId;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        dim = buf.readInt();
        entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(dim);
        buf.writeInt(entityId);
    }

    public static class Handler implements IMessageHandler<EntityRequestMessage, IMessage>
    {
        public static HashMultimap<Integer, String> map = HashMultimap.create();

        @Override
        public IMessage onMessage(EntityRequestMessage message, MessageContext ctx)
        {
            if (ctx.side.isServer())
            {
                Entity entity = DimensionManager.getWorld(message.dim).getEntityByID(message.entityId);

                if (entity instanceof IInventory || entity instanceof IMerchant)
                {
                    EntityPlayerMP player = ctx.getServerHandler().playerEntity;

                    if (HoloInventory.getConfig().bannedEntities.contains(entity.getClass().getCanonicalName()))
                    {
                        if (map.containsEntry(message.entityId, player.getDisplayName()))
                        {
                            map.remove(message.entityId, player.getDisplayName());
                            NBTTagCompound root = new NBTTagCompound();
                            root.setByte("type", (byte) 1);
                            root.setInteger("id", message.entityId);

                            return new RemoveInventoryMessage(root);
                        }
                        return null;
                    }

                    map.put(message.entityId, player.getDisplayName());

                    if (entity instanceof IInventory)
                    {
                        IInventory inventory = (IInventory) entity;
                        NBTTagCompound root = new NBTTagCompound();
                        root.setInteger("id", message.entityId);
                        root.setString("name", inventory.getInventoryName());
                        root.setString("class", entity.getClass().getCanonicalName());
                        NBTTagList list = new NBTTagList();
                        for (int i = 0; i < inventory.getSizeInventory(); i++)
                        {
                            if (inventory.getStackInSlot(i) != null)
                            {
                                list.appendTag(inventory.getStackInSlot(i).writeToNBT(new NBTTagCompound()));
                            }
                        }
                        root.setTag("list", list);
                        return new EntityInventoryMessage(root);
                    }
                    else
                    {
                        NBTTagCompound tag = ((IMerchant) entity).getRecipes(player).getRecipiesAsTags();
                        tag.setInteger("id", message.entityId);
                        tag.setString("name", entity.getCommandSenderName());
                        tag.setString("class", entity.getClass().getCanonicalName());
                        return new MerchantInventoryMessage(tag);
                    }
                }
            }
            return null;
        }
    }
}
