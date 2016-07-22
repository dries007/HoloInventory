package net.dries007.holoInventory.client.renderers;

import net.dries007.holoInventory.client.ClientEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import java.util.List;

public class MerchantRenderer implements IRenderer
{
    private final String name;
    private final List<MerchantRecipe> recipes;

    public MerchantRenderer(String name, MerchantRecipeList input)
    {
        this.name = I18n.format(name);
        this.recipes = input;
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

        // Draw name, with depth disabled
        {
            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();
            GlStateManager.rotate(180, 0, 0, 1);
            GlStateManager.translate(0, -0.6f -0.4f * (recipes.size()/2.0), 0);
            //GlStateManager.translate(0, -1 - 0.5f * (rows/2.0), 0);
            GlStateManager.scale(0.03, 0.03, 0.03);
            int w = mc.fontRendererObj.getStringWidth(name);
            GlStateManager.disableDepth();
            mc.fontRendererObj.drawString(name,  -w/2, 0, 0xFFFFFF);
            GlStateManager.enableDepth();
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }

        for (int row = 0; row < recipes.size(); row ++)
        {
            final MerchantRecipe recipe = recipes.get(row);

            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();

            RenderHelper.renderStack(ri, recipe.getItemToBuy(), 4, 0, recipes.size(), row);
            if (recipe.hasSecondItemToBuy())
                RenderHelper.renderStack(ri, recipe.getSecondItemToBuy(), 4, 1, recipes.size(), row);
            RenderHelper.renderStack(ri, recipe.getItemToSell(), 4, 3, recipes.size(), row);

            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }
        // Draw stack sizes later, to draw over the items (disableDepth)
        GlStateManager.disableDepth();
        for (int row = 0; row < recipes.size(); row ++)
        {
            final MerchantRecipe recipe = recipes.get(row);

            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();

            int color = recipe.isRecipeDisabled() ? ClientEventHandler.TEXT_COLOR_LIGHT : ClientEventHandler.TEXT_COLOR;

            RenderHelper.renderName(mc.fontRendererObj, recipe.getItemToBuy(), 4, 0, recipes.size(), row, color);
            if (recipe.hasSecondItemToBuy())
                RenderHelper.renderName(mc.fontRendererObj, recipe.getSecondItemToBuy(), 4, 1, recipes.size(), row, color);
            RenderHelper.renderName(mc.fontRendererObj, recipe.getItemToSell(), 4, 3, recipes.size(), row, color);

            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }
        GlStateManager.enableDepth();
    }

    @Override
    public boolean shouldRender()
    {
        return recipes.size() != 0;
    }

}
