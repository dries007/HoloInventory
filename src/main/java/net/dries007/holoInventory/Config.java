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
import java.util.ArrayList;
import java.util.Arrays;

import static net.dries007.holoInventory.util.Data.MODID;

public class Config
{
    final Configuration configuration;

    public boolean colorEnable    = false;
    public int     colorAlpha     = 200;
    public int     colorR         = 14;
    public int     colorG         = 157;
    public int     colorB         = 196;
    public boolean doVersionCheck = true;
    public int     syncFreq       = 2;
    public boolean renderText     = true;
    public boolean renderMultiple = true;
    public boolean enableStacking = true;
    public boolean renderName     = true;
    public int     mode           = 0;
    public int     cycle          = 0;

    public int keyMode;
    public ArrayList<String> bannedTiles    = new ArrayList<String>();
    public ArrayList<String> bannedEntities = new ArrayList<String>();

    public Config(File file)
    {
        configuration = new Configuration(file);
        doConfig();
    }

    public void overrideBannedThings()
    {
        configuration.get(MODID, "bannedTiles", bannedTiles.toArray(new String[bannedTiles.size()]), "Banned inventories.\n" + "Use the ingame command '/holoinventory' to change this list easily.").set(bannedTiles.toArray(new String[bannedTiles.size()]));
        configuration.get(MODID, "bannedEntities", bannedEntities.toArray(new String[bannedEntities.size()]), "Banned inventories.\n" + "Use the ingame command '/holoinventory' to change this list easily.").set(bannedEntities.toArray(new String[bannedEntities.size()]));
        configuration.save();
    }

    public void doConfig()
    {
        configuration.addCustomCategoryComment(MODID, "All our settings are in here, as you might expect...");

        colorEnable = configuration.get(MODID, "colorEnable", colorEnable, "Enable a BG color").getBoolean(false);
        colorAlpha = configuration.get(MODID, "colorAlpha", colorAlpha, "The BG transparancy (0-255)").getInt();
        colorR = configuration.get(MODID, "colorRed", colorR, "0-255").getInt();
        colorG = configuration.get(MODID, "colorGreen", colorG, "0-255").getInt();
        colorB = configuration.get(MODID, "colorBlue", colorB, "0-255").getInt();

        keyMode = configuration.get(MODID, "keyMode", keyMode, "Valid modes:\n" +
                "0: Always display hologram.\n" +
                "1: The key toggles the rendering.\n" +
                "2: Only render hologram while key pressed.\n" +
                "3: Don't render hologram while key pressed.").getInt();

        renderName = configuration.get(MODID, "renderName", renderName, "Renders the inv name above the hologram").getBoolean(true);
        renderText = configuration.get(MODID, "renderText", renderText, "Render the stacksize as text on top of the items").getBoolean(true);
        renderMultiple = configuration.get(MODID, "renderMultiple", renderMultiple, "Render multiple items depending on stacksize").getBoolean(true);

        doVersionCheck = configuration.get(MODID, "doVersionCheck", doVersionCheck).getBoolean(true);

        syncFreq = configuration.get(MODID, "syncFreq", syncFreq, "Amout of seconds pass before sending a new update to the client looking at the chest.").getInt();

        enableStacking = configuration.get(MODID, "enableStacking", enableStacking, "Stack items, even above 64.").getBoolean(enableStacking);

        mode = configuration.get(MODID, "mode", mode, "Valid modes:\n" +
                "0: Default mode (Display all items).\n" +
                "1: Sorting mode, biggest stack size first.\n" +
                "2: Most abundant mode (Only display the item the most abundant in the chest.\n" +
                "3: Same as 1, but with 3 items.\n" +
                "4: Same as 1, but with 5 items.\n" +
                "5: Same as 1, but with 7 items.\n" +
                "6: Same as 1, but with 9 items.").getInt();

        cycle = configuration.get(MODID, "cycle", cycle, "Cycle trough all the items one by one. Set to the delay time wanted in ticks. If 0, cycle mode is off. Still takes into a count the mode.").getInt();

        bannedTiles.clear();
        bannedTiles.addAll(Arrays.asList(configuration.get(MODID,
                "bannedTiles",
                bannedTiles.toArray(new String[bannedTiles.size()]),
                "Banned inventories.\n" + "Use the ingame command '/holoinventory' to change this list easily.").getStringList()));
        bannedEntities.clear();
        bannedEntities.addAll(Arrays.asList(configuration.get(MODID,
                "bannedEntities",
                bannedEntities.toArray(new String[bannedEntities.size()]),
                "Banned inventories.\n" + "Use the ingame command '/holoinventory' to change this list easily.").getStringList()));

        configuration.save();
    }
}
