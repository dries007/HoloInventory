package net.dries007.holoInventory.items;

import java.util.List;

import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.api.IHoloGlasses;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;

import org.lwjgl.input.Keyboard;

import tconstruct.armor.ArmorProxyClient;
import tconstruct.armor.player.TPlayerStats;
import tconstruct.library.accessory.IAccessory;
import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.common.Optional.Method;

@InterfaceList({ @Interface(iface = "baubles.api.IBauble", modid = "Baubles"),
        @Interface(iface = "tconstruct.library.accessory.IAccessory", modid = "TConstruct") })
public class HoloGlasses extends ItemArmor implements IHoloGlasses, IBauble, IAccessory {

    public static final ArmorMaterial MATERIAL = EnumHelper
            .addArmorMaterial("holoGlasses", 0, new int[] { 0, 0, 0, 0 }, 0);

    public HoloGlasses(String name) {
        super(MATERIAL, 0, 0);
        this.setMaxStackSize(1);
        this.setTextureName(HoloInventory.MODID + ":" + name);
        this.setUnlocalizedName(name);
        setCreativeTab(CreativeTabs.tabTools);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
        return "holoinventory:textures/models/armor/glasses.png";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        list.add("You can put it in the Tinkers mask slot");
        list.add("<Hold Shift>");
        if ((Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))) {
            list.add(EnumChatFormatting.RED + "Adds Holographic inventory screen for all blocks with an inventory");
        }
    }

    public static ItemStack getHoloGlasses(World world, EntityPlayer player) {
        if (player.inventory.getStackInSlot(39) != null
                && player.inventory.getStackInSlot(39).getItem() instanceof IHoloGlasses)
            return player.inventory.getStackInSlot(39);

        if (HoloInventory.isBaublesLoaded) {
            IInventory inventory = BaublesApi.getBaubles(player);
            for (int i = 0; i != inventory.getSizeInventory(); i++) if (inventory.getStackInSlot(i) != null
                    && inventory.getStackInSlot(i).getItem() instanceof IHoloGlasses)
                return inventory.getStackInSlot(i);
        }

        if (HoloInventory.isTinkersLoaded) {
            IInventory inventory = TPlayerStats.get(player).armor;

            if (world.isRemote) for (int i = 0; i != inventory.getSizeInventory(); i++) {
                if (ArmorProxyClient.armorExtended.getStackInSlot(i) != null
                        && ArmorProxyClient.armorExtended.getStackInSlot(i).getItem() instanceof IHoloGlasses)
                    return ArmorProxyClient.armorExtended.getStackInSlot(i);
            }
            else for (int i = 0; i != inventory.getSizeInventory(); i++) if (inventory.getStackInSlot(i) != null
                    && inventory.getStackInSlot(i).getItem() instanceof IHoloGlasses)
                return inventory.getStackInSlot(i);
        }
        return null;
    }

    @Override
    public boolean shouldRender(ItemStack stack) {
        return true;
    }

    // TConstruct

    @Override
    @Method(modid = "TConstruct")
    public boolean canEquipAccessory(ItemStack itemStack, int slot) {
        return slot == 0;
    }

    // Baubles

    @Override
    @Method(modid = "Baubles")
    public boolean canEquip(ItemStack arg0, EntityLivingBase arg1) {
        return true;
    }

    @Override
    @Method(modid = "Baubles")
    public boolean canUnequip(ItemStack arg0, EntityLivingBase arg1) {
        return true;
    }

    @Override
    @Method(modid = "Baubles")
    public BaubleType getBaubleType(ItemStack arg0) {
        return BaubleType.RING;
    }

    @Override
    @Method(modid = "Baubles")
    public void onEquipped(ItemStack arg0, EntityLivingBase arg1) {}

    @Override
    @Method(modid = "Baubles")
    public void onUnequipped(ItemStack arg0, EntityLivingBase arg1) {}

    @Override
    @Method(modid = "Baubles")
    public void onWornTick(ItemStack arg0, EntityLivingBase arg1) {}
}
