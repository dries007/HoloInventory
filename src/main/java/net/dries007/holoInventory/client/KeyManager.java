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

package net.dries007.holoInventory.client;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.TickType;
import net.dries007.holoInventory.HoloInventory;
import net.minecraft.client.settings.KeyBinding;

import java.util.EnumSet;

import static net.dries007.holoInventory.util.Data.MODID;

public class KeyManager extends KeyBindingRegistry.KeyHandler
{
    static final KeyBinding key = new KeyBinding("HoloInventory", 0);

    public KeyManager()
    {
        super(new KeyBinding[] {key}, new boolean[] {false});
        if (HoloInventory.instance.config.keyMode == 2) Renderer.enabled = false;
    }

    /**
     * Valid modes:
     * 0: Always display hologram.
     * 1: The key toggles the rendering.
     * 2: Only render hologram while key pressed.
     * 3: Don't render hologram while key pressed.
     */

    @Override
    public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat)
    {
        if (tickEnd) return;
        switch (HoloInventory.instance.config.keyMode)
        {
            case 1:
                Renderer.enabled = !Renderer.enabled;
                break;
            case 2:
                Renderer.enabled = true;
                break;
            case 3:
                Renderer.enabled = false;
                break;
        }
    }

    @Override
    public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd)
    {
        if (tickEnd) return;
        switch (HoloInventory.instance.config.keyMode)
        {
            case 2:
                Renderer.enabled = false;
                break;
            case 3:
                Renderer.enabled = true;
                break;
        }
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.CLIENT);
    }

    @Override
    public String getLabel()
    {
        return MODID + "_KeyManager";
    }
}
