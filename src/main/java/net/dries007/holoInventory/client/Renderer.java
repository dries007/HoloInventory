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

import cpw.mods.fml.common.registry.LanguageRegistry;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.util.Coord;
import net.dries007.holoInventory.util.Helper;
import net.dries007.holoInventory.util.NamedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.ForgeSubscribe;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Renderer
{
    private static final DecimalFormat                                      DF          = new DecimalFormat("#.#");
    private static final int                                                TEXTCOLOR   = 255 + (255 << 8) + (255 << 16) + (170 << 24);
    public static final  HashMap<Integer, NamedData<ItemStack[]>>           tileMap     = new HashMap<>();
    public static final  HashMap<Integer, NamedData<ItemStack[]>>           entityMap   = new HashMap<>();
    public static final  HashMap<Integer, NamedData<MerchantRecipeList>>    merchantMap = new HashMap<>();
    public static final  HashMap<Integer, Long>                             requestMap  = new HashMap<>();

    private EntityItem customitem = new EntityItem(Minecraft.getMinecraft().theWorld);
    private Coord coord;
    public boolean enabled = true;

    public static final Renderer INSTANCE = new Renderer();
    private float timeD, blockScale, maxWith, maxHeight;
    private int maxColumns, maxRows;
    private boolean renderText;

    private Renderer()
    {
        customitem.hoverStart = 0f;
    }

    @ForgeSubscribe
    public void renderEvent(RenderWorldLastEvent event)
    {
        if (!enabled) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.renderEngine == null || RenderManager.instance == null || RenderManager.instance.getFontRenderer() == null || mc.gameSettings.thirdPersonView != 0 || mc.objectMouseOver == null)
            return;
        coord = new Coord(mc.theWorld.provider.dimensionId, mc.objectMouseOver);
        switch (mc.objectMouseOver.typeOfHit)
        {
            case TILE:
                // Remove if there is no longer a TE there
                if (mc.theWorld.getBlockTileEntity((int) coord.x, (int) coord.y, (int) coord.z) == null) tileMap.remove(coord.hashCode());
                // Render if we know the content
                if (tileMap.containsKey(coord.hashCode()))
                {
                    // Check for local ban
                    if (HoloInventory.getConfig().bannedTiles.contains(mc.theWorld.getBlockTileEntity((int) coord.x,
                            (int) coord.y,
                            (int) coord.z).getClass().getCanonicalName())) return;
                    int i = coord.hashCode();
                    coord.x += 0.5;
                    coord.y += 0.5;
                    coord.z += 0.5;
                    renderHologram(tileMap.get(i));

                }
                break;
            case ENTITY:
                Entity entity = mc.objectMouseOver.entityHit;
                if (entity instanceof IMerchant || entity instanceof IInventory)
                {
                    // Check for local ban
                    if (HoloInventory.getConfig().bannedEntities.contains(entity.getClass().getCanonicalName())) return;

                    int id = entity.entityId;
                    // Make & store request
                    if (!requestMap.containsKey(id))
                    {
                        Helper.request(mc.theWorld.provider.dimensionId, id);
                        requestMap.put(id, mc.theWorld.getTotalWorldTime());
                    }
                    // Remove old request so that we get updated info next render tick
                    else if (mc.theWorld.getTotalWorldTime() > requestMap.get(id) + 20 * HoloInventory.getConfig().syncFreq)
                    {
                        requestMap.remove(id);
                    }

                    // Render appropriate hologram
                    if (entity instanceof IInventory && entityMap.containsKey(id))
                    {
                        renderHologram(entityMap.get(id));
                    }
                    if (entity instanceof IMerchant && merchantMap.containsKey(id)) renderMerchant(merchantMap.get(id));
                }
                break;
        }
    }

    /**
     * Render a villagers hologram
     *
     * @param namedData The things to render
     */
    private void renderMerchant(NamedData<MerchantRecipeList> namedData)
    {
        coord.y += 2; //Adjust for villager height
        if (namedData.data.size() == 0) return;
        final double distance = distance();
        if (distance < 1) return;

        GL11.glPushMatrix();
        moveAndRotate(-0.25);

        // Values for later
        timeD = (float) (360.0 * (double) (System.currentTimeMillis() & 0x3FFFL) / (double) 0x3FFFL);
        maxColumns = 3;
        maxRows = namedData.data.size();
        blockScale = getBlockScaleModifier(maxColumns) + (float) (0.1f * distance);
        maxWith = maxColumns * blockScale * 0.7f * 0.4f;
        maxHeight = maxRows * blockScale * 0.7f * 0.4f;
        renderText = true;

        // Render the BG
        if (HoloInventory.getConfig().colorEnable) renderBG();

        // Render the inv name
        if (HoloInventory.getConfig().renderName) renderName(namedData.name);

        for (int row = 0; row < namedData.data.size(); row++)
        {
            MerchantRecipe recipe = (MerchantRecipe) namedData.data.get(row);

            renderItem(recipe.getItemToBuy(), 0, row, recipe.getItemToBuy().stackSize);
            if (recipe.hasSecondItemToBuy()) renderItem(recipe.getSecondItemToBuy(), 1, row, recipe.getSecondItemToBuy().stackSize);
            renderItem(recipe.getItemToSell(), 2, row, recipe.getItemToSell().stackSize);
        }

        GL11.glPopMatrix();
    }

    /**
     * Render a regular hologram
     * Does stacking first if user wants it
     *
     * @param namedData Array of items in the inventory
     */
    private void renderHologram(NamedData<ItemStack[]> namedData)
    {
        if (namedData.data.length == 0) return;
        final double distance = distance();
        if (distance < 1.5) return;

        if (HoloInventory.getConfig().enableStacking)
        {
            // Stack same items together
            ArrayList<ItemStack> list = new ArrayList<>();
            for (ItemStack stackToAdd : Arrays.asList(namedData.data))
            {
                boolean f = false;
                for (ItemStack stackInList : list)
                {
                    if (stackToAdd.isItemEqual(stackInList) && ItemStack.areItemStackTagsEqual(stackToAdd, stackInList))
                    {
                        stackInList.stackSize += stackToAdd.stackSize;
                        f = true;
                        break;
                    }
                }
                if (!f) list.add(stackToAdd.copy());
            }

            doRenderHologram(namedData.name, list, distance);
        }
        else
        {
            doRenderHologram(namedData.name, Arrays.asList(namedData.data), distance);
        }
    }

    /**
     * Actually renders a regular hologram
     *
     * @param itemStacks The itemStacks that will be rendered
     * @param distance   The distance the player is from the hologram, passed to avoid 2th calculation.
     */
    private void doRenderHologram(String name, List<ItemStack> itemStacks, double distance)
    {
        // Move to right position and rotate to face the player
        GL11.glPushMatrix();
        moveAndRotate(-1);

        // Values for later
        timeD = (float) (360.0 * (double) (System.currentTimeMillis() & 0x3FFFL) / (double) 0x3FFFL);
        maxColumns = getMaxColumns(itemStacks.size());
        maxRows = (itemStacks.size() % maxColumns == 0) ? (itemStacks.size() / maxColumns) - 1 : itemStacks.size() / maxColumns;
        blockScale = getBlockScaleModifier(maxColumns) + (float) (0.05f * distance);
        maxWith = maxColumns * blockScale * 0.7f * 0.4f;
        maxHeight = maxRows * blockScale * 0.7f * 0.4f;
        renderText = HoloInventory.getConfig().renderText;

        // Render the BG
        if (HoloInventory.getConfig().colorEnable) renderBG();

        // Render the inv name
        if (HoloInventory.getConfig().renderName) renderName(name);

        // Render items
        int column = 0, row = 0;
        for (ItemStack item : itemStacks)
        {
            int stackSize = item.stackSize;
            if (!HoloInventory.getConfig().renderMultiple)
            {
                item = item.copy();
                item.stackSize = 1;
            }
            renderItem(item, column, row, stackSize);
            column++;
            if (column >= maxColumns)
            {
                column = 0;
                row++;
            }
        }
        GL11.glPopMatrix();
    }

    /**
     * Move GL to render in the right spot
     *
     * @param depth Shift towards the player if negative
     */
    private void moveAndRotate(double depth)
    {
        GL11.glTranslated(coord.x - RenderManager.renderPosX, coord.y - RenderManager.renderPosY, coord.z - RenderManager.renderPosZ);
        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 0.5F, 0.0F);
        GL11.glRotatef(RenderManager.instance.playerViewX, 0.5F, 0.0F, 0.0F);
        GL11.glTranslated(0, 0, depth);
    }

    /**
     * @param columns amount of columns in the hologram
     * @return the blockScaleModifier
     */
    private float getBlockScaleModifier(int columns)
    {
        if (columns > 9) return 0.2f - columns * 0.005f;
        else return 0.2f + (9 - columns) * 0.05f;
    }

    /**
     * @param size of the inventory
     * @return columns of the hologram
     */
    private int getMaxColumns(int size)
    {
        if (size < 9) return size;
        else if (size <= 27) return 9;
        else if (size <= 54) return 11;
        else if (size <= 90) return 14;
        else if (size <= 109) return 18;
        else return 21;
    }

    /**
     * Shifts GL & returns the string
     *
     * @param stackSize the stackSize.
     * @return the string to be rendered
     */
    private String doStackSizeCrap(int stackSize)
    {
        String string = (stackSize < 1000) ? stackSize + "" : DF.format((double) stackSize / 1000) + "K";

        if (string.contains(",")) GL11.glTranslatef(3f, 0f, 0f);

        switch (string.length())
        {
            case 0:
                return string;
            case 1:
                GL11.glTranslatef(3f, 0f, 0f);
                return string;
            default:
                GL11.glTranslatef(6f, 0f, 0f);
                GL11.glTranslatef(6f * (1 - string.length()), 0f, 0f);
                return string;
        }
    }

    /**
     * Renders 1 item
     *
     * @param itemStack itemStack to render
     * @param column    the column the item needs to be rendered at
     * @param row       the row the item needs to be rendered at
     * @param stackSize the stackSize to use for text
     */
    private void renderItem(ItemStack itemStack, int column, int row, int stackSize)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef(maxWith - ((column + 0.2f) * blockScale * 0.6f), maxHeight - ((row + 0.05f) * blockScale * 0.6f), 0f);
        GL11.glScalef(blockScale, blockScale, blockScale);
        if (Minecraft.getMinecraft().gameSettings.fancyGraphics) GL11.glRotatef(timeD, 0.0F, 1.0F, 0.0F);
        else GL11.glRotatef(RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        customitem.setEntityItemStack(itemStack);
        ClientHandler.RENDER_ITEM.doRenderItem(customitem, 0, 0, 0, 0, 0);
        if (itemStack.hasEffect(0)) GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
        if (renderText && !(itemStack.getMaxStackSize() == 1 && itemStack.stackSize == 1))
        {
            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glTranslatef(maxWith - ((column + 0.2f) * blockScale * 0.6f), maxHeight - ((row + 0.05f) * blockScale * 0.6f), 0f);
            GL11.glScalef(blockScale, blockScale, blockScale);
            GL11.glScalef(0.03f, 0.03f, 0.03f);
            GL11.glRotatef(180, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-1f, 1f, 0f);
            RenderManager.instance.getFontRenderer().drawString(doStackSizeCrap(stackSize), 0, 0, TEXTCOLOR, true);
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glPopMatrix();
        }
    }

    private void renderBG()
    {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        Tessellator tess = Tessellator.instance;
        Tessellator.renderingWorldRenderer = false;
        tess.startDrawing(GL11.GL_QUADS);
        tess.setColorRGBA(HoloInventory.getConfig().colorR,
                HoloInventory.getConfig().colorG,
                HoloInventory.getConfig().colorB,
                HoloInventory.getConfig().colorAlpha);
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

    private void renderName(String name)
    {
        name = StatCollector.translateToLocal(name);
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        GL11.glTranslated(0f, maxHeight + blockScale/1.25, 0f);

        GL11.glScaled(blockScale, blockScale, blockScale);
        if (blockScale < 0.5) GL11.glScalef(1.5f, 1.5f, 1.5f);
        GL11.glScalef(0.03f, 0.03f, 0.03f);
        GL11.glTranslated(3f * name.length(), 0f, 0f);
        GL11.glRotatef(180, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-1f, 1f, 0f);
        RenderManager.instance.getFontRenderer().drawString(name, 0, 0, TEXTCOLOR, true);

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopMatrix();
    }

    private double distance()
    {
        return Math.sqrt((coord.x - RenderManager.renderPosX) * (coord.x - RenderManager.renderPosX) +
                (coord.y - RenderManager.renderPosY) * (coord.y - RenderManager.renderPosY) +
                (coord.z - RenderManager.renderPosZ) * (coord.z - RenderManager.renderPosZ));
    }
}
