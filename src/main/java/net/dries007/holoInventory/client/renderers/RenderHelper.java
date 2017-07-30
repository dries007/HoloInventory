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
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderHelper
{
    public static void start()
    {
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void end()
    {
        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    public static void renderName(FontRenderer fr, ItemStack stack, int cols, int col, int rows, int row, int color)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.4f * (cols / 2.0 - col) - 0.2f, 0.4f * (rows / 2.0 - row) - 0.15f, 0);
        GlStateManager.pushAttrib();
        GlStateManager.rotate(180, 0, 0, 1);
        GlStateManager.translate(0.2, 0, -0.1);
        GlStateManager.scale(0.01, 0.01, 0.01);

        String size;
        if (stack.getCount() < 1000) size = String.valueOf(stack.getCount());
        else if (stack.getCount() > 1000) size = ClientEventHandler.DF.format(stack.getCount() / 1000.0) + "k";
        else size = ClientEventHandler.DF.format(stack.getCount() / (1000.0 * 1000.0)) + "M";

        int w = fr.getStringWidth(size);
        fr.drawStringWithShadow(size, -w, 0, color);

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    public static void renderStack(RenderItem ri, ItemStack stack, int cols, int col, int rows, int row)
    {
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.translate(0.4f * (cols / 2.0 - col) - 0.2f, 0.4f * (rows / 2.0 - row), 0);
        GlStateManager.pushMatrix();
        GlStateManager.rotate((float) (360.0 * (double) (System.currentTimeMillis() & 0x3FFFL) / (double) 0x3FFFL), 0, 1, 0);
        GlStateManager.scale(0.45, 0.45, 0.45);

        ri.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);

        if (stack.hasEffect())
        {
            GlStateManager.disableAlpha();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableLighting();
        }
        GlStateManager.popMatrix();

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }
}
