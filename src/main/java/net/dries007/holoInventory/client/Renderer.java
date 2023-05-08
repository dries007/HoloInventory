/*
 * Copyright (c) 2014. Dries K. Aka Dries007 Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions: The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.dries007.holoInventory.client;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.dries007.holoInventory.Config;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.api.IHoloGlasses;
import net.dries007.holoInventory.items.HoloGlasses;
import net.dries007.holoInventory.network.EntityRequestMessage;
import net.dries007.holoInventory.util.Coord;
import net.dries007.holoInventory.util.Helper;
import net.dries007.holoInventory.util.NamedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import org.lwjgl.opengl.GL11;

import codechicken.nei.ItemList;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.SearchField;
import codechicken.nei.api.ItemFilter;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class Renderer {

    public static final HashMap<Integer, NamedData<ItemStack[]>> tileInventoryMap = new HashMap<>();
    public static final HashMap<Integer, List<FluidTankInfo>> tileFluidHandlerMap = new HashMap<>();
    public static final HashMap<Integer, NamedData<ItemStack[]>> entityMap = new HashMap<>();
    public static final HashMap<Integer, NamedData<MerchantRecipeList>> merchantMap = new HashMap<>();
    public static final HashMap<Integer, Long> requestMap = new HashMap<>();

    private Coord coord;
    public boolean enabled = true;

    // copy of RenderManager#renderPosX and its cousins. we need to calculate these ourselves as they can be broken
    // by optifine
    private static double renderPosX, renderPosY, renderPosZ;

    public static final Renderer INSTANCE = new Renderer();

    private final GroupRenderer itemGroupRenderer = new GroupRenderer();
    private final GroupRenderer fluidGroupRenderer = new GroupRenderer();

    ItemFilter cachedFilter = null;
    String cachedSearch = "";

    private Renderer() {}

    @SubscribeEvent
    public void renderEvent(RenderWorldLastEvent event) {
        if (!enabled) {
            return;
        }
        final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        final World world = Minecraft.getMinecraft().theWorld;
        final ItemStack glasses = HoloGlasses.getHoloGlasses(world, player);
        try {
            if (!Config.requireGlasses || glasses != null && ((IHoloGlasses) glasses.getItem()).shouldRender(glasses)) {
                doEvent(event.partialTicks);
            }
        } catch (Exception e) {
            HoloInventory.getLogger().warn("Some error while rendering the hologram :(");
            HoloInventory.getLogger().warn("Please make an issue on github if this happens");
            e.printStackTrace();
        }
    }

    private void doEvent(float partialTicks) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.renderEngine == null || RenderManager.instance == null
                || RenderManager.instance.getFontRenderer() == null
                || mc.gameSettings.thirdPersonView != 0
                || mc.objectMouseOver == null) {
            return;
        }
        coord = new Coord(mc.theWorld.provider.dimensionId, mc.objectMouseOver);
        itemGroupRenderer.reset();
        fluidGroupRenderer.reset();
        GroupRenderer.updateTime();
        switch (mc.objectMouseOver.typeOfHit) {
            case BLOCK:
                // Remove if there is no longer a TE there
                TileEntity te = mc.theWorld.getTileEntity((int) coord.x, (int) coord.y, (int) coord.z);
                if (Helper.weWant(te) || te instanceof IFluidHandler) {
                    String clazz = te.getClass().getCanonicalName();
                    // Check for local ban
                    if (Config.bannedTiles.contains(clazz)) return;

                    NamedData<ItemStack[]> invData = tileInventoryMap.get(coord.hashCode());
                    if (invData != null) {
                        if (invData.clazz != null && !invData.clazz.equals(clazz)) {
                            // Render only if we know the content
                            invData = null;
                            tileInventoryMap.remove(coord.hashCode());
                        }
                    }

                    List<FluidTankInfo> fluidTankInfos = tileFluidHandlerMap.get(coord.hashCode());
                    if (fluidTankInfos != null && !(te instanceof IFluidHandler)) {
                        // Render only if we know the content
                        fluidTankInfos = null;
                        tileFluidHandlerMap.remove(coord.hashCode());
                    }

                    coord.x += 0.5;
                    coord.y += 0.5;
                    coord.z += 0.5;
                    setRenderPos(partialTicks);
                    renderHologram(invData, fluidTankInfos);
                } else {
                    tileInventoryMap.remove(coord.hashCode());
                }
                break;
            case ENTITY:
                if (!Config.enableEntities) break;
                Entity entity = mc.objectMouseOver.entityHit;
                if (Helper.weWant(entity)) {
                    // Check for local ban
                    if (Config.bannedEntities.contains(entity.getClass().getCanonicalName())) return;

                    int id = entity.getEntityId();
                    // Make & store request
                    if (!requestMap.containsKey(id)) {
                        HoloInventory.getSnw()
                                .sendToServer(new EntityRequestMessage(mc.theWorld.provider.dimensionId, id));
                        requestMap.put(id, mc.theWorld.getTotalWorldTime());
                    }
                    // Remove old request so that we get updated info next render tick
                    else if (mc.theWorld.getTotalWorldTime() > requestMap.get(id) + 20L * Config.syncFreq) {
                        requestMap.remove(id);
                    }

                    // Render appropriate hologram
                    setRenderPos(partialTicks);
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
    private void renderMerchant(NamedData<MerchantRecipeList> namedData) {
        coord.y += 2; // Adjust for villager height
        if (namedData.data.size() == 0) return;
        final double distance = distance();
        if (distance < 1) return;

        GL11.glPushMatrix();
        moveAndRotate(-0.25);

        double uiScaleFactor = Config.renderScaling;
        if (uiScaleFactor < 0.1) uiScaleFactor = 0.1;
        GL11.glScaled(uiScaleFactor, uiScaleFactor, uiScaleFactor);

        itemGroupRenderer.calculateColumns(3);
        itemGroupRenderer.setRows(namedData.data.size() - 1);
        itemGroupRenderer.setScale((float) (0.1f * distance));
        // merchant cannot sell more than 127 items. no need to increase spacing whatsoever
        itemGroupRenderer.setSpacing(0.6f);
        itemGroupRenderer.setRenderText(true);

        // Render the BG
        if (Config.colorEnable) {
            GroupRenderer.renderBG(itemGroupRenderer);
        }

        // Render the inv name
        if (Config.renderName) {
            GroupRenderer.renderName(namedData.name, itemGroupRenderer);
        }

        List<ItemStack> stacks = new ArrayList<>();
        for (int row = 0; row < namedData.data.size(); row++) {
            MerchantRecipe recipe = (MerchantRecipe) namedData.data.get(row);
            stacks.add(recipe.getItemToBuy());
            if (recipe.hasSecondItemToBuy()) {
                stacks.add(recipe.getSecondItemToBuy());
            } else {
                stacks.add(null);
            }
            stacks.add(recipe.getItemToSell());
        }
        itemGroupRenderer.renderItems(stacks);

        GL11.glPopMatrix();
    }

    /**
     * Recreate the item filter from updated NEI search string
     *
     * @param s_filter new search string
     */
    private ItemFilter getFilter(String s_filter) {
        cachedSearch = s_filter;
        List<ItemFilter> primary = new LinkedList<>();
        List<ItemFilter> secondary = new LinkedList<>();
        for (SearchField.ISearchProvider p : SearchField.searchProviders) {
            ItemFilter filter = p.getFilter(s_filter);
            if (filter != null) (p.isPrimary() ? primary : secondary).add(filter);
        }
        if (!primary.isEmpty()) return new ItemList.AnyMultiItemFilter(primary);
        if (!secondary.isEmpty()) return new ItemList.AnyMultiItemFilter(secondary);
        return new ItemList.EverythingItemFilter();
    }

    /**
     * Filter items by NEI search string
     *
     * @param namedData Data containing array of items in the inventory
     */
    private List<ItemStack> filterByNEI(NamedData<ItemStack[]> namedData) {
        if (namedData == null || namedData.isInvalid()) return Collections.emptyList();
        ItemStack[] items = namedData.data;
        try {
            if (Config.hideItemsNotSelected && Loader.isModLoaded("NotEnoughItems")
                    && SearchField.searchInventories()) {
                final String searchString = NEIClientConfig.getSearchExpression().toLowerCase();
                if (!cachedSearch.equals(searchString) || cachedFilter == null) cachedFilter = getFilter(searchString);
                return Arrays.stream(items).filter(s -> cachedFilter.matches(s)).collect(Collectors.toList());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Arrays.asList(items);
    }

    private void renderHologram(NamedData<ItemStack[]> namedData) {
        renderHologram(namedData, null);
    }

    /**
     * Render a regular hologram Does stacking first if user wants it
     *
     * @param namedData      Array of items in the inventory
     * @param fluidTankInfos List of fluids in the tile
     */
    private void renderHologram(@Nullable NamedData<ItemStack[]> namedData,
            @Nullable List<FluidTankInfo> fluidTankInfos) {
        final double distance = distance();
        if (distance < 1.5) return;

        List<ItemStack> list = filterByNEI(namedData);

        int wantedSize = list.size();

        switch (Config.mode) {
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

        if (Config.mode != 0) {
            list.sort(Comparator.<ItemStack>comparingInt(s -> s.stackSize).reversed());
            if (list.size() > wantedSize) list = list.subList(0, wantedSize);
        }

        if (Config.cycle != 0) {
            int i = (int) ((Minecraft.getMinecraft().theWorld.getTotalWorldTime() / Config.cycle) % list.size());
            list = Collections.singletonList(list.get(i));
        }

        doRenderHologram(
                namedData != null ? namedData.name : null,
                list,
                fluidTankInfos != null ? fluidTankInfos : Collections.emptyList(),
                distance);
    }

    /**
     * Actually renders a regular hologram
     *
     * @param itemStacks The itemStacks that will be rendered
     * @param distance   The distance the player is from the hologram, passed to avoid 2th calculation.
     */
    private void doRenderHologram(@Nullable String name, @Nonnull List<ItemStack> itemStacks,
            @Nonnull List<FluidTankInfo> fluidTankInfos, double distance) {
        if (itemStacks.isEmpty() && fluidTankInfos.isEmpty()) return;
        // Move to right position and rotate to face the player
        GL11.glPushMatrix();

        moveAndRotate(-1);

        double uiScaleFactor = Config.renderScaling;
        if (uiScaleFactor < 0.1) uiScaleFactor = 0.1;
        GL11.glScaled(uiScaleFactor, uiScaleFactor, uiScaleFactor);

        preRenderHologramItems(itemStacks, distance);
        preRenderHologramFluids(fluidTankInfos, distance);

        itemGroupRenderer.setOffset(fluidGroupRenderer.calculateOffset());

        // Render the BG
        if (Config.colorEnable) {
            List<GroupRenderer> list = new ArrayList<>();
            if (!itemStacks.isEmpty()) {
                list.add(itemGroupRenderer);
            }
            if (!fluidTankInfos.isEmpty()) {
                list.add(fluidGroupRenderer);
            }
            GroupRenderer.renderBG(list.toArray(new GroupRenderer[0]));
        }

        // Render the inv name
        if (Config.renderName && name != null) {
            GroupRenderer.renderName(name, itemGroupRenderer, fluidGroupRenderer);
        }

        renderHologramItems(itemStacks);
        renderHologramFluids(fluidTankInfos);

        GL11.glPopMatrix();
    }

    private void preRenderHologramItems(List<ItemStack> itemStacks, double distance) {
        if (itemStacks.isEmpty()) return;

        // See if we need to increase spacing
        float stackSpacing = 0.6f;
        if (Config.renderText) {
            for (ItemStack stack : itemStacks) {
                if (stack.stackSize >= 1000 || GroupRenderer.stackSizeDebugOverride >= 1000) {
                    stackSpacing = 0.8f;
                    break;
                }
            }
        }

        itemGroupRenderer.calculateColumns(itemStacks.size());
        itemGroupRenderer.calculateRows(itemStacks.size());
        itemGroupRenderer.setScale((float) (0.05f * distance));
        itemGroupRenderer.setSpacing(stackSpacing);
        itemGroupRenderer.setRenderText(Config.renderText);
    }

    private void renderHologramItems(List<ItemStack> itemStacks) {
        if (itemStacks.isEmpty()) return;

        List<ItemStack> renderStacks = new ArrayList<>();
        for (ItemStack item : itemStacks) {
            if (!Config.renderMultiple) {
                item = item.copy();
                item.stackSize = 1;
            }
            renderStacks.add(item);
        }
        itemGroupRenderer.renderItems(renderStacks);
    }

    private void preRenderHologramFluids(List<FluidTankInfo> fluidTankInfos, double distance) {
        if (fluidTankInfos.isEmpty()) return;

        // See if we need to increase spacing
        float spacing = 0.6f;
        if (Config.renderText) {
            for (FluidTankInfo fluidTankInfo : fluidTankInfos) {
                if (fluidTankInfo.fluid.amount >= 1000) {
                    spacing = 0.8f;
                    break;
                }
            }
        }

        fluidGroupRenderer.calculateColumns(fluidTankInfos.size());
        fluidGroupRenderer.calculateRows(fluidTankInfos.size());
        fluidGroupRenderer.setScale((float) (0.05f * distance));
        fluidGroupRenderer.setSpacing(spacing);
        fluidGroupRenderer.setRenderText(Config.renderText);
    }

    private void renderHologramFluids(List<FluidTankInfo> fluidTankInfos) {
        if (fluidTankInfos.isEmpty()) return;
        fluidGroupRenderer.renderFluids(fluidTankInfos);
    }

    /**
     * Move GL to render in the right spot
     *
     * @param depth Shift towards the player if negative
     */
    private void moveAndRotate(double depth) {
        GL11.glTranslated(coord.x - renderPosX, coord.y - renderPosY, coord.z - renderPosZ);
        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 0.5F, 0.0F);
        GL11.glRotatef(RenderManager.instance.playerViewX, 0.5F, 0.0F, 0.0F);
        GL11.glTranslated(0, 0, depth);
    }

    private static void setRenderPos(float partialTicks) {
        Entity thePlayer = Minecraft.getMinecraft().thePlayer;
        double lastTickPosX = thePlayer.lastTickPosX;
        double posX = thePlayer.posX;
        renderPosX = lastTickPosX + (posX - lastTickPosX) * partialTicks;
        renderPosY = thePlayer.lastTickPosY + (thePlayer.posY - thePlayer.lastTickPosY) * partialTicks;
        renderPosZ = thePlayer.lastTickPosZ + (thePlayer.posZ - thePlayer.lastTickPosZ) * partialTicks;
    }

    private double distance() {
        // it appears optifine might mess up the renderViewEntity's posX and lasttickposx, so we have to do it
        // ourselves
        double dx = coord.x - renderPosX;
        double dy = coord.y - renderPosY;
        double dz = coord.z - renderPosZ;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
