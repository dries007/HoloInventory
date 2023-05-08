package net.dries007.holoInventory.client;

import java.text.DecimalFormat;
import java.util.List;

import net.dries007.holoInventory.Config;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.util.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * Keeps track of render scale, spacing, etc. to draw a set of icons prettier
 */
public class GroupRenderer {

    private static float time;
    private final EntityItem fakeEntityItem = new EntityItem(Minecraft.getMinecraft().theWorld);
    private static final int TEXT_COLOR = 255 + (255 << 8) + (255 << 16) + (170 << 24);

    // changed with an attached debugger..
    static int stackSizeDebugOverride = 0;

    private float scale, width, height, spacing, offset;
    private int columns, rows;
    private boolean renderText;

    public GroupRenderer() {
        fakeEntityItem.setEntityItemStack(new ItemStack(HoloInventory.fluidRenderFakeItem));
        fakeEntityItem.hoverStart = 0f;
    }

    public static void updateTime() {
        time = (float) (360.0 * (double) (System.currentTimeMillis() & 0x3FFFL) / (double) 0x3FFFL);
    }

    public void calculateColumns(int totalAmount) {
        if (totalAmount < 9) columns = totalAmount;
        else if (totalAmount <= 27) columns = 9;
        else if (totalAmount <= 54) columns = 11;
        else if (totalAmount <= 90) columns = 14;
        else if (totalAmount <= 109) columns = 18;
        else columns = 21;
    }

    public void calculateRows(int totalAmount) {
        setRows((totalAmount % columns == 0) ? (totalAmount / columns) - 1 : totalAmount / columns);
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setScale(float scaleAddition) {
        float scaleModifier;
        if (columns > 9) scaleModifier = 0.2f - columns * 0.005f;
        else scaleModifier = 0.2f + (9 - columns) * 0.05f;
        scale = scaleModifier + scaleAddition;
    }

    public void setSpacing(float spacing) {
        this.spacing = spacing;
        width = columns * getActualSpacing();
        height = rows * getActualSpacing();
    }

    public void setOffset(float offset) {
        this.offset = offset;
    }

    public void setRenderText(boolean renderText) {
        this.renderText = renderText;
    }

    public void reset() {
        scale = 0;
        width = 0;
        height = 0;
        spacing = 0;
        offset = 0;
        columns = 0;
        rows = 0;
        renderText = false;
    }

    private float getActualSpacing() {
        return scale * (spacing + 0.1f) * 0.4f;
    }

    public float calculateOffset() {
        return height + getActualSpacing() * 2;
    }

    public void renderItems(List<ItemStack> itemStacks) {
        int column = 0, row = 0;
        for (ItemStack stack : itemStacks) {
            renderItem(stack, column, row);
            column++;
            if (column >= columns) {
                column = 0;
                row++;
            }
        }
    }

    /**
     * Renders 1 item
     *
     * @param itemStack itemStack to render
     * @param column    the column the item needs to be rendered at
     * @param row       the row the item needs to be rendered at
     */
    private void renderItem(ItemStack itemStack, int column, int row) {
        if (itemStack == null) return;
        fakeEntityItem.setEntityItemStack(itemStack);
        if (itemStack.hasEffect(0)) {
            GL11.glDisable(GL11.GL_LIGHTING);
        }
        doRenderEntityItem(
                column,
                row,
                (itemStack.getMaxStackSize() == 1 && itemStack.stackSize == 1) ? null
                        : doStackSizeCrap(itemStack.stackSize));
    }

    public void renderFluids(List<FluidTankInfo> fluidTankInfos) {
        int column = 0, row = 0;
        for (FluidTankInfo tankInfo : fluidTankInfos) {
            renderFluid(tankInfo.fluid, column, row);
            column++;
            if (column >= columns) {
                column = 0;
                row++;
            }
        }
    }

    private void renderFluid(FluidStack fluidStack, int column, int row) {
        Fluid fluid = fluidStack.getFluid();
        HoloInventory.fluidRenderFakeItem.setFluid(fluid);
        String suffix = Config.renderSuffixDarkened ? EnumChatFormatting.GRAY + "L" : "L";
        doRenderEntityItem(column, row, doStackSizeCrap(fluidStack.amount) + suffix);
    }

    private void doRenderEntityItem(int column, int row, String stackSizeText) {
        RenderHelper.enableStandardItemLighting();
        GL11.glPushMatrix();
        GL11.glTranslatef(width - ((column + 0.2f) * scale * spacing), height - ((row + 0.05f) * scale * spacing), 0f);
        GL11.glTranslatef(0, offset, 0);
        GL11.glScalef(scale, scale, scale);
        if (Minecraft.getMinecraft().gameSettings.fancyGraphics)
            GL11.glRotatef(Config.rotateItems ? time : 0f, 0.0F, 1.0F, 0.0F);
        else GL11.glRotatef(RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        ClientHandler.RENDER_ITEM.doRender(fakeEntityItem, 0, 0, 0, 0, 0);
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        if (renderText && stackSizeText != null) {
            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glTranslatef(
                    width - ((column + 0.2f) * scale * spacing),
                    height - ((row + 0.05f) * scale * spacing),
                    0f);
            GL11.glTranslatef(0, offset, 0);
            GL11.glScalef(scale, scale, scale);
            GL11.glScalef(0.03f, 0.03f, 0.03f);
            GL11.glRotatef(180, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-1f, 1f, 0f);
            GL11.glTranslatef(-RenderManager.instance.getFontRenderer().getStringWidth(stackSizeText) / 2f, 0f, 0f);
            RenderManager.instance.getFontRenderer().drawString(stackSizeText, 0, 0, TEXT_COLOR, true);
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glPopMatrix();
        }
    }

    public static void renderBG(GroupRenderer... renderers) {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        Tessellator tess = Tessellator.instance;
        Tessellator.renderingWorldRenderer = false;
        tess.startDrawing(GL11.GL_QUADS);
        tess.setColorRGBA(Config.colorR, Config.colorG, Config.colorB, Config.colorAlpha);
        float x0 = Helper.max(renderer -> renderer.width + renderer.scale / 3, renderers);
        float x1 = Helper.min(renderer -> -renderer.width - renderer.scale / 3, renderers);
        float y0 = Helper.max(renderer -> renderer.scale / 3 + renderer.height + renderer.offset, renderers);
        float y1 = Helper.min(renderer -> -renderer.scale / 3 - renderer.height, renderers);
        tess.addVertex(x0, y1, 0);
        tess.addVertex(x1, y1, 0);
        tess.addVertex(x1, y0, 0);
        tess.addVertex(x0, y0, 0);
        tess.draw();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    public static void renderName(String name, GroupRenderer... renderers) {
        FontRenderer fontRenderer = RenderManager.instance.getFontRenderer();
        if (Config.nameOverrides.containsKey(name)) name = Config.nameOverrides.get(name);
        else name = StatCollector.translateToLocal(name);
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        GL11.glTranslatef(0f, Helper.sum(renderer -> renderer.height + renderer.scale / 1.25f, renderers), 0f);

        float scale = Helper.max(renderer -> renderer.scale, renderers);
        GL11.glScaled(scale, scale, scale);
        GL11.glScalef(1.5f, 1.5f, 1.5f);
        GL11.glScalef(0.03f, 0.03f, 0.03f);
        GL11.glTranslated(fontRenderer.getStringWidth(name) / 2f, 0f, 0f);
        GL11.glRotatef(180, 0.0F, 0.0F, 1.0F);
        fontRenderer.drawString(name, 0, 0, TEXT_COLOR, true);

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopMatrix();
    }

    private static final DecimalFormat DF_ONE_FRACTION_DIGIT = new DecimalFormat("##.0");
    private static final DecimalFormat DF_TWO_FRACTION_DIGIT = new DecimalFormat("#.00");
    private static final String[] suffixNormal = { "", "K", "M", "B" };
    private static final String[] suffixDarkened = { "", EnumChatFormatting.GRAY + "K", EnumChatFormatting.GRAY + "M",
            EnumChatFormatting.GRAY + "B" };

    /**
     * Shifts GL & returns the string
     *
     * @param stackSize the stackSize.
     * @return the string to be rendered
     */
    private String doStackSizeCrap(int stackSize) {
        if (stackSizeDebugOverride != 0) stackSize = stackSizeDebugOverride;
        return formatStackSize(stackSize);
    }

    private static String formatStackSize(long i) {
        String[] suffixSelected = Config.renderSuffixDarkened ? suffixDarkened : suffixNormal;
        int level = 0;
        while (i > 1000 && level < suffixSelected.length - 1) {
            level++;
            if (i >= 100_000) {
                // still more level to go, or 0 fraction digit
                i /= 1000;
            } else if (i >= 10_000) {
                // 1 fraction digit
                return DF_ONE_FRACTION_DIGIT.format(i / 1000.0d) + suffixSelected[level];
            } else {
                // 2 fraction digit
                return DF_TWO_FRACTION_DIGIT.format(i / 1000.0d) + suffixSelected[level];
            }
        }
        return i + suffixSelected[level];
    }
}
