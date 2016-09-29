package net.dries007.holoInventory;

import net.dries007.holoInventory.server.ServerEventHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class HICommand extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return HoloInventory.MODID.toLowerCase();
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        if (isOP(sender)) return "Use on a SSP world to configure. You don't have permission to modify the server.";
        return "Use '/" + getCommandName() + " help' for more info.";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (isOP(sender)) throw new CommandException(getCommandUsage(sender));
        else if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h") || args[0].equalsIgnoreCase("?"))  showHelp(sender);
        else if (args[0].equalsIgnoreCase("ban"))
        {
            ServerEventHandler.catchNext = ServerEventHandler.Type.BAN;
            sender.addChatMessage(new ChatComponentText("Right click a block.").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA)));
        }
        else if (args[0].equalsIgnoreCase("unban"))
        {
            if (args.length == 1)
            {
                ServerEventHandler.catchNext = ServerEventHandler.Type.UNBAN;
                sender.addChatMessage(new ChatComponentText("Right click a block.").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA)));
            }
            else
            {
                boolean wasBanned = Helper.banned.remove(args[1]);
                if (wasBanned)
                    sender.addChatMessage(new ChatComponentText("Unbanned " + args[1]).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)));
                else
                    sender.addChatMessage(new ChatComponentText(args[1] + " is not banned.").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
            }
        }
        else if (args[0].equalsIgnoreCase("list"))
        {
            sender.addChatMessage(new ChatComponentText(HoloInventory.MODID.concat(" banlist:")).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA)));
            for (String type : Helper.banned)
            {
                sender.addChatMessage(new ChatComponentText(type));
            }
        }
        else throw new WrongUsageException(getCommandUsage(sender));
    }

    private void showHelp(ICommandSender sender)
    {
        sender.addChatMessage(new ChatComponentText(HoloInventory.MODID).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA)).appendSibling(new ChatComponentText(" by Dries007").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE))));
        sender.addChatMessage(new ChatComponentText("This command depends on the context in which it is used."));
        sender.addChatMessage(new ChatComponentText("On a server (OP only) it can be used to (un)ban a block type, server wide."));
        sender.addChatMessage(new ChatComponentText("On a client (SSP) it can be used to (un)ban a block type, client side only."));
        sender.addChatMessage(new ChatComponentText("Syntax:").setChatStyle(new ChatStyle().setUnderlined(true)));
        sender.addChatMessage(new ChatComponentText("/" + getCommandName() + " <ban>"));
        sender.addChatMessage(new ChatComponentText("        Ban the next block you right click."));
        sender.addChatMessage(new ChatComponentText("/" + getCommandName() + " <unban> [name]"));
        sender.addChatMessage(new ChatComponentText("        Un ban the next block you right click, or specified via the name."));
        sender.addChatMessage(new ChatComponentText("/" + getCommandName() + " <list>"));
        sender.addChatMessage(new ChatComponentText("        Print a list of banned block types on the current side."));
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, "help", "ban", "unban", "list");
        if (args.length == 2 && args[0].equalsIgnoreCase("unban")) return getListOfStringsMatchingLastWord(args, Helper.banned);
        return super.addTabCompletionOptions(sender, args, pos);
    }

    private boolean isOP(ICommandSender sender)
    {
        return sender instanceof EntityPlayer && !MinecraftServer.getServer().getConfigurationManager().canSendCommands(((EntityPlayer) sender).getGameProfile());
    }
}
