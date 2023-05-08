/*
 * Copyright (c) 2014. Dries K. Aka Dries007 Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions: The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.dries007.holoInventory.server;

import java.util.ArrayList;
import java.util.List;

import net.dries007.holoInventory.Config;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.network.ResetMessage;
import net.dries007.holoInventory.util.InventoryData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CommandHoloInventory extends CommandBase {

    @Override
    public String getCommandName() {
        return "holoinventory";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender) {
        return true;
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        return EnumChatFormatting.RED + "Use /" + getCommandName() + " to get a list of options";
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        switch (args.length) {
            default:
                return null;
            case 1:
                if (isOp(sender)) {
                    return getListOfStringsMatchingLastWord(args, "reset", "reload", "overrideName", "ban", "unban");
                } else {
                    return getListOfStringsMatchingLastWord(args, "reset", "overrideName");
                }
            case 2:
                if (!isOp(sender) || !args[0].equalsIgnoreCase("unban")) {
                    return null;
                } else {
                    return getListOfStringsFromIterableMatchingLastWord(args, getAllList());
                }
        }
    }

    private boolean isOp(ICommandSender sender) {
        return MinecraftServer.getServer().isSinglePlayer() || !(sender instanceof EntityPlayerMP)
                || MinecraftServer.getServer().getConfigurationManager()
                        .func_152596_g(((EntityPlayerMP) sender).getGameProfile());
    }

    private List<String> getAllList() {
        ArrayList<String> temp = new ArrayList<>();
        temp.addAll(Config.bannedEntities);
        temp.addAll(Config.bannedTiles);
        return temp;
    }

    private void sendHelp(ICommandSender sender) {
        sender.addChatMessage(
                new ChatComponentText(
                        EnumChatFormatting.GREEN.toString() + EnumChatFormatting.STRIKETHROUGH
                                + "-----------------------------------------------------"));
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "           HoloInventory By Dries007"));
        sender.addChatMessage(
                new ChatComponentText(
                        EnumChatFormatting.YELLOW + "/"
                                + getCommandName()
                                + " reset "
                                + EnumChatFormatting.GRAY
                                + " - Clears the client cache"));
        sender.addChatMessage(
                new ChatComponentText(
                        EnumChatFormatting.YELLOW + "/"
                                + getCommandName()
                                + " reload "
                                + EnumChatFormatting.GRAY
                                + " - Reloads the server config"));
        sender.addChatMessage(
                new ChatComponentText(
                        EnumChatFormatting.YELLOW + "/"
                                + getCommandName()
                                + " overrideName <new Name> "
                                + EnumChatFormatting.GRAY
                                + " - Give a holo name to the next inventory you right click"));
        sender.addChatMessage(
                new ChatComponentText(
                        EnumChatFormatting.YELLOW + "/"
                                + getCommandName()
                                + " ban "
                                + EnumChatFormatting.GRAY
                                + " - Stops rendering hologram for the next inventory you right click"));
        sender.addChatMessage(
                new ChatComponentText(
                        EnumChatFormatting.YELLOW + "/"
                                + getCommandName()
                                + " unban <name> "
                                + EnumChatFormatting.GRAY
                                + "  -> Unban an inventory"));
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Pro tip: Use tab completion!"));
        sender.addChatMessage(
                new ChatComponentText(
                        EnumChatFormatting.GREEN.toString() + EnumChatFormatting.STRIKETHROUGH
                                + "-----------------------------------------------------"));
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        if (args.length == 0) {

            sendHelp(sender);

        } else if (args[0].equalsIgnoreCase("reset")) {

            if (!(sender instanceof EntityPlayer)) {
                throw new WrongUsageException("You can't use this as the server...");
            }
            HoloInventory.getSnw().sendTo(new ResetMessage(), (EntityPlayerMP) sender);
            for (InventoryData data : ServerHandler.serverEventHandler.mapBlockToInv.values()) {
                data.playerSet.remove(sender);
            }
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Cleared client cache"));

        } else if (args[0].equalsIgnoreCase("reload")) {

            if (sender instanceof EntityPlayer) {
                HoloInventory.getSnw().sendTo(new ResetMessage(), (EntityPlayerMP) sender);
            }
            if (isOp(sender)) {
                HoloInventory.getConfig().reload();
            }
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Reloading request sent to server"));

        } else if (args[0].equalsIgnoreCase("overrideName")) {

            if (!(sender instanceof EntityPlayer)) {
                throw new WrongUsageException(EnumChatFormatting.RED + "You can't use this as the server...");
            }
            if (args.length == 1) {
                throw new WrongUsageException(EnumChatFormatting.RED + "Give a name, can contain spaces.");
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    stringBuilder.append(args[i]).append(' ');
                }
                String name = stringBuilder.toString().trim();
                sender.addChatMessage(
                        new ChatComponentText(
                                EnumChatFormatting.GREEN
                                        + "Right click a block or entity to override the hologram name on that type to "
                                        + EnumChatFormatting.GOLD
                                        + name));
                ServerHandler.serverEventHandler.overrideUsers.put(sender.getCommandSenderName(), name);
            }

        } else if (args[0].equalsIgnoreCase("ban")) {

            if (isOp(sender)) {
                ServerHandler.serverEventHandler.banUsers.add(sender.getCommandSenderName());
                sender.addChatMessage(
                        new ChatComponentText(
                                EnumChatFormatting.GREEN
                                        + "Right click a block or entity to stop rendering the hologram on that type."));
            } else {
                sender.addChatMessage(
                        new ChatComponentText(
                                EnumChatFormatting.RED
                                        + "You are not opped. Ban stuff in SSP to block it client side."));
            }

        } else if (args[0].equalsIgnoreCase("unban")) {

            if (isOp(sender)) {
                if (args.length == 1) {
                    if (getAllList().size() == 0) {
                        sender.addChatMessage(
                                new ChatComponentText(
                                        EnumChatFormatting.RED + "You didn't ban any inventories yet..."));
                    } else {
                        sender.addChatMessage(
                                new ChatComponentText(
                                        EnumChatFormatting.GREEN + "A list of all banned inventories : "
                                                + joinNiceString(getAllList().toArray())));
                    }
                } else if (getAllList().contains(args[1])) {
                    Config.bannedEntities.remove(args[1]);
                    Config.bannedTiles.remove(args[1]);
                    HoloInventory.getConfig().overrideBannedThings();
                    sender.addChatMessage(
                            new ChatComponentText(
                                    EnumChatFormatting.GREEN + "Successfully unbanned "
                                            + EnumChatFormatting.GOLD
                                            + args[1]));
                } else {
                    sender.addChatMessage(
                            new ChatComponentText(
                                    EnumChatFormatting.RED + "That thing is not on any banlist I know of..."));
                }
            } else {
                sender.addChatMessage(
                        new ChatComponentText(
                                EnumChatFormatting.RED
                                        + "You are not opped. Unban stuff in SSP to unblock it client side."));
                sender.addChatMessage(
                        new ChatComponentText(
                                EnumChatFormatting.RED
                                        + "If you think this ban is stupid, ask a server admin to remove it."));
            }

        } else {

            sendHelp(sender);
        }
    }

    @Override
    public int compareTo(Object par1Obj) {
        return super.compareTo((ICommand) par1Obj);
    }
}
