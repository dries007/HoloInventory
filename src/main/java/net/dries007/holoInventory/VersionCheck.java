package net.dries007.holoInventory;

import com.google.common.base.Joiner;
import net.dries007.holoInventory.HoloInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.commons.io.IOUtils;

import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Disabled for now, since Forge seems to have something build in.
 */
@SuppressWarnings("unused")
class VersionCheck implements Runnable
{
    private static final Pattern VERSIONS = Pattern.compile("(?:\\d+\\.)+.*");
    private boolean done = false;
    private String latest;

    private enum Result
    {
        UNKNOWN, OK, OLD, ERROR
    }

    private Result result = Result.UNKNOWN;

    VersionCheck()
    {
        Thread vc = new Thread(this);
        vc.setDaemon(true);
        vc.setName(HoloInventory.MODID + "-VersionCheckThread");
        vc.run();
    }

    @Override
    public void run()
    {
        MinecraftForge.EVENT_BUS.register(this);
        try
        {
            Minecraft.getMinecraft();
            URL url = new URL(HoloInventory.URL + "version." + MinecraftForge.MC_VERSION + ".txt");
            List<String> lines = IOUtils.readLines(url.openStream());
            for (String line : lines)
            {
                if (VERSIONS.matcher(line).matches())
                {
                    if (result != Result.UNKNOWN)
                    {
                        HoloInventory.getLogger().warn("The version checker got more then 1 viable version line back. Here is the entire log:");
                        HoloInventory.getLogger().warn(Joiner.on("\r\n").join(lines));
                        result = Result.ERROR;
                        return;
                    }
                    latest = line;
                    result = HoloInventory.getVersion().equals(latest) ? Result.OK : Result.OLD;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            result = Result.ERROR;
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (done) return;
        ITextComponent root = new TextComponentString("[HoloInventory] ").setStyle(new Style().setColor(TextFormatting.AQUA));
        switch (result)
        {
            case ERROR:
                root.appendSibling(new TextComponentString("Something went wrong version checking, please check the log file.").setStyle(new Style().setColor(TextFormatting.RED)));
                break;
            case OLD:
                root.appendSibling(new TextComponentString("You are running " + HoloInventory.getVersion() + ", the newest available is " + latest + ". ").setStyle(new Style().setColor(TextFormatting.RED)));
                root.appendSibling(new TextComponentString("Click here!").setStyle(new Style().setColor(TextFormatting.GOLD).setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, HoloInventory.URL))));
                break;
            default:
                return;
        }
        done = true;
        event.player.addChatComponentMessage(root);
        MinecraftForge.EVENT_BUS.unregister(this);
    }
}
