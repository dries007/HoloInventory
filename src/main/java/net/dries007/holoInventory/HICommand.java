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
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (isOP(sender)) throw new CommandException(getCommandUsage(sender));
        else if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h") || args[0].equalsIgnoreCase("?"))  showHelp(sender);
        else if (args[0].equalsIgnoreCase("ban"))
        {
            ServerEventHandler.catchNext = ServerEventHandler.Type.BAN;
            sender.addChatMessage(new TextComponentString("Right click a block.").setStyle(new Style().setColor(TextFormatting.AQUA)));
        }
        else if (args[0].equalsIgnoreCase("unban"))
        {
            if (args.length == 1)
            {
                ServerEventHandler.catchNext = ServerEventHandler.Type.UNBAN;
                sender.addChatMessage(new TextComponentString("Right click a block.").setStyle(new Style().setColor(TextFormatting.AQUA)));
            }
            else
            {
                boolean wasBanned = Helper.banned.remove(args[1]);
                if (wasBanned)
                    sender.addChatMessage(new TextComponentString("Unbanned " + args[1]).setStyle(new Style().setColor(TextFormatting.GREEN)));
                else
                    sender.addChatMessage(new TextComponentString(args[1] + " is not banned.").setStyle(new Style().setColor(TextFormatting.RED)));
            }
        }
        else if (args[0].equalsIgnoreCase("list"))
        {
            sender.addChatMessage(new TextComponentString(HoloInventory.MODID.concat(" banlist:")).setStyle(new Style().setColor(TextFormatting.AQUA)));
            for (String type : Helper.banned)
            {
                sender.addChatMessage(new TextComponentString(type));
            }
        }
        else throw new WrongUsageException(getCommandUsage(sender));
    }

    private void showHelp(ICommandSender sender)
    {
        sender.addChatMessage(new TextComponentString(HoloInventory.MODID).setStyle(new Style().setColor(TextFormatting.AQUA)).appendSibling(new TextComponentString(" by Dries007").setStyle(new Style().setColor(TextFormatting.WHITE))));
        sender.addChatMessage(new TextComponentString("This command depends on the context in which it is used."));
        sender.addChatMessage(new TextComponentString("On a server (OP only) it can be used to (un)ban a block type, server wide."));
        sender.addChatMessage(new TextComponentString("On a client (SSP) it can be used to (un)ban a block type, client side only."));
        sender.addChatMessage(new TextComponentString("Syntax:").setStyle(new Style().setUnderlined(true)));
        sender.addChatMessage(new TextComponentString("/" + getCommandName() + " <ban>"));
        sender.addChatMessage(new TextComponentString("        Ban the next block you right click."));
        sender.addChatMessage(new TextComponentString("/" + getCommandName() + " <unban> [name]"));
        sender.addChatMessage(new TextComponentString("        Un ban the next block you right click, or specified via the name."));
        sender.addChatMessage(new TextComponentString("/" + getCommandName() + " <list>"));
        sender.addChatMessage(new TextComponentString("        Print a list of banned block types on the current side."));
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, "help", "ban", "unban", "list");
        if (args.length == 2 && args[0].equalsIgnoreCase("unban")) return getListOfStringsMatchingLastWord(args, Helper.banned);
        return super.getTabCompletionOptions(server, sender, args, pos);
    }

    private boolean isOP(ICommandSender sender)
    {
        return sender instanceof EntityPlayer && !sender.getServer().getPlayerList().canSendCommands(((EntityPlayer) sender).getGameProfile());
    }
}
