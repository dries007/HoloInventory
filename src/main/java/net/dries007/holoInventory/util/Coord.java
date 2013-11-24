/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Dries K. Aka Dries007
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.dries007.holoInventory.util;

import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;

public class Coord
{
    public int dim;
    public int x;
    public int y;
    public int z;

    public Coord(int dim, MovingObjectPosition mop)
    {
        this.dim = dim;

        switch (mop.typeOfHit)
        {
            case TILE:
                this.x = mop.blockX;
                this.y = mop.blockY;
                this.z = mop.blockZ;
                break;
            case ENTITY:
                this.x = (int) mop.entityHit.posX;
                this.y = (int) mop.entityHit.posY;
                this.z = (int) mop.entityHit.posZ;
                break;
        }
    }

    public Coord offset(int side)
    {
        ForgeDirection dir = ForgeDirection.getOrientation(side);
        this.x = +dir.offsetX;
        this.y = +dir.offsetY;
        this.z = +dir.offsetZ;

        return this;
    }

    public int hashCode()
    {
        return this.x + (this.z << 8) + (this.y << 16) + (this.dim << 24);
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

    public void add(double i)
    {
        x += i;
        y += i;
        z += i;
    }
}
