package net.dries007.holoInventory.client.renderers;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public interface IRenderer
{
    void render(WorldClient worldClient, RayTraceResult ray, Vec3d vec3d);
}
