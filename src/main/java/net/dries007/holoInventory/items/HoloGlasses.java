package net.dries007.holoInventory.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import tconstruct.library.accessory.IAccessory;

import java.util.List;

public class HoloGlasses  extends Item implements IAccessory
{
    public HoloGlasses()
    {
        super();
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        setCreativeTab(CreativeTabs.tabTools);
    }

    public IIcon icon;
    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister ir) {
        icon = ir.registerIcon("holoinventory:hologlasses");
    }
    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconFromDamage(int meta) {
        return icon;
    }

    @Override
    public boolean canEquipAccessory(ItemStack itemStack, int slot) {
        return slot == 0;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs,List par3List) {
        par3List.add(new ItemStack(this,1,0));
    }

    @Override
    public String getUnlocalizedName(ItemStack par1ItemStack)
    {
        return super.getUnlocalizedName() + "." + par1ItemStack.getItemDamage();
    }
}
