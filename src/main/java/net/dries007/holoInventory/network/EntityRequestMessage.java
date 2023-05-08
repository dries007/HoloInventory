package net.dries007.holoInventory.network;

import static net.dries007.holoInventory.util.NBTKeys.*;

import net.dries007.holoInventory.Config;
import net.dries007.holoInventory.compat.InventoryDecoderRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.DimensionManager;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Client -> Server
 */
public class EntityRequestMessage implements IMessage {

    private int dim;
    private int entityId;

    @SuppressWarnings("unused")
    public EntityRequestMessage() {}

    public EntityRequestMessage(int dim, int entityId) {
        this.dim = dim;
        this.entityId = entityId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        dim = buf.readInt();
        entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dim);
        buf.writeInt(entityId);
    }

    public static class Handler implements IMessageHandler<EntityRequestMessage, IMessage> {

        public static final HashMultimap<Integer, String> map = HashMultimap.create();

        @Override
        public IMessage onMessage(EntityRequestMessage message, MessageContext ctx) {
            if (ctx.side.isServer()) {
                Entity entity = DimensionManager.getWorld(message.dim).getEntityByID(message.entityId);

                if (entity instanceof IInventory || entity instanceof IMerchant) {
                    EntityPlayerMP player = ctx.getServerHandler().playerEntity;

                    if (Config.bannedEntities.contains(entity.getClass().getCanonicalName())) {
                        if (map.containsEntry(message.entityId, player.getDisplayName())) {
                            map.remove(message.entityId, player.getDisplayName());
                            NBTTagCompound root = new NBTTagCompound();
                            root.setByte(NBT_KEY_TYPE, (byte) 1);
                            root.setInteger(NBT_KEY_ID, message.entityId);

                            return new RemoveInventoryMessage(root);
                        }
                        return null;
                    }

                    map.put(message.entityId, player.getDisplayName());

                    if (entity instanceof IInventory) {
                        IInventory inventory = (IInventory) entity;
                        NBTTagCompound root = new NBTTagCompound();
                        root.setInteger(NBT_KEY_ID, message.entityId);
                        root.setString(NBT_KEY_NAME, Strings.nullToEmpty(inventory.getInventoryName()));
                        root.setString(NBT_KEY_CLASS, entity.getClass().getCanonicalName());
                        root.setTag(NBT_KEY_LIST, InventoryDecoderRegistry.toNBT(inventory));
                        return new EntityInventoryMessage(root);
                    } else {
                        NBTTagCompound tag = ((IMerchant) entity).getRecipes(player).getRecipiesAsTags();
                        tag.setInteger(NBT_KEY_ID, message.entityId);
                        tag.setString(NBT_KEY_NAME, entity.getCommandSenderName());
                        tag.setString(NBT_KEY_CLASS, entity.getClass().getCanonicalName());
                        return new MerchantInventoryMessage(tag);
                    }
                }
            }
            return null;
        }
    }
}
