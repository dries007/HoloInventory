package net.dries007.holoInventory;

import net.minecraftforge.common.Configuration;
import static net.dries007.holoInventory.util.Data.*;
import java.io.File;

public class Config
{
    final Configuration configuration;
    public Config(File file)
    {
        configuration = new Configuration(file);

        configuration.addCustomCategoryComment(MODID, "All our settings are in here, as you might expect...");

        configuration.save();
    }
}
