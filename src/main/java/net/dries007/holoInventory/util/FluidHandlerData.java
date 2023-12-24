package net.dries007.holoInventory.util;

import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_CAPACITY;
import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_ID;
import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_TANK;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.network.BlockFluidHandlerMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class FluidHandlerData {

    public final int id;
    public WeakReference<IFluidHandler> te;
    public final WeakHashMap<EntityPlayer, NBTTagCompound> playerSet = new WeakHashMap<>();

    public FluidHandlerData(IFluidHandler fluidHandler, int id) {
        this.id = id;
        this.te = new WeakReference<>(fluidHandler);
    }

    public void sendIfOld(EntityPlayerMP player) {
        IFluidHandler fluidHandler = te.get();
        if (fluidHandler == null) {
            return;
        }
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger(NBT_KEY_ID, id);
        data.setTag(NBT_KEY_TANK, encodeFluidTankInfo(fluidHandler));

        if (!playerSet.containsKey(player) || !playerSet.get(player).equals(data)) {
            playerSet.put(player, data);
            HoloInventory.getSnw().sendTo(new BlockFluidHandlerMessage(data), player);
        }
    }

    private NBTTagList encodeFluidTankInfo(IFluidHandler fluidHandler) {
        NBTTagList tagList = new NBTTagList();
        FluidTankInfo[] tankInfos = fluidHandler.getTankInfo(ForgeDirection.UNKNOWN);
        if (tankInfos == null) return tagList;

        for (FluidTankInfo tankInfo : tankInfos) {
            NBTTagCompound fluidTag = new NBTTagCompound();
            if (tankInfo.fluid != null) {
                tankInfo.fluid.writeToNBT(fluidTag);
                fluidTag.setInteger(NBT_KEY_CAPACITY, tankInfo.capacity);
                tagList.appendTag(fluidTag);
            }
        }
        return tagList;
    }

    public void update(IFluidHandler fluidHandler) {
        te = new WeakReference<>(fluidHandler);
    }
}
