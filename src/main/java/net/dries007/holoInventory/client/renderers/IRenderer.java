package net.dries007.holoInventory.client.renderers;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public interface IRenderer
{
    void render(final WorldClient world, final RayTraceResult hit, final Vec3d pos);

    boolean shouldRender();
}
