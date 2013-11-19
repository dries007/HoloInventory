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

package net.dries007.holoInventory;

import net.minecraftforge.common.Configuration;

import java.io.File;

import static net.dries007.holoInventory.util.Data.MODID;

public class Config
{
    final        Configuration configuration;
    public final boolean       colorEnable;
    public final int           colorAlpha;
    public final int           colorR;
    public final int           colorG;
    public final int           colorB;
    public final int           syncFreq;
    public final boolean       renderText;
    public final boolean       renderMultiple;
    public final boolean       doVersioncheck;
    public final int           keyMode;

    public Config(File file)
    {
        configuration = new Configuration(file);

        configuration.addCustomCategoryComment(MODID, "All our settings are in here, as you might expect...");

        colorEnable = configuration.get(MODID, "colorEnable", false, "Enable a BG color").getBoolean(false);
        colorAlpha = configuration.get(MODID, "colorAlpha", 200, "The BG transparancy (0-255)").getInt();
        colorR = configuration.get(MODID, "colorRed", 14, "0-255").getInt();
        colorG = configuration.get(MODID, "colorGreen", 157, "0-255").getInt();
        colorB = configuration.get(MODID, "colorBlue", 196, "0-255").getInt();

        keyMode = configuration.get(MODID,"keyMode", 0,
                "Valid modes:\n" +
                        "0: Always display hologram.\n" +
                        "1: The key toggles the rendering.\n" +
                        "2: Only render hologram while key pressed.\n" +
                        "3: Don't render hologram while key pressed.").getInt();

        renderText = configuration.get(MODID, "renderText", true, "Render the stacksize as text on top of the items").getBoolean(true);
        renderMultiple = configuration.get(MODID, "renderMultiple", true, "Render multiple items depending on stacksize").getBoolean(true);

        doVersioncheck = configuration.get(MODID, "doVersioncheck", true).getBoolean(true);

        syncFreq = configuration.get(MODID, "syncFreq", 2, "Amout of seconds pass before sending a new update to the client looking at the chest.").getInt();

        configuration.save();
    }
}
