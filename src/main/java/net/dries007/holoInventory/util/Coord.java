package net.dries007.holoInventory.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class Coord
{
    public int dim;
    public int x;
    public int y;
    public int z;

    public Coord(int dim, int x, int y, int z)
    {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Coord(int dim, MovingObjectPosition mop)
    {
        this.dim = dim;
        this.x = mop.blockX;
        this.y = mop.blockY;
        this.z = mop.blockZ;
    }

    public Coord(int hash)
    {
        this.dim = hash >> 24;
        this.y = (hash >> 16) - (this.dim << 24);
        this.z = (hash >> 8) - (this.dim << 24) - (this.y << 16);
        this.x = hash  - (this.dim << 24) - (this.y << 16) - (this.z << 8);
    }

    public Coord(NBTTagCompound coord)
    {
        this.dim = coord.getInteger("dim");
        this.x = coord.getInteger("x");
        this.y = coord.getInteger("y");
        this.z = coord.getInteger("z");
    }

    public int hashCode()
    {
        return this.x + this.z << 8 + this.y << 16 + this.dim << 24;
    }

    public boolean equals(Object obj)
    {
        if (obj instanceof Coord)
        {
            Coord coord = (Coord) obj;
            return this.x == coord.x && this.y == coord.y && this.z == coord.z && this.dim == coord.dim;
        }
        return false;
    }

    public NBTTagCompound toNBT()
    {
        NBTTagCompound root = new NBTTagCompound();

        root.setInteger("dim", this.dim);
        root.setInteger("x", this.x);
        root.setInteger("y", this.y);
        root.setInteger("z", this.z);

        return root;
    }
}
