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

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.network.EntityRequestMessage;
import net.dries007.holoInventory.util.Coord;
import net.dries007.holoInventory.util.Helper;
import net.dries007.holoInventory.util.NamedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL12;

import java.text.DecimalFormat;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;

public class Renderer
{
    private static final DecimalFormat DF = new DecimalFormat("#.#");
    private static final int TEXTCOLOR = 255 + (255 << 8) + (255 << 16) + (170 << 24);
    public static final HashMap<Integer, NamedData<ItemStack[]>> tileMap = new HashMap<Integer, NamedData<ItemStack[]>>();
    public static final HashMap<Integer, NamedData<ItemStack[]>> entityMap = new HashMap<Integer, NamedData<ItemStack[]>>();
    public static final HashMap<Integer, NamedData<MerchantRecipeList>> merchantMap = new HashMap<Integer, NamedData<MerchantRecipeList>>();
    public static final HashMap<Integer, Long> requestMap = new HashMap<Integer, Long>();

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

    @SubscribeEvent
    public void renderEvent(RenderWorldLastEvent event)
    {
        try
        {
            doEvent();
        }
        catch (Exception e)
        {
            HoloInventory.getLogger().warn("Some error while rendering the hologram :(");
            HoloInventory.getLogger().warn("Please make an issue on github if this happens.");

            e.printStackTrace();
        }
    }

    private void doEvent()
    {
        if (!enabled) return;
        Minecraft mc = Minecraft.getMinecraft();
        //if (HoloInventory.getConfig().keyMode == 2 && !KeyManager.key.getIsKeyPressed()) return;
        //if (HoloInventory.getConfig().keyMode == 3 && KeyManager.key.getIsKeyPressed()) return;
        if (mc.renderEngine == null || RenderManager.instance == null || RenderManager.instance.getFontRenderer() == null || mc.gameSettings.thirdPersonView != 0 || mc.objectMouseOver == null) return;
        coord = new Coord(mc.theWorld.provider.dimensionId, mc.objectMouseOver);
        switch (mc.objectMouseOver.typeOfHit)
        {
            case BLOCK:
                // Remove if there is no longer a TE there
                TileEntity te = mc.theWorld.getTileEntity((int) coord.x, (int) coord.y, (int) coord.z);
                if (Helper.weWant(te))
                {
                    String clazz = te.getClass().getCanonicalName();
                    // Check for local ban
                    if (HoloInventory.getConfig().bannedTiles.contains(clazz)) return;
                    NamedData<ItemStack[]> data = tileMap.get(coord.hashCode());
                    if (data != null)
                    {
                        if (data.clazz == null || data.clazz.equals(clazz))
                        {
                            // Render if we know the content
                            coord.x += 0.5;
                            coord.y += 0.5;
                            coord.z += 0.5;
                            renderHologram(data);
                        }
                        else
                            tileMap.remove(coord.hashCode());
                    }
                }
                else
                    tileMap.remove(coord.hashCode());
                break;
            case ENTITY:
                if (!HoloInventory.getConfig().enableEntities) break;
                Entity entity = mc.objectMouseOver.entityHit;
                if (Helper.weWant(entity))
                {
                    // Check for local ban
                    if (HoloInventory.getConfig().bannedEntities.contains(entity.getClass().getCanonicalName())) return;

                    int id = entity.getEntityId();
                    // Make & store request
                    if (!requestMap.containsKey(id))
                    {
                        HoloInventory.getSnw().sendToServer(new EntityRequestMessage(mc.theWorld.provider.dimensionId, id));
                        requestMap.put(id, mc.theWorld.getTotalWorldTime());
                    }
                    // Remove old request so that we get updated info next render tick
                    else if (mc.theWorld.getTotalWorldTime() > requestMap.get(id) + 20 * HoloInventory.getConfig().syncFreq)
                    {
                        requestMap.remove(id);
                    }

                    // Render appropriate hologram
                    if (entity instanceof IInventory && entityMap.containsKey(id)) renderHologram(entityMap.get(id));
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

        glPushMatrix();
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

        glPopMatrix();
    }

    /**
     * Render a regular hologram
     * Does stacking first if user wants it
     *
     * @param namedData Array of items in the inventory
     */
    private void renderHologram(NamedData<ItemStack[]> namedData)
    {
        if (namedData.data == null || namedData.name == null || namedData.data.length == 0) return;
        final double distance = distance();
        if (distance < 1.5) return;

        List<ItemStack> list;

        if (HoloInventory.getConfig().enableStacking)
        {
            list = new ArrayList<ItemStack>();
            // Stack same items together
            for (ItemStack stackToAdd : Arrays.asList(namedData.data))
            {
                boolean f = false;
                for (ItemStack stackInList : list)
                {
                    if (stackInList == null) continue;
                    if (stackToAdd.isItemEqual(stackInList) && ItemStack.areItemStackTagsEqual(stackToAdd, stackInList))
                    {
                        stackInList.stackSize += stackToAdd.stackSize;
                        f = true;
                        break;
                    }
                }
                if (!f) list.add(stackToAdd.copy());
            }
        }
        else
        {
            list = Arrays.asList(namedData.data);
        }

        int wantedSize = list.size();

        switch (HoloInventory.getConfig().mode)
        {
            // Most abundant, 1 item
            case 2:
                wantedSize = 1;
                break;
            // Most abundant, 3 items
            case 3:
                wantedSize = 3;
                break;
            // Most abundant, 5 items
            case 4:
                wantedSize = 5;
                break;
            // Most abundant, 7 items
            case 5:
                wantedSize = 7;
                break;
            // Most abundant, 9 items
            case 6:
                wantedSize = 9;
                break;
        }

        if (HoloInventory.getConfig().mode != 0)
        {
            Collections.sort(list, new Comparator<ItemStack>()
            {
                @Override
                public int compare(ItemStack o1, ItemStack o2)
                {
                    return o2.stackSize - o1.stackSize;
                }
            });
            if (list.size() > wantedSize) list = list.subList(0, wantedSize);
        }

        if (HoloInventory.getConfig().cycle != 0)
        {
            int i = (int) ((Minecraft.getMinecraft().theWorld.getTotalWorldTime() / HoloInventory.getConfig().cycle) % list.size());
            list = Arrays.asList(list.get(i));
        }

        doRenderHologram(namedData.name, list, distance);
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
        glPushMatrix();
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
        glPopMatrix();
    }

    /**
     * Move GL to render in the right spot
     *
     * @param depth Shift towards the player if negative
     */
    private void moveAndRotate(double depth)
    {
        glTranslated(coord.x - RenderManager.renderPosX, coord.y - RenderManager.renderPosY, coord.z - RenderManager.renderPosZ);
        glRotatef(-RenderManager.instance.playerViewY, 0.0F, 0.5F, 0.0F);
        glRotatef(RenderManager.instance.playerViewX, 0.5F, 0.0F, 0.0F);
        glTranslated(0, 0, depth);
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

        if (string.contains(",")) glTranslatef(3f, 0f, 0f);

        switch (string.length())
        {
            case 0:
                return string;
            case 1:
                glTranslatef(3f, 0f, 0f);
                return string;
            default:
                glTranslatef(6f, 0f, 0f);
                glTranslatef(6f * (1 - string.length()), 0f, 0f);
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
        RenderHelper.enableStandardItemLighting();
        glPushMatrix();
        glTranslatef(maxWith - ((column + 0.2f) * blockScale * 0.6f), maxHeight - ((row + 0.05f) * blockScale * 0.6f), 0f);
        glScalef(blockScale, blockScale, blockScale);
        if (Minecraft.getMinecraft().gameSettings.fancyGraphics) glRotatef(HoloInventory.getConfig().rotateItems ? timeD : 0f, 0.0F, 1.0F, 0.0F);
        else glRotatef(RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        customitem.setEntityItemStack(itemStack);
        ClientHandler.RENDER_ITEM.doRender(customitem, 0, 0, 0, 0, 0);
        if (itemStack.hasEffect(0)) glDisable(GL_LIGHTING);
        glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        if (renderText && !(itemStack.getMaxStackSize() == 1 && itemStack.stackSize == 1))
        {
            glPushMatrix();
            glDisable(GL_DEPTH_TEST);
            glTranslatef(maxWith - ((column + 0.2f) * blockScale * 0.6f), maxHeight - ((row + 0.05f) * blockScale * 0.6f), 0f);
            glScalef(blockScale, blockScale, blockScale);
            glScalef(0.03f, 0.03f, 0.03f);
            glRotatef(180, 0.0F, 0.0F, 1.0F);
            glTranslatef(-1f, 1f, 0f);
            RenderManager.instance.getFontRenderer().drawString(doStackSizeCrap(stackSize), 0, 0, TEXTCOLOR, true);
            glDisable(GL12.GL_RESCALE_NORMAL);
            glEnable(GL_DEPTH_TEST);
            glPopMatrix();
        }
    }

    private void renderBG()
    {
        glPushMatrix();
        glEnable(GL12.GL_RESCALE_NORMAL);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        Tessellator tess = Tessellator.instance;
        Tessellator.renderingWorldRenderer = false;
        tess.startDrawing(GL_QUADS);
        tess.setColorRGBA(HoloInventory.getConfig().colorR, HoloInventory.getConfig().colorG, HoloInventory.getConfig().colorB, HoloInventory.getConfig().colorAlpha);
        double d = blockScale / 3;
        tess.addVertex(maxWith + d, -d - maxHeight, 0);
        tess.addVertex(-maxWith - d, -d - maxHeight, 0);
        tess.addVertex(-maxWith - d, d + maxHeight, 0);
        tess.addVertex(maxWith + d, d + maxHeight, 0);
        tess.draw();
        glDisable(GL_BLEND);
        glDisable(GL12.GL_RESCALE_NORMAL);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
    }

    private void renderName(String name)
    {
        if (HoloInventory.getConfig().nameOverrides.containsKey(name)) name = HoloInventory.getConfig().nameOverrides.get(name);
        else name = StatCollector.translateToLocal(name);
        glPushMatrix();
        glEnable(GL12.GL_RESCALE_NORMAL);
        glDisable(GL_DEPTH_TEST);

        glTranslated(0f, maxHeight + blockScale / 1.25, 0f);

        glScaled(blockScale, blockScale, blockScale);
        glScalef(1.5f, 1.5f, 1.5f);
        glScalef(0.03f, 0.03f, 0.03f);
        glTranslated(3f * name.length(), 0f, 0f);
        glRotatef(180, 0.0F, 0.0F, 1.0F);
        glTranslatef(-1f, 1f, 0f);
        RenderManager.instance.getFontRenderer().drawString(name, 0, 0, TEXTCOLOR, true);

        glDisable(GL12.GL_RESCALE_NORMAL);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
        glPopMatrix();
    }

    private double distance()
    {
        return Math.sqrt((coord.x - RenderManager.renderPosX) * (coord.x - RenderManager.renderPosX) +
                (coord.y - RenderManager.renderPosY) * (coord.y - RenderManager.renderPosY) +
                (coord.z - RenderManager.renderPosZ) * (coord.z - RenderManager.renderPosZ));
    }
}
