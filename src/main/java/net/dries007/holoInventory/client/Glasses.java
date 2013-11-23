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

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelSkeletonHead;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

public class Glasses
{
    public static final Glasses instance = new Glasses();

    private HashMap<String, String>                  users           = new HashMap<>();
    private HashMap<String, ResourceLocation>        resources       = new HashMap<>();
    private HashMap<String, ThreadDownloadImageData> downloadThreads = new HashMap<>();

    ModelSkeletonHead modelskeletonhead = new ModelSkeletonHead(0, 0, 64, 32);

    public Glasses()
    {
        if (FMLCommonHandler.instance().getSide().isClient()) MinecraftForge.EVENT_BUS.register(this);
    }

    public static void addFileUrl(String url)
    {
        if (FMLCommonHandler.instance().getSide().isClient()) instance.addFileUrlInternal(url);
    }

    @ForgeSubscribe
    public void renderEvent(RenderPlayerEvent.Specials.Pre event)
    {
        if (users.containsKey(event.entityPlayer.getDisplayName()) && event.entityPlayer.inventory.armorInventory[3] == null)
        {
            if (event.entityPlayer.isInvisible()) return;
            GL11.glPushMatrix();
            float f3 = event.entityPlayer.prevRotationYawHead + (event.entityPlayer.rotationYawHead - event.entityPlayer.prevRotationYawHead) - (event.entityPlayer.prevRenderYawOffset + (event.entityPlayer.renderYawOffset - event.entityPlayer.prevRenderYawOffset) * event.partialRenderTick);
            float f4 = event.entityPlayer.prevRotationPitch + (event.entityPlayer.rotationPitch - event.entityPlayer.prevRotationPitch);

            GL11.glRotatef(f3, 0.0F, .5F, 0.0F);
            GL11.glRotatef(f4, .5F, 0.0F, 0.0F);

            if (event.entityPlayer.isSneaking()) GL11.glTranslated(0, 0.075, -0.075);

            float f5 = 1.1f;
            GL11.glScalef(f5, f5, f5);

            Minecraft.getMinecraft().renderEngine.bindTexture(getResource(event.entityPlayer.getDisplayName()));
            modelskeletonhead.render((Entity) null, 1.0F, 0.0F, 0.0F, 0F, 0.0F, 0.0625F);
            GL11.glPopMatrix();
        }
    }

    private ResourceLocation getResource(String username)
    {
        return resources.get(users.get(username));
    }

    private void addFileUrlInternal(String parTxtUrl)
    {
        try
        {
            URL url = new URL(parTxtUrl);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;

            String username = "";
            String group = "";
            String capeUrl = "";

            while ((line = reader.readLine()) != null)
            {
                if (line.startsWith("#")) continue;

                for (int i = 0; i < line.length(); i++)
                {
                    if (line.charAt(i) == '=')
                    {
                        group = line.substring(0, i);
                        String subLine = line.substring(i + 1);
                        if (subLine.startsWith("http"))
                        {
                            capeUrl = subLine;
                            ResourceLocation r = new ResourceLocation("Glasses/" + group);
                            ThreadDownloadImageData t = makeDownloadThread(r, capeUrl, null, new ImageBufferDownload());

                            resources.put(group, r);
                            downloadThreads.put(group, t);
                        }
                        else
                        {
                            username = subLine.toLowerCase();
                            users.put(username, group);
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static ThreadDownloadImageData makeDownloadThread(ResourceLocation par0ResourceLocation, String par1Str, ResourceLocation par2ResourceLocation, IImageBuffer par3IImageBuffer)
    {
        TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
        ThreadDownloadImageData threadDownloadImageData = new ThreadDownloadImageData(par1Str, par2ResourceLocation, par3IImageBuffer);
        texturemanager.loadTexture(par0ResourceLocation, threadDownloadImageData);
        return threadDownloadImageData;
    }

    @SideOnly(Side.CLIENT)
    public static class ImageBufferDownload implements IImageBuffer
    {

        private int imageWidth;
        private int imageHeight;

        @Override
        public BufferedImage parseUserSkin(BufferedImage par1BufferedImage)
        {
            if (par1BufferedImage == null)
            {
                return null;
            }
            else
            {
                this.imageWidth = (par1BufferedImage.getWidth((ImageObserver) null) <= 64) ? 64 : (par1BufferedImage.getWidth((ImageObserver) null));
                this.imageHeight = (par1BufferedImage.getHeight((ImageObserver) null) <= 32) ? 32 : (par1BufferedImage.getHeight((ImageObserver) null));

                BufferedImage capeImage = new BufferedImage(this.imageWidth, this.imageHeight, 2);

                Graphics graphics = capeImage.getGraphics();
                graphics.drawImage(par1BufferedImage, 0, 0, (ImageObserver) null);
                graphics.dispose();

                return capeImage;
            }
        }
    }
}
