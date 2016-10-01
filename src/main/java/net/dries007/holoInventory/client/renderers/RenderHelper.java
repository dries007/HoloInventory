package net.dries007.holoInventory.client.renderers;

import net.dries007.holoInventory.client.ClientEventHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

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
        GlStateManager.blendFunc(770, 771);
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

        String size = stack.stackSize < 1000 ? String.valueOf(stack.stackSize) : ClientEventHandler.DF.format(stack.stackSize / 1000.0);
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
