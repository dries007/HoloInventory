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
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.event.ForgeSubscribe;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import static org.lwjgl.opengl.GL11.*;

import java.util.HashMap;

public class Renderer
{
    public              HashMap<Coord, ItemStack[]> temp  = new HashMap<Coord, ItemStack[]>();
    public static final ResourceLocation            slot = new ResourceLocation("holoinventory:textures/slot.png");

    @ForgeSubscribe
    public void renderEvent(RenderWorldLastEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE)
        {
            Coord coord = new Coord(mc.theWorld.provider.dimensionId, mc.objectMouseOver);
            if (temp.containsKey(coord))
            {
                double distance = distance(coord);
                if (distance < 1.7) return;
                GL11.glPushMatrix();
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glTranslated(coord.x + 0.5 - RenderManager.renderPosX,
                        coord.y + 0.5 - RenderManager.renderPosY,
                        coord.z + 0.5 - RenderManager.renderPosZ);
                GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
                GL11.glTranslated(0, 0, -1);

                float timeD = (float) (360.0 * (double) (System.currentTimeMillis() & 0x3FFFL) / (double) 0x3FFFL);
                EntityItem customitem = new EntityItem(mc.theWorld);
                customitem.hoverStart = 0f;
                ItemStack[] itemStacks = temp.get(coord);
                int maxCollums = itemStacks.length >= 9 ? 9 : itemStacks.length;
                int maxRows = itemStacks.length / 9;
                float blockScale = (float) (0.6f * distance * (1.3f / maxCollums));
                float maxWith = maxCollums * blockScale * 0.7f * 0.4f;
                float maxHeight = maxRows * blockScale * 0.7f * 0.3f;

                int collum = 0, row = 0;
                for (ItemStack item : itemStacks)
                {
                    GL11.glPushMatrix();

                    renderbox(blockScale, maxWith, collum, maxHeight, row);

                    GL11.glRotatef(timeD, 0.0F, 1.0F, 0.0F);
                    GL11.glScalef(blockScale, blockScale, blockScale);
                    customitem.setEntityItemStack(item);
                    HoloInventory.instance.clientHandler.itemRenderer.doRenderItem(customitem, 0, 0, 0, 0, 0);
                    GL11.glPopMatrix();
                    collum ++;
                    if (collum >= 9)
                    {
                        collum = 0;
                        row ++;
                    }
                }
                while (collum != 0)
                {
                    GL11.glPushMatrix();
                    renderbox(blockScale, maxWith, collum, maxHeight, row);
                    GL11.glPopMatrix();
                    collum ++;
                    if (collum == 9) collum = 0;
                }
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glPopMatrix();
            }
        }
    }

    public void renderbox(float blockScale, float maxWith, int collum, float maxHeight, int row)
    {
        GL11.glTranslatef(maxWith - (collum * blockScale * 0.6f),maxHeight - (row * blockScale * 0.6f),0f);

        Tessellator tess = Tessellator.instance;
        Minecraft.getMinecraft().getTextureManager().bindTexture(slot);
        Tessellator.renderingWorldRenderer = false;
        tess.startDrawing(GL11.GL_QUADS);
        double d = blockScale/3;
        tess.addVertex(-d, d, 0);
        tess.addVertex(d, d, 0);
        tess.addVertex(d, -d, 0);
        tess.addVertex(-d, -d, 0);
        tess.draw();
    }

    public double distance(Coord coord)
    {
        return Math.sqrt(   (coord.x + 0.5 - RenderManager.renderPosX) * (coord.x + 0.5 - RenderManager.renderPosX)+
                            (coord.y + 0.5 - RenderManager.renderPosY) * (coord.y + 0.5 - RenderManager.renderPosY)+
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
