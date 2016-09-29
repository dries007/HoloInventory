package net.dries007.holoInventory.client.renderers;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public interface IRenderer
{
    void render(final WorldClient world, final MovingObjectPosition hit, final Vec3 pos);

    boolean shouldRender();
}
