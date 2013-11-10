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

import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.util.Coord;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.ForgeSubscribe;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.HashMap;

public class Renderer
{
    @ForgeSubscribe
    public void renderEvent(RenderWorldLastEvent event)
    {
        renderHologram();
    }

    public static final HashMap<Coord, ItemStack[]> temp = new HashMap<>();

    public void renderHologram()
    {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE)
        {
            Coord coord = new Coord(mc.theWorld.provider.dimensionId, mc.objectMouseOver);
            if (temp.containsKey(coord))
            {
                // Move to right position and rotate to face the player
                ItemStack[] itemStacks = temp.get(coord);
                if (itemStacks.length == 0) return;
                double distance = distance(coord);
                if (distance < 2) return;
                GL11.glPushMatrix();
                GL11.glTranslated(coord.x + 0.5 - RenderManager.renderPosX, coord.y + 0.5 - RenderManager.renderPosY, coord.z + 0.5 - RenderManager.renderPosZ);
                GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 0.5F, 0.0F);
                GL11.glRotatef(RenderManager.instance.playerViewX, 0.5F, 0.0F, 0.0F);
                GL11.glTranslated(0, 0, -1);

                // Calculate angle based on time (so items rotate)
                float timeD = (float) (360.0 * (double) (System.currentTimeMillis() & 0x3FFFL) / (double) 0x3FFFL);
                EntityItem customitem = new EntityItem(mc.theWorld);
                customitem.hoverStart = 0f;

                int maxCollums = 9;
                int maxRows = itemStacks.length / 9;
                if (itemStacks.length % 9 == 0) maxRows--;
                float blockScale = 0.2f + (float) (0.1f * distance);
                float maxWith = maxCollums * blockScale * 0.7f * 0.4f;
                float maxHeight = maxRows * blockScale * 0.7f * 0.4f;

                // Render the BG
                renderBG(blockScale, maxWith, maxHeight);

                // Render items
                int collum = 0, row = 0;
                for (ItemStack item : itemStacks)
                {
                    GL11.glPushMatrix();
                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                    GL11.glTranslatef(maxWith - ((collum + 0.2f) * blockScale * 0.6f), maxHeight - ((row + 0.05f) * blockScale * 0.6f), 0f);
                    GL11.glScalef(blockScale, blockScale, blockScale);
                    GL11.glRotatef(timeD, 0.0F, 1.0F, 0.0F);
                    customitem.setEntityItemStack(item);
                    HoloInventory.instance.clientHandler.itemRenderer.doRenderItem(customitem, 0, 0, 0, 0, 0);
                    GL11.glPopMatrix();
                    collum++;
                    if (collum >= 9)
                    {
                        collum = 0;
                        row++;
                    }
                }

                // Render stacksizes
                collum = 0;
                row = 0;
                for (ItemStack item : itemStacks)
                {
                    GL11.glPushMatrix();
                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                    GL11.glTranslatef(maxWith - ((collum + 0.2f) * blockScale * 0.6f), maxHeight - ((row + 0.05f) * blockScale * 0.6f), 0f);
                    GL11.glScalef(blockScale, blockScale, blockScale);
                    GL11.glScalef(0.03f, 0.03f, 0.03f);
                    GL11.glRotatef(180, 0.0F, 0.0F, 1.0F);
                    GL11.glTranslatef(-1f, 1f, 0f);
                    if (item.stackSize < 10) GL11.glTranslatef(6f, 0f, 0f);
                    if (item.stackSize > 99) GL11.glTranslatef(-6f, 0f, 0f);
                    if (item.stackSize > 999) GL11.glTranslatef(6f, 0f, 0f);
                    if (item.stackSize > 9999) GL11.glTranslatef(-6f, 0f, 0f);
                    RenderManager.instance.getFontRenderer().drawString(item.stackSize > 999 ? item.stackSize / 1000 + "K" : item.stackSize + "",
                            0,
                            0,
                            255 + (255 << 8) + (255 << 16) + (170 << 24),
                            true);
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glPopMatrix();
                    collum++;
                    if (collum >= 9)
                    {
                        collum = 0;
                        row++;
                    }
                }
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glPopMatrix();
            }
        }
    }

    public void renderBG(float blockScale, float maxWith, float maxHeight)
    {
        if (!HoloInventory.instance.config.colorEnable) return;

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        Tessellator tess = Tessellator.instance;
        Tessellator.renderingWorldRenderer = false;
        tess.startDrawing(GL11.GL_QUADS);
        tess.setColorRGBA(HoloInventory.instance.config.colorR,
                HoloInventory.instance.config.colorG,
                HoloInventory.instance.config.colorB,
                HoloInventory.instance.config.colorAlpha);
        double d = blockScale / 3;
        tess.addVertex(maxWith + d, -d - maxHeight, 0);
        tess.addVertex(-maxWith - d, -d - maxHeight, 0);
        tess.addVertex(-maxWith - d, d + maxHeight, 0);
        tess.addVertex(maxWith + d, d + maxHeight, 0);
        tess.draw();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    public double distance(Coord coord)
    {
        return Math.sqrt((coord.x + 0.5 - RenderManager.renderPosX) * (coord.x + 0.5 - RenderManager.renderPosX) +
                (coord.y + 0.5 - RenderManager.renderPosY) * (coord.y + 0.5 - RenderManager.renderPosY) +
                (coord.z + 0.5 - RenderManager.renderPosZ) * (coord.z + 0.5 - RenderManager.renderPosZ));
    }

    public void read(NBTTagCompound tag)
    {
        Coord coord = new Coord(tag.getCompoundTag("coord"));
        NBTTagList list = tag.getTagList("list");
        ItemStack[] itemStacks = new ItemStack[list.tagCount()];
        for (int i = 0; i < list.tagCount(); i++)
        {
            itemStacks[i] = ItemStack.loadItemStackFromNBT((NBTTagCompound) list.tagAt(i));
        }
        temp.put(coord, itemStacks);
    }
}
