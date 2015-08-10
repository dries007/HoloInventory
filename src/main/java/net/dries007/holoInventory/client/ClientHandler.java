/*
 * Copyright (c) 2014. Dries K. Aka Dries007
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

package net.dries007.holoInventory.client;

import com.google.common.base.Joiner;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.util.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.io.IOUtils;

import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

import static net.dries007.holoInventory.client.ClientHandler.VersionCheck.Result.*;
import static net.minecraft.event.ClickEvent.Action.OPEN_URL;
import static net.minecraft.util.EnumChatFormatting.*;

public class ClientHandler
{
    public static final VersionCheck VERSION_CHECK = new VersionCheck();
    public static final KeyManager KEY_MANAGER = new KeyManager();
    public static final RenderItem RENDER_ITEM = new RenderItem()
    {
        @Override
        public void doRender(EntityItem par1EntityItem, double par2, double par4, double par6, float par8, float par9)
        {
            try
            {
                super.doRender(par1EntityItem, par2, par4, par6, par8, par9);
            }
            catch (Exception e)
            {
                //e.printStackTrace();
            }
        }

        @Override
        public boolean shouldBob()
        {
            return false;
        }

        @Override
        public boolean shouldSpreadItems()
        {
            return false;
        }
    };

    public void postInit()
    {
        RENDER_ITEM.setRenderManager(RenderManager.instance);
    }

    public static class VersionCheck implements Runnable
    {
        public static final Pattern VERSIONS = Pattern.compile("(?:\\d+\\.)+.*");

        enum Result
        {
            UNKNOWN, OK, OLD, ERROR
        }

        public Result result = UNKNOWN;
        public String latest = "";

        @Override
        public void run()
        {
            try
            {
                Minecraft.getMinecraft();
                URL url = new URL(Data.VERSION.replace("MCVERSION", MinecraftForge.MC_VERSION));
                List<String> lines = IOUtils.readLines(url.openStream());
                for (String line : lines)
                {
                    if (VERSIONS.matcher(line).matches())
                    {
                        if (result != UNKNOWN)
                        {
                            HoloInventory.getLogger().warn("The version checker got more then 1 viable version line back. Here is the entire log:");
                            HoloInventory.getLogger().warn(Joiner.on("\r\n").join(lines));
                            result = ERROR;
                            return;
                        }
                        latest = line;
                        result = HoloInventory.getVersion().equals(latest) ? OK : OLD;
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                result = ERROR;
            }
        }
    }

    public ClientHandler()
    {

    }

    public void init()
    {
        MinecraftForge.EVENT_BUS.register(Renderer.INSTANCE);
        FMLCommonHandler.instance().bus().register(Renderer.INSTANCE);

        FMLCommonHandler.instance().bus().register(KEY_MANAGER);
        MinecraftForge.EVENT_BUS.register(KEY_MANAGER);

        if (HoloInventory.getConfig().doVersionCheck)
        {
            Thread vc = new Thread(VERSION_CHECK);
            vc.setDaemon(true);
            vc.setName(Data.MODID + "VersionCheckThread");
            vc.run();

            FMLCommonHandler.instance().bus().register(this);
        }
    }

    boolean done = false;

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (done) return;
        IChatComponent root = new ChatComponentText("[HoloInventory] ").setChatStyle(new ChatStyle().setColor(AQUA));
        switch (VERSION_CHECK.result)
        {
            case ERROR:
                root.appendSibling(new ChatComponentText("Something went wrong version checking, please check the log file.").setChatStyle(new ChatStyle().setColor(RED)));
                break;
            case OLD:
                root.appendSibling(new ChatComponentText("You are running " + HoloInventory.getVersion() + ", the newest available is " + VERSION_CHECK.latest + ". ").setChatStyle(new ChatStyle().setColor(WHITE)));
                root.appendSibling(new ChatComponentText("Click here!").setChatStyle(new ChatStyle().setColor(GOLD).setChatClickEvent(new ClickEvent(OPEN_URL, "https://www.dries007.net/holoinventory/"))));
                break;
            default:
                return;
        }
        done = true;
        event.player.addChatMessage(root);
    }
}
