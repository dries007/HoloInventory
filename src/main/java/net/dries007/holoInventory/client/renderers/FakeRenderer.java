package net.dries007.holoInventory.client.renderers;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class FakeRenderer implements IRenderer
{
    @Override
    public void render(WorldClient world, MovingObjectPosition hit, Vec3 pos)
    {

    }

    @Override
    public boolean shouldRender()
    {
        return false;
    }
}
