package net.dries007.holoInventory.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dries007.holoInventory.Helper;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.client.renderers.IRenderer;
import net.dries007.holoInventory.network.request.EntityRequest;
import net.dries007.holoInventory.network.request.TileRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class ClientEventHandler
{
    public static final DecimalFormat DF = new DecimalFormat("#.#k");
    public static final int TEXT_COLOR = 255 + (255 << 8) + (255 << 16) + (255 << 24);

    private static final Cache<BlockPos, IRenderer> TILE_CACHE = CacheBuilder.newBuilder().maximumSize(150).expireAfterWrite(500, TimeUnit.MILLISECONDS).build();
    private static final Cache<Integer, IRenderer> ENTITY_CACHE = CacheBuilder.newBuilder().maximumSize(150).expireAfterWrite(500, TimeUnit.MILLISECONDS).build();

    private static ClientEventHandler instance;
    private static boolean enabled = true;
    private static boolean toggleState = true;
    private static KeyBinding keyHold;
    private static KeyBinding keyToggle;

    private WeakReference<WorldClient> worldRef = new WeakReference<>(null);

    private ClientEventHandler()
    {

    }

    public static void init()
    {
        if (instance != null) MinecraftForge.EVENT_BUS.unregister(instance);
        instance = new ClientEventHandler();
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());

        keyHold = new KeyBinding("Hold to show", KeyConflictContext.IN_GAME, Keyboard.KEY_H, HoloInventory.MODID);
        keyToggle = new KeyBinding("Toggle to show", KeyConflictContext.IN_GAME, 0, HoloInventory.MODID);
        ClientRegistry.registerKeyBinding(keyHold);
        ClientRegistry.registerKeyBinding(keyToggle);
    }

    public static void cache(BlockPos pos, IRenderer iRenderer)
    {
        TILE_CACHE.put(pos, iRenderer);
    }

    public static void cache(int id, IRenderer iRenderer)
    {
        ENTITY_CACHE.put(id, iRenderer);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event)
    {
        if (keyToggle.isPressed()) toggleState = !toggleState;
        if (keyHold.isKeyDown()) enabled = true;
    }

    @SubscribeEvent
    public void updateEvent(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.isGamePaused()) return;
        WorldClient mcWorld = Minecraft.getMinecraft().theWorld;
        if (mcWorld == null) return;

        WorldClient world = worldRef.get();
        if (world == null || mcWorld != world)
        {
            TILE_CACHE.invalidateAll();
            ENTITY_CACHE.invalidateAll();
            worldRef = new WeakReference<>(mcWorld);
            return;
        }

        enabled = (Helper.showOnSneak && mc.gameSettings.keyBindSneak.isKeyDown()) ||
                (Helper.showOnSprint && mc.gameSettings.keyBindSprint.isKeyDown()) ||
                (keyHold.getKeyCode() != 0 && keyHold.isKeyDown()) ||
                (keyToggle.getKeyCode() != 0 && toggleState);

        if (!enabled) return;

        TILE_CACHE.cleanUp();
        ENTITY_CACHE.cleanUp();

        RayTraceResult ray = mc.objectMouseOver;
        if (ray == null) return;

        if (ray.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            final TileEntity tileEntity = world.getTileEntity(ray.getBlockPos());
            if (tileEntity == null || !Helper.accept(tileEntity) || Helper.banned.contains(tileEntity.getClass().getCanonicalName())) return;
            HoloInventory.getSnw().sendToServer(new TileRequest(world.provider.getDimension(), ray.getBlockPos()));
        }
        else if (ray.typeOfHit == RayTraceResult.Type.ENTITY && Helper.accept(ray.entityHit))
        {
            HoloInventory.getSnw().sendToServer(new EntityRequest(world.provider.getDimension(), ray.entityHit.getEntityId()));
        }
    }

    @SubscribeEvent
    public void renderEvent(RenderWorldLastEvent event)
    {
        if (worldRef.get() == null || !enabled) return;
        Minecraft mc = Minecraft.getMinecraft();
        RayTraceResult ray = mc.objectMouseOver;
        if (ray == null) return;

        if (ray.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            final IRenderer renderer = TILE_CACHE.getIfPresent(ray.getBlockPos());
            if (renderer == null) return;
            try
            {
                renderer.render(worldRef.get(), ray, ray.hitVec);
            }
            catch (Exception e)
            {
                HoloInventory.getLogger().warn("Some error while rendering the hologram :(");
                HoloInventory.getLogger().warn("INFO: Block @ {}", ray.getBlockPos());
                HoloInventory.getLogger().warn("Please make an issue on github if this happens.");
                HoloInventory.getLogger().catching(e);
            }
        }
        else if (ray.typeOfHit == RayTraceResult.Type.ENTITY)
        {
            final IRenderer renderer = ENTITY_CACHE.getIfPresent(ray.entityHit.getEntityId());
            if (renderer == null) return;
            try
            {
                renderer.render(worldRef.get(), ray, ray.hitVec);
            }
            catch (Exception e)
            {
                HoloInventory.getLogger().warn("Some error while rendering the hologram :(");
                HoloInventory.getLogger().warn("INFO: Entity: {}", ray.entityHit);
                HoloInventory.getLogger().warn("Please make an issue on github if this happens.");
                HoloInventory.getLogger().catching(e);
            }
        }
    }
}
