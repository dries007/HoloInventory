package net.dries007.holoInventory.server;

import net.dries007.holoInventory.Helper;
import net.dries007.holoInventory.HoloInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
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
    public void onRightClick(PlayerInteractEvent.RightClickBlock event)
    {
        if (catchNext == Type.NONE) return;
        boolean ban = catchNext == Type.BAN;
        catchNext = Type.NONE;
        event.setCanceled(true);

        TileEntity te = event.getWorld().getTileEntity(event.getPos());

        if (te == null)
        {
            event.getEntityPlayer().addChatComponentMessage(new TextComponentString("That block does not have a TileEntity.").setStyle(new Style().setColor(TextFormatting.RED)));
            return;
        }

        if (ban)
        {
            if (Helper.banned.add(te.getClass().getCanonicalName()))
                event.getEntityPlayer().addChatComponentMessage(new TextComponentString("Banned " + te.getClass().getCanonicalName()).setStyle(new Style().setColor(TextFormatting.GREEN)));
            else
                event.getEntityPlayer().addChatComponentMessage(new TextComponentString(te.getClass().getCanonicalName() + " is already banned.").setStyle(new Style().setColor(TextFormatting.RED)));
        }
        else
        {
            boolean wasBanned = Helper.banned.remove(te.getClass().getCanonicalName());
            if (wasBanned)
                event.getEntityPlayer().addChatComponentMessage(new TextComponentString("Unbanned " + te.getClass().getCanonicalName()).setStyle(new Style().setColor(TextFormatting.GREEN)));
            else
                event.getEntityPlayer().addChatComponentMessage(new TextComponentString(te.getClass().getCanonicalName() + " is not banned. Perhaps it is banned on the " + (FMLCommonHandler.instance().getSide().isClient() ? "server" : "client") + "?").setStyle(new Style().setColor(TextFormatting.RED)));
        }

        HoloInventory.getInstance().saveBanned();
    }
}
