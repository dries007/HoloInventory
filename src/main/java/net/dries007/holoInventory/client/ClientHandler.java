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

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.util.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class ClientHandler
{
    public static final VersionCheck VERSION_CHECK = new VersionCheck();
    public static final KeyManager   KEY_MANAGER   = new KeyManager();
    public static final RenderItem   RENDER_ITEM   = new RenderItem()
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
        enum Result
        {
            UNKNOWN, OK, OLD, ERROR
        }

        public Result result = Result.UNKNOWN;
        public String latest = "";

        @Override
        public void run()
        {
            try
            {
                Minecraft.getMinecraft();
                URL url = new URL(Data.VERSION.replace("MCVERSION", MinecraftForge.MC_VERSION));
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                latest = reader.readLine();

                if (latest.equals(HoloInventory.getVersion())) result = Result.OK;
                else result = Result.OLD;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                result = Result.ERROR;
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
        done = true;
        if (VERSION_CHECK.result.equals(VersionCheck.Result.OLD)) event.player.addChatMessage(new ChatComponentText("[HoloInventory] You are running " + HoloInventory.getVersion() + ", newest available is " + VERSION_CHECK.latest + ". Please update :)"));
    }
}
