/*
 * Copyright (c) 2014. Dries K. Aka Dries007 Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions: The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.dries007.holoInventory.client;

import net.dries007.holoInventory.Config;
import net.dries007.holoInventory.HoloInventory;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;

public class KeyManager {

    public static final KeyBinding key = new KeyBinding(
            "HoloGlasses",
            Keyboard.KEY_NUMPAD5,
            "key.categories.holoinventory");

    public KeyManager() {
        ClientRegistry.registerKeyBinding(key);
        switch (Config.keyMode) {
            case 1:
                Renderer.INSTANCE.enabled = Config.keyState;
                break;
            case 2:
                Renderer.INSTANCE.enabled = false;
                break;
        }
    }

    /**
     * Valid modes: 0: Always display hologram. 1: The key toggles the rendering. 2: Only render hologram while key
     * pressed. (Handled in Renderer) 3: Don't render hologram while key pressed. (Handled in Renderer)
     */
    boolean alreadyToggling = false;

    @SubscribeEvent
    public void input(InputEvent.KeyInputEvent event) {
        final boolean isKeyPressed = key.getIsKeyPressed();
        switch (Config.keyMode) {
            case 1:
                if (isKeyPressed && FMLClientHandler.instance().getClient().inGameHasFocus) {
                    if (!alreadyToggling) {
                        alreadyToggling = true;
                        Renderer.INSTANCE.enabled = !Renderer.INSTANCE.enabled;
                        HoloInventory.getConfig().setKeyState(Renderer.INSTANCE.enabled);
                    }
                } else {
                    alreadyToggling = false;
                }
                break;
            case 2:
                Renderer.INSTANCE.enabled = isKeyPressed;
                break;
            case 3:
                Renderer.INSTANCE.enabled = !isKeyPressed;
                break;
        }
    }
}
