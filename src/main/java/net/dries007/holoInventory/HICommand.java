/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 - 2017 Dries K. Aka Dries007
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.dries007.holoInventory;

import net.dries007.holoInventory.server.ServerEventHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class HICommand extends CommandBase
{
    @Override
    public String getName()
    {
        return HoloInventory.MODID.toLowerCase();
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        if (isOP(sender)) return "Use on a SSP world to configure. You don't have permission to modify the server.";
        return "Use '/" + getName() + " help' for more info.";
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (isOP(sender)) throw new CommandException(getUsage(sender));
        else if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h") || args[0].equalsIgnoreCase("?"))  showHelp(sender);
        else if (args[0].equalsIgnoreCase("ban"))
        {
            ServerEventHandler.catchNext = ServerEventHandler.Type.BAN;
            sender.sendMessage(new TextComponentString("Right click a block.").setStyle(new Style().setColor(TextFormatting.AQUA)));
        }
        else if (args[0].equalsIgnoreCase("unban"))
        {
            if (args.length == 1)
            {
                ServerEventHandler.catchNext = ServerEventHandler.Type.UNBAN;
                sender.sendMessage(new TextComponentString("Right click a block.").setStyle(new Style().setColor(TextFormatting.AQUA)));
            }
            else
            {
                boolean wasBanned = Helper.banned.remove(args[1]);
                if (wasBanned)
                    sender.sendMessage(new TextComponentString("Unbanned " + args[1]).setStyle(new Style().setColor(TextFormatting.GREEN)));
                else
                    sender.sendMessage(new TextComponentString(args[1] + " is not banned.").setStyle(new Style().setColor(TextFormatting.RED)));
            }
        }
        else if (args[0].equalsIgnoreCase("list"))
        {
            sender.sendMessage(new TextComponentString(HoloInventory.MODID.concat(" banlist:")).setStyle(new Style().setColor(TextFormatting.AQUA)));
            for (String type : Helper.banned)
            {
                sender.sendMessage(new TextComponentString(type));
            }
        }
        else throw new WrongUsageException(getUsage(sender));
    }

    private void showHelp(ICommandSender sender)
    {
        sender.sendMessage(new TextComponentString(HoloInventory.MODID).setStyle(new Style().setColor(TextFormatting.AQUA)).appendSibling(new TextComponentString(" by Dries007").setStyle(new Style().setColor(TextFormatting.WHITE))));
        sender.sendMessage(new TextComponentString("This command depends on the context in which it is used."));
        sender.sendMessage(new TextComponentString("On a server (OP only) it can be used to (un)ban a block type, server wide."));
        sender.sendMessage(new TextComponentString("On a client (SSP) it can be used to (un)ban a block type, client side only."));
        sender.sendMessage(new TextComponentString("Syntax:").setStyle(new Style().setUnderlined(true)));
        sender.sendMessage(new TextComponentString("/" + getName() + " <ban>"));
        sender.sendMessage(new TextComponentString("        Ban the next block you right click."));
        sender.sendMessage(new TextComponentString("/" + getName() + " <unban> [name]"));
        sender.sendMessage(new TextComponentString("        Un ban the next block you right click, or specified via the name."));
        sender.sendMessage(new TextComponentString("/" + getName() + " <list>"));
        sender.sendMessage(new TextComponentString("        Print a list of banned block types on the current side."));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, "help", "ban", "unban", "list");
        if (args.length == 2 && args[0].equalsIgnoreCase("unban")) return getListOfStringsMatchingLastWord(args, Helper.banned);
        return super.getTabCompletions(server, sender, args, pos);
    }

    private boolean isOP(ICommandSender sender)
    {
        return sender instanceof EntityPlayer && !sender.getServer().getPlayerList().canSendCommands(((EntityPlayer) sender).getGameProfile());
    }
}
