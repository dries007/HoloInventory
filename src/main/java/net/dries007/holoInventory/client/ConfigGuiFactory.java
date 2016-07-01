package net.dries007.holoInventory.client;

import net.dries007.holoInventory.HoloInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConfigGuiFactory implements IModGuiFactory
{
    public static class ConfigGuiScreen extends GuiConfig
    {
        public ConfigGuiScreen(GuiScreen parentScreen)
        {
            super(parentScreen, getConfigElements(), HoloInventory.MODID, false, false, HoloInventory.MODID);
        }

        private static List<IConfigElement> getConfigElements()
        {
            Configuration c = HoloInventory.getConfig();
            if (c.getCategoryNames().size() == 1)
            {
                //noinspection LoopStatementThatDoesntLoop
                for (String k : c.getCategoryNames())
                {
                    // Let forge do the work, for loop abused to avoid other strange constructs
                    return new ConfigElement(c.getCategory(k)).getChildElements();
                }
            }

            List<IConfigElement> list = new ArrayList<>();
            for (String k : c.getCategoryNames())
            {
                list.add(new ConfigElement(c.getCategory(k)));
            }
            return list;
        }
    }

    @Override
    public void initialize(Minecraft minecraftInstance)
    {

    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass()
    {
        return ConfigGuiScreen.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
    {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element)
    {
        return null;
    }
}
