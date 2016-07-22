package net.dries007.holoInventory.client.renderers;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class FakeRenderer implements IRenderer
{
    @Override
    public void render(WorldClient world, RayTraceResult hit, Vec3d pos)
    {

    }

    @Override
    public boolean shouldRender()
    {
        return false;
    }
}
