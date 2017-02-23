/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 - 2017 Dries K. Aka Dries007
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

package net.dries007.holoInventory.network.response;

import io.netty.buffer.ByteBuf;
import net.dries007.holoInventory.Helper;
import net.dries007.holoInventory.client.ClientEventHandler;
import net.dries007.holoInventory.client.renderers.IRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public abstract class ResponseMessage implements IMessage
{
    private Helper.Type type;
    private int id;
    private BlockPos pos;

    @SuppressWarnings("unused") // netty again
    public ResponseMessage()
    {
    }

    public ResponseMessage(int id)
    {
        this.type = Helper.Type.ENTITY;
        this.id = id;
    }

    public ResponseMessage(BlockPos pos)
    {
        this.type = Helper.Type.TILE;
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        type = Helper.Type.values()[buf.readByte()];
        switch (type)
        {
            case TILE: pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()); break;
            case ENTITY: id = buf.readInt(); break;
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(type.ordinal());
        switch (type)
        {
            case TILE:
                buf.writeInt(pos.getX());
                buf.writeInt(pos.getY());
                buf.writeInt(pos.getZ());
                break;
            case ENTITY:
                buf.writeInt(id);
                break;
        }
    }

    public final Helper.Type getType()
    {
        return type;
    }

    public final BlockPos getPos()
    {
        return pos;
    }

    public final int getId()
    {
        return id;
    }

    public static void handle(ResponseMessage message, IRenderer renderer)
    {
        switch (message.getType())
        {
            case TILE: ClientEventHandler.cache(message.getPos(), renderer); break;
            case ENTITY: ClientEventHandler.cache(message.getId(), renderer); break;
        }
    }
}
