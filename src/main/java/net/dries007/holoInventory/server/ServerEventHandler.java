package net.dries007.holoInventory.server;

import net.dries007.holoInventory.Helper;
import net.dries007.holoInventory.HoloInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ServerEventHandler
{
    public static final ServerEventHandler I = new ServerEventHandler();

    private ServerEventHandler()
    {

    }

    public static Type catchNext = Type.NONE;

    public enum Type
    {
        NONE, BAN, UNBAN
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRightClick(PlayerInteractEvent event)
    {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;

        if (catchNext == Type.NONE) return;
        boolean ban = catchNext == Type.BAN;
        catchNext = Type.NONE;
        event.setCanceled(true);

        TileEntity te = event.world.getTileEntity(event.pos);

        if (te == null)
        {
            event.entityPlayer.addChatComponentMessage(new ChatComponentText("That block does not have a TileEntity.").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
            return;
        }

        if (ban)
        {
            if (Helper.banned.add(te.getClass().getCanonicalName()))
                event.entityPlayer.addChatComponentMessage(new ChatComponentText("Banned " + te.getClass().getCanonicalName()).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)));
            else
                event.entityPlayer.addChatComponentMessage(new ChatComponentText(te.getClass().getCanonicalName() + " is already banned.").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
        }
        else
        {
            boolean wasBanned = Helper.banned.remove(te.getClass().getCanonicalName());
            if (wasBanned)
                event.entityPlayer.addChatComponentMessage(new ChatComponentText("Unbanned " + te.getClass().getCanonicalName()).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)));
            else
                event.entityPlayer.addChatComponentMessage(new ChatComponentText(te.getClass().getCanonicalName() + " is not banned. Perhaps it is banned on the " +
                        (FMLCommonHandler.instance().getSide().isClient() ? "server" : "client") + "?").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
        }

        HoloInventory.getInstance().saveBanned();
    }
}
