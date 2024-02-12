package net.dries007.holoInventory.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class FluidRenderFakeItem extends Item {

    public FluidRenderFakeItem(String unlocalizedName) {
        setUnlocalizedName(unlocalizedName);
    }

    private Fluid fluid = FluidRegistry.WATER;

    public void setFluid(Fluid fluid) {
        this.fluid = fluid;
    }

    @Override
    public IIcon getIconFromDamage(int p_77617_1_) {
        return fluid.getIcon();
    }

    @Override
    public int getSpriteNumber() {
        return 0;
    }

    @Override
    public int getColorFromItemStack(ItemStack p_82790_1_, int p_82790_2_) {
        return fluid.getColor(new FluidStack(fluid, 0));
    }

    @Override
    public void getSubItems(Item p_150895_1_, CreativeTabs p_150895_2_, List<ItemStack> p_150895_3_) {}
}
