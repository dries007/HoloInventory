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

package net.dries007.holoInventory.server;

import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_ID;
import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_TYPE;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.dries007.holoInventory.Config;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.network.RemoveInventoryMessage;
import net.dries007.holoInventory.network.RenameMessage;
import net.dries007.holoInventory.util.Coord;
import net.dries007.holoInventory.util.FakeInventory;
import net.dries007.holoInventory.util.FluidHandlerData;
import net.dries007.holoInventory.util.Helper;
import net.dries007.holoInventory.util.InventoryData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockJukebox;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.IFluidHandler;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.api.storage.data.IAEItemStack;
import appeng.parts.misc.PartInterface;
import appeng.tile.misc.TileInterface;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;

public class ServerEventHandler {

    public final List<String> banUsers = new ArrayList<>();
    public final HashMap<String, String> overrideUsers = new HashMap<>();
    public final HashMap<Integer, InventoryData> mapBlockToInv = new HashMap<>();
    public final HashMap<Integer, FluidHandlerData> mapBlockToFluidHandler = new HashMap<>();

    private static class CachedPatternInventory {

        public static int computeHash(IInventory key) {
            int h = 0;
            for (int i = 0; i < key.getSizeInventory(); ++i) {
                ItemStack s = key.getStackInSlot(i);
                // we only care about NBT because patterns are all the same otherwise
                if (s != null && s.getTagCompound() != null) {
                    h ^= s.getTagCompound().hashCode();
                }
            }
            return h;
        }

        public CachedPatternInventory(IInventory wrapper, IInventory key) {
            inventory = new WeakReference<>(wrapper);
            hash = computeHash(key);
        }

        public final WeakReference<IInventory> inventory;
        public final int hash;
    }

    final Map<IInventory, CachedPatternInventory> wrappedInventoryCache = new WeakHashMap<>();
    private static final String JUKEBOX_NAME = "Jukebox";

    @SubscribeEvent()
    public void event(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        if (banUsers.contains(event.entityPlayer.getDisplayName())) { // TODO do we really need to check a list on each
                                                                      // players right click ?
            banUsers.remove(event.entityPlayer.getDisplayName());
            event.setCanceled(true);

            TileEntity te = event.entity.worldObj.getTileEntity(event.x, event.y, event.z);
            if (Helper.weWant(te)) {
                Config.bannedTiles.add(te.getClass().getCanonicalName());
                event.entityPlayer.addChatComponentMessage(
                        new ChatComponentText(
                                te.getClass().getCanonicalName() + " will no longer display a hologram."));
            } else {
                event.entityPlayer.addChatComponentMessage(new ChatComponentText("That is no inventory. Try again."));
            }

            HoloInventory.getConfig().overrideBannedThings();
        }

        if (overrideUsers.containsKey(event.entityPlayer.getDisplayName())) { // TODO do we really need to check a list
                                                                              // on each players right click ?
            if (FMLCommonHandler.instance().getEffectiveSide().isClient()) return;

            String nameOverride = overrideUsers.get(event.entityPlayer.getDisplayName());
            overrideUsers.remove(event.entityPlayer.getDisplayName());
            event.setCanceled(true);

            TileEntity te = event.entity.worldObj.getTileEntity(event.x, event.y, event.z);
            if (Helper.weWant(te)) {
                String name = null;
                if (te instanceof BlockJukebox.TileEntityJukebox) name = JUKEBOX_NAME;
                else if (te instanceof IInventory) name = ((IInventory) te).getInventoryName();
                else if (te instanceof TileEntityEnderChest)
                    name = event.entityPlayer.getInventoryEnderChest().getInventoryName();

                HoloInventory.getSnw().sendTo(
                        new RenameMessage(name == null ? "" : name, nameOverride),
                        (EntityPlayerMP) event.entityPlayer);
                event.entityPlayer.addChatComponentMessage(
                        new ChatComponentText(te.getClass().getCanonicalName() + " will now be named " + nameOverride));
            } else {
                event.entityPlayer.addChatComponentMessage(new ChatComponentText("That is no inventory. Try again."));
            }
        }
    }

    @SubscribeEvent()
    public void event(EntityInteractEvent event) {
        if (banUsers.contains(event.entityPlayer.getDisplayName())) { // TODO do we really need to check a list on each
                                                                      // players right click ?
            banUsers.remove(event.entityPlayer.getDisplayName());
            event.setCanceled(true);
            if (Helper.weWant(event.target)) {
                Config.bannedEntities.add(event.target.getClass().getCanonicalName());
                event.entityPlayer.addChatComponentMessage(
                        new ChatComponentText(
                                event.target.getClass().getCanonicalName() + " will no longer display a hologram."));
            } else {
                event.entityPlayer.addChatComponentMessage(new ChatComponentText("That is no inventory. Try again."));
            }
            HoloInventory.getConfig().overrideBannedThings();
        }
    }

    @SubscribeEvent()
    public void event(TickEvent.PlayerTickEvent event) {
        try {
            if (event.phase != TickEvent.Phase.END || event.side != Side.SERVER) return;
            final EntityPlayerMP player = (EntityPlayerMP) event.player;
            final WorldServer world = player.getServerForPlayer();
            if (world == null) return;

            final MovingObjectPosition mo = Helper.getPlayerLookingSpot(player);
            if (mo == null) return;

            switch (mo.typeOfHit) {
                case BLOCK:
                    handleInventoryBlock(world, player, mo);
                    handleFluidHandlerBlock(world, player, mo);
                    break;
                case ENTITY:
                    if (mo.entityHit instanceof IInventory) {
                        processInventoryData(mo.entityHit.getEntityId(), player, (IInventory) mo.entityHit);
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            HoloInventory.getLogger().warn("Some error while sending over inventory, no hologram for you :(");
            HoloInventory.getLogger().warn("Please make an issue on github if this happens.");
            e.printStackTrace();
        }
    }

    private void handleInventoryBlock(WorldServer world, EntityPlayerMP player, MovingObjectPosition mo) {
        final Coord coord = new Coord(world.provider.dimensionId, mo);
        final int x = (int) coord.x, y = (int) coord.y, z = (int) coord.z;
        final TileEntity te = world.getTileEntity(x, y, z);
        if (te == null) return;

        checkForChangedType(coord, te, player);
        if (Config.bannedTiles.contains(te.getClass().getCanonicalName())) {
            // BANNED THING
            removeInventoryData(coord, player);
        } else if (te instanceof TileEntityChest) {
            final Block block = world.getBlock(x, y, z);
            final TileEntityChest teChest = (TileEntityChest) te;
            IInventory inventory = teChest;

            if (world.getBlock(x, y, z + 1) == block) inventory = new InventoryLargeChest(
                    "container.chestDouble",
                    teChest,
                    (TileEntityChest) world.getTileEntity(x, y, z + 1));
            else if (world.getBlock(x - 1, y, z) == block) inventory = new InventoryLargeChest(
                    "container.chestDouble",
                    (TileEntityChest) world.getTileEntity(x - 1, y, z),
                    teChest);
            else if (world.getBlock(x, y, z - 1) == block) inventory = new InventoryLargeChest(
                    "container.chestDouble",
                    (TileEntityChest) world.getTileEntity(x, y, z - 1),
                    teChest);
            else if (world.getBlock(x + 1, y, z) == block) inventory = new InventoryLargeChest(
                    "container.chestDouble",
                    teChest,
                    (TileEntityChest) world.getTileEntity(x + 1, y, z));

            processInventoryData(coord.hashCode(), player, inventory);
        } else if (HoloInventory.isAE2Loaded && te instanceof TileInterface) {
            final IInventory patterns = ((TileInterface) te).getInventoryByName("patterns");
            final IInventory wrapped = getCachedPatternsWrapper(world, ((TileInterface) te).getCustomName(), patterns);
            processInventoryData(coord.hashCode(), player, wrapped);
        } else if (HoloInventory.isAE2Loaded && te instanceof IPartHost) {
            final Vec3 position = mo.hitVec.addVector(-mo.blockX, -mo.blockY, -mo.blockZ);
            final IPartHost host = (IPartHost) te;
            final SelectedPart sp = host.selectPart(position);
            if (sp != null && sp.part instanceof PartInterface) {
                final IInventory patterns = ((PartInterface) sp.part).getInventoryByName("patterns");
                final IInventory wrapped = getCachedPatternsWrapper(
                        world,
                        ((PartInterface) sp.part).getCustomName(),
                        patterns);
                processInventoryData(coord.hashCode(), player, wrapped);
            } else {
                removeInventoryData(coord, player);
            }
        } else if (te instanceof IInventory) {
            processInventoryData(coord.hashCode(), player, (IInventory) te);
        } else if (te instanceof TileEntityEnderChest) {
            processInventoryData(coord.hashCode(), player, player.getInventoryEnderChest());
        } else if (te instanceof BlockJukebox.TileEntityJukebox) {
            BlockJukebox.TileEntityJukebox realTe = ((BlockJukebox.TileEntityJukebox) te);
            processInventoryData(coord.hashCode(), player, JUKEBOX_NAME, realTe.func_145856_a());
        }
    }

    private IInventory getCachedPatternsWrapper(WorldServer world, String name, IInventory patterns) {
        CachedPatternInventory cache = wrappedInventoryCache.get(patterns);
        IInventory ret;
        if (cache == null || (ret = cache.inventory.get()) == null
                || cache.hash != CachedPatternInventory.computeHash(patterns)) {
            cache = new CachedPatternInventory(ret = convertToOutputItems(name, patterns, world), patterns);
            wrappedInventoryCache.put(patterns, cache);
        }
        return ret;
    }

    private void checkForChangedType(Coord coord, TileEntity te, EntityPlayerMP player) {
        int id = coord.hashCode();
        if (mapBlockToInv.containsKey(id)) {
            final InventoryData data = mapBlockToInv.get(id);
            if (!te.getClass().getCanonicalName().equals(data.getType())) {
                doRemoveInventoryData(id, player, data);
            }
        }
    }

    private void removeInventoryData(Coord coord, EntityPlayerMP player) {
        int id = coord.hashCode();
        if (mapBlockToInv.containsKey(id)) {
            final InventoryData inventoryData = mapBlockToInv.get(id);
            doRemoveInventoryData(id, player, inventoryData);
        }
    }

    private void doRemoveInventoryData(int id, EntityPlayerMP player, InventoryData inventoryData) {
        inventoryData.playerSet.remove(player);
        if (inventoryData.playerSet.isEmpty()) mapBlockToInv.remove(id);
        final NBTTagCompound root = new NBTTagCompound();
        root.setByte(NBT_KEY_TYPE, (byte) 0);
        root.setInteger(NBT_KEY_ID, id);
        HoloInventory.getSnw().sendTo(new RemoveInventoryMessage(root), player);
    }

    private IInventory convertToOutputItems(String name, IInventory patterns, World w) {
        int N = patterns.getSizeInventory();
        final ItemStack[] outputs = new ItemStack[N];
        for (int i = 0; i < N; ++i) {
            final ItemStack stack = patterns.getStackInSlot(i);
            outputs[i] = null;
            if (stack != null && stack.getItem() instanceof ICraftingPatternItem) {
                ICraftingPatternDetails pd = ((ICraftingPatternItem) stack.getItem()).getPatternForItem(stack, w);
                if (pd == null) continue;
                IAEItemStack[] outs = pd.getCondensedOutputs();
                if (outs != null && outs.length > 0) outputs[i] = outs[0].getItemStack();
            }
        }
        return new FakeInventory(name, outputs);
    }

    private void processInventoryData(int id, EntityPlayerMP player, String name, ItemStack... itemStacks) {
        processInventoryData(id, player, new FakeInventory(name, itemStacks));
    }

    private void processInventoryData(int id, EntityPlayerMP player, IInventory inventory) {
        InventoryData inventoryData = mapBlockToInv.get(id);
        if (inventoryData == null) {
            inventoryData = new InventoryData(inventory, id);
        } else {
            inventoryData.update(inventory);
        }
        inventoryData.sendIfOld(player);
        mapBlockToInv.put(id, inventoryData);
    }

    public void clearInventoryData() {
        mapBlockToInv.clear();
    }

    private void handleFluidHandlerBlock(WorldServer world, EntityPlayerMP player, MovingObjectPosition mo) {
        final Coord coord = new Coord(world.provider.dimensionId, mo);
        final int x = (int) coord.x, y = (int) coord.y, z = (int) coord.z;
        final TileEntity te = world.getTileEntity(x, y, z);
        if (te == null) return;

        if (te instanceof IFluidHandler) {
            processFluidHandlerData(coord.hashCode(), player, (IFluidHandler) te);
        }
    }

    private void processFluidHandlerData(int id, EntityPlayerMP player, IFluidHandler fluidHandler) {
        FluidHandlerData fluidHandlerData = mapBlockToFluidHandler.get(id);
        if (fluidHandlerData == null) {
            fluidHandlerData = new FluidHandlerData(fluidHandler, id);
        } else {
            fluidHandlerData.update(fluidHandler);
        }
        fluidHandlerData.sendIfOld(player);
        mapBlockToFluidHandler.put(id, fluidHandlerData);
    }
}
