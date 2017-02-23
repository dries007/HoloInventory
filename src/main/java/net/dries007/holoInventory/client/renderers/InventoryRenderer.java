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

package net.dries007.holoInventory.client.renderers;

import net.dries007.holoInventory.client.ClientEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class InventoryRenderer implements IRenderer
{
    private final String name;
    private final List<ItemStack> stacks;

    public InventoryRenderer(final String name, List<ItemStack> input)
    {
        // Minecraft & Localization. One giant clusterfuck.
        String tmp = I18n.format(name);
        if (!tmp.equals(name)) this.name = tmp;
        else
        {
            String name2 = name + ".name";
            tmp = I18n.format(name2);
            if (!tmp.equals(name2)) this.name = tmp;
            else this.name = name;
        }

        this.stacks = new ArrayList<>(input.size());
        for (ItemStack stack : input)
        outer: {
            if (stack == null) continue;
            for (ItemStack stack2 : stacks)
            {
                if (!ItemStack.areItemStackTagsEqual(stack, stack2) || !ItemStack.areItemsEqual(stack, stack2)) continue;
                stack2.grow(stack.getCount());
                break outer;
            }
            this.stacks.add(stack);
        }
    }

    @Override
    public void render(WorldClient world, RayTraceResult hit, Vec3d pos)
    {
        Minecraft mc = Minecraft.getMinecraft();
        RenderManager rm = mc.getRenderManager();
        RenderItem ri = mc.getRenderItem();

        GlStateManager.translate(pos.xCoord - TileEntityRendererDispatcher.staticPlayerX, pos.yCoord - TileEntityRendererDispatcher.staticPlayerY, pos.zCoord - TileEntityRendererDispatcher.staticPlayerZ);

        GlStateManager.rotate(-rm.playerViewY, 0.0F, 0.5F, 0.0F);
        GlStateManager.rotate(rm.playerViewX, 0.5F, 0.0F, 0.0F);
        GlStateManager.translate(0, 0, -0.5);

        double d = pos.distanceTo(new Vec3d(TileEntityRendererDispatcher.staticPlayerX, TileEntityRendererDispatcher.staticPlayerY, TileEntityRendererDispatcher.staticPlayerZ));

        if (d < 1.75) return;
        GlStateManager.scale(d * 0.2, d * 0.2, d * 0.2);

        int cols;
        if (stacks.size() <= 9) cols = stacks.size();
        else if (stacks.size() <= 27) cols = 9;
        else if (stacks.size() <= 54) cols = 11;
        else if (stacks.size() <= 90) cols = 14;
        else if (stacks.size() <= 109) cols = 18;
        else cols = 21;
        int rows = 1 + ((stacks.size() % cols == 0) ? (stacks.size() / cols) - 1 : stacks.size() / cols);

        if (rows > 4) GlStateManager.scale(0.8, 0.8, 0.8);

        // Draw name, with depth disabled
        {
            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();
            GlStateManager.rotate(180, 0, 0, 1);
            GlStateManager.translate(0, -0.6f -0.4f * (rows/2.0), 0);
            //GlStateManager.translate(0, -1 - 0.5f * (rows/2.0), 0);
            GlStateManager.scale(0.03, 0.03, 0.03);
            int w = mc.fontRenderer.getStringWidth(name);
            GlStateManager.disableDepth();
            mc.fontRenderer.drawString(name,  -w/2, 0, 0xFFFFFF);
            GlStateManager.enableDepth();
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }

        int r = 0;
        int c = 0;
        for (final ItemStack stack : stacks)
        {
            RenderHelper.renderStack(ri, stack, cols, c, rows, r);
            if (++c == cols)
            {
                r++;
                c = 0;
            }
        }
        // Draw stack sizes later, to draw over the items (disableDepth)
        r = 0;
        c = 0;
        GlStateManager.disableDepth();
        for (final ItemStack stack : stacks)
        {
            RenderHelper.renderName(mc.fontRenderer, stack, cols, c, rows, r, ClientEventHandler.TEXT_COLOR);
            if (++c == cols)
            {
                r++;
                c = 0;
            }
        }
        GlStateManager.enableDepth();
    }

    @Override
    public boolean shouldRender()
    {
        return stacks.size() != 0;
    }
}
