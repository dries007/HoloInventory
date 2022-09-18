/*
 * Copyright (c) 2014. Dries K. Aka Dries007
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minecraftforge.common.config.Configuration;

public class Config {
    private static Configuration configuration;
    private final File file;

    public static final boolean requireGlasses = true;
    public static boolean colorEnable = false;
    public static int colorAlpha = 200;
    public static int colorR = 14;
    public static int colorG = 157;
    public static int colorB = 196;
    public static int syncFreq = 2;
    public static boolean renderText = true;
    public static boolean renderSuffixDarkened = true;
    public static boolean renderMultiple = true;
    public static boolean enableStacking = true;
    public static boolean renderName = true;
    public static boolean hideItemsNotSelected = true;
    public static int mode = 0;
    public static int cycle = 0;
    public static int keyMode = 1;
    public static boolean enableEntities = true;
    public static boolean keyState = true;
    public static boolean rotateItems = true;
    public static boolean debug = false;
    public static double renderScaling = 1.0;

    public static final ArrayList<String> bannedTiles = new ArrayList<>();
    public static final ArrayList<String> bannedEntities = new ArrayList<>();
    public static final HashMap<String, String> nameOverrides = new HashMap<>();

    public Config(File file) {
        this.file = file;
        reload();
    }

    public void overrideBannedThings() {
        configuration
                .get(
                        HoloInventory.MODID,
                        "bannedTiles",
                        bannedTiles.toArray(new String[0]),
                        "Banned inventories.\n" + "Use the ingame command '/holoinventory' to change this list easily.")
                .set(bannedTiles.toArray(new String[0]));
        configuration
                .get(
                        HoloInventory.MODID,
                        "bannedEntities",
                        bannedEntities.toArray(new String[0]),
                        "Banned inventories.\n" + "Use the ingame command '/holoinventory' to change this list easily.")
                .set(bannedEntities.toArray(new String[0]));
        save();
    }

    public void reload() {
        configuration = new Configuration(file);
        doConfig();
    }

    public void overrideNameThings() {
        int i = 0;
        String[] things = new String[nameOverrides.size()];
        for (Map.Entry<String, String> entry : nameOverrides.entrySet()) {
            things[i] = '"' + entry.getKey() + '#' + entry.getValue() + '"';
            i++;
        }

        configuration
                .get(
                        HoloInventory.MODID,
                        "overrideNameThings",
                        things,
                        "Name overrides.\n" + "Use the ingame command '/holoinventory' to change this list easily.")
                .set(things);
        save();
    }

    public void doConfig() {
        configuration.addCustomCategoryComment(
                HoloInventory.MODID, "All our settings are in here, as you might expect...");

        renderScaling = configuration
                .get(HoloInventory.MODID, "renderScaling", renderScaling, "Visual scale factor (0.0-1.0)")
                .getDouble(1.0);

        //    requireGlasses = configuration.get(HoloInventory.MODID, "requireGlasses", requireGlasses,"Makes
        // HoloInventory require HoloGlasses").getBoolean(true);
        colorEnable = configuration
                .get(HoloInventory.MODID, "colorEnable", colorEnable, "Enable a BG color")
                .getBoolean(false);
        colorAlpha = configuration
                .get(HoloInventory.MODID, "colorAlpha", colorAlpha, "The BG transparancy (0-255)")
                .getInt();
        colorR = configuration
                .get(HoloInventory.MODID, "colorRed", colorR, "0-255")
                .getInt();
        colorG = configuration
                .get(HoloInventory.MODID, "colorGreen", colorG, "0-255")
                .getInt();
        colorB = configuration
                .get(HoloInventory.MODID, "colorBlue", colorB, "0-255")
                .getInt();
        keyMode = configuration
                .get(
                        HoloInventory.MODID,
                        "keyMode",
                        keyMode,
                        "Valid modes:\n" + "0: Always display hologram.\n"
                                + "1: The key toggles the rendering.\n"
                                + "2: Only render hologram while key pressed.\n"
                                + "3: Don't render hologram while key pressed.")
                .getInt();
        keyState = configuration
                .get(HoloInventory.MODID, "keyState", keyState, "Stores last toggle value. Don't worry about this.")
                .getBoolean(keyState);
        renderName = configuration
                .get(HoloInventory.MODID, "renderName", renderName, "Renders the inv name above the hologram")
                .getBoolean(true);
        renderText = configuration
                .get(HoloInventory.MODID, "renderText", renderText, "Render the stacksize as text on top of the items")
                .getBoolean(true);
        renderSuffixDarkened = configuration
                .get(
                        HoloInventory.MODID,
                        "renderSuffixDarkened",
                        renderSuffixDarkened,
                        "Render the stacksize suffix darkened")
                .getBoolean(true);
        renderMultiple = configuration
                .get(
                        HoloInventory.MODID,
                        "renderMultiple",
                        renderMultiple,
                        "Render multiple items depending on stacksize")
                .getBoolean(true);
        enableEntities = configuration
                .get(
                        HoloInventory.MODID,
                        "enableEntities",
                        enableEntities,
                        "Set to false to prevent all entities from rendering the hologram.")
                .getBoolean(true);
        hideItemsNotSelected = configuration
                .get(
                        HoloInventory.MODID,
                        "filterItemsByNEI",
                        hideItemsNotSelected,
                        "Filter items to render by the NEI search string (when focused)")
                .getBoolean(true);
        syncFreq = configuration
                .get(
                        HoloInventory.MODID,
                        "syncFreq",
                        syncFreq,
                        "Amout of seconds pass before sending a new update to the client looking at the chest.")
                .getInt();

        enableStacking = configuration
                .get(HoloInventory.MODID, "enableStacking", enableStacking, "Stack items, even above 64.")
                .getBoolean(enableStacking);

        mode = configuration
                .get(
                        HoloInventory.MODID,
                        "mode",
                        mode,
                        "Valid modes:\n" + "0: Default mode (Display all items).\n"
                                + "1: Sorting mode, biggest stack size first.\n"
                                + "2: Most abundant mode (Only display the item the most abundant in the chest.\n"
                                + "3: Same as 1, but with 3 items.\n"
                                + "4: Same as 1, but with 5 items.\n"
                                + "5: Same as 1, but with 7 items.\n"
                                + "6: Same as 1, but with 9 items.")
                .getInt();

        cycle = configuration
                .get(
                        HoloInventory.MODID,
                        "cycle",
                        cycle,
                        "Cycle trough all the items one by one. Set to the delay time wanted in ticks. If 0, cycle mode is off. Still takes into a count the mode.")
                .getInt();
        rotateItems = configuration
                .get(
                        HoloInventory.MODID,
                        "rotateItems",
                        rotateItems,
                        "Rotate the items in the hologram. Only works on fancy rendering.")
                .getBoolean(rotateItems);

        bannedTiles.clear();
        bannedTiles.addAll(Arrays.asList(configuration
                .get(
                        HoloInventory.MODID,
                        "bannedTiles",
                        bannedTiles.toArray(new String[0]),
                        "Banned inventories.\n" + "Use the ingame command '/holoinventory' to change this list easily.")
                .getStringList()));
        bannedEntities.clear();
        bannedEntities.addAll(Arrays.asList(configuration
                .get(
                        HoloInventory.MODID,
                        "bannedEntities",
                        bannedEntities.toArray(new String[0]),
                        "Banned inventories.\n" + "Use the ingame command '/holoinventory' to change this list easily.")
                .getStringList()));

        nameOverrides.clear();
        String[] things = configuration
                .get(
                        HoloInventory.MODID,
                        "overrideNameThings",
                        new String[0],
                        "Name overrides.\n" + "Use the ingame command '/holoinventory' to change this list easily.")
                .getStringList();
        for (String thing : things) {
            // Cut off " at end and begin
            thing = thing.substring(1, thing.length() - 1);
            String[] subThings = thing.split("#", 2);
            if (subThings.length != 2) continue;
            nameOverrides.put(subThings[0], subThings[1]);
        }

        debug = configuration.getBoolean(
                "debug", HoloInventory.MODID, debug, "Enable debug, use when errors or weird behaviour happens.");

        save();
    }

    public void save() {
        if (configuration.hasChanged()) configuration.save();
    }

    public void setKeyState(boolean keyState) {
        configuration
                .get(HoloInventory.MODID, "keyState", keyState, "Stores last toggle value. Don't worry about this.")
                .set(keyState);
        save();
    }
}
