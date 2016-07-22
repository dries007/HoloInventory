package net.dries007.holoInventory.network.response;

import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import net.dries007.holoInventory.client.renderers.MerchantRenderer;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MerchantRecipes extends ResponseMessage
{
    private String name;
    private NBTTagCompound tag;

    @SuppressWarnings("unused")
    public MerchantRecipes()
    {

    }

    public MerchantRecipes(int id, IMerchant entity, EntityPlayerMP player)
    {
        super(id);
        name = entity.getDisplayName() == null ? "" : entity.getDisplayName().getFormattedText();
        MerchantRecipeList recipes = entity.getRecipes(player);
        if (recipes == null) recipes = new MerchantRecipeList();
        tag = recipes.getRecipiesAsTags();
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        super.fromBytes(buf);
        name = ByteBufUtils.readUTF8String(buf);
        tag = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        super.toBytes(buf);
        ByteBufUtils.writeUTF8String(buf, Strings.nullToEmpty(name));
        ByteBufUtils.writeTag(buf, tag);
    }

    public static class Handler implements IMessageHandler<MerchantRecipes, IMessage>
    {
        @Override
        public IMessage onMessage(MerchantRecipes message, MessageContext ctx)
        {
            ResponseMessage.handle(message, new MerchantRenderer(message.name, new MerchantRecipeList(message.tag)));
            return null;
        }
    }
}
