/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Dries K. Aka Dries007
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

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.registry.GameRegistry;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.util.Data;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class ClientHandler
{
    public final Renderer renderer;
    public static final VersionCheck VERSION_CHECK = new VersionCheck();
    public static final KeyManager   KEY_MANAGER   = new KeyManager();
    public static final RenderItem   RENDER_ITEM   = new RenderItem()
    {
        @Override
        public void doRenderItem(EntityItem par1EntityItem, double par2, double par4, double par6, float par8, float par9)
        {
            try
            {
                super.doRenderItem(par1EntityItem, par2, par4, par6, par8, par9);
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
                URL url = new URL(Data.VERSION);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                latest = reader.readLine();

                if (latest.equals(HoloInventory.instance.getVersion())) result = Result.OK;
                else result = Result.OLD;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                result = Result.ERROR;
            }
        }
    }

    public static final IPlayerTracker PLAYER_TRACKER = new IPlayerTracker()
    {
        boolean done = false;

        @Override
        public void onPlayerLogin(EntityPlayer player)
        {
            if (done) return;

            done = true;
            if (VERSION_CHECK.result.equals(VersionCheck.Result.OLD))
                player.addChatMessage("[HoloInventory] You are running " + HoloInventory.instance.getVersion() + ", newest available is " + VERSION_CHECK.latest + ". Please update :)");
        }

        @Override
        public void onPlayerLogout(EntityPlayer player)
        {
        }

        @Override
        public void onPlayerChangedDimension(EntityPlayer player)
        {
        }

        @Override
        public void onPlayerRespawn(EntityPlayer player)
        {
        }
    };

    public ClientHandler()
    {
        renderer = new Renderer();
        RENDER_ITEM.setRenderManager(RenderManager.instance);
    }

    public void init()
    {
        MinecraftForge.EVENT_BUS.register(renderer);

        if (HoloInventory.instance.config.keyMode != 0)
        {
            KeyBindingRegistry.registerKeyBinding(KEY_MANAGER);
        }

        if (HoloInventory.instance.config.doVersionCheck)
        {
            Thread vc = new Thread(VERSION_CHECK);
            vc.setDaemon(true);
            vc.setName(Data.MODID + "VersionCheckThread");
            vc.run();

            GameRegistry.registerPlayerTracker(PLAYER_TRACKER);
        }
    }
}
