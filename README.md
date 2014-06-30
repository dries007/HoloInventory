# HoloInventory

Adds Holographic inventory screen for all blocks with an inventory.

[Screenshots & Downloads](http://www.dries007.net/holoinventory/)
Make sure to check the config file for options!

Based on idea of [this reddit thread](http://www.reddit.com/r/Minecraft/comments/1prvo4) by [aleqsio](http://www.reddit.com/user/aleqsio).

If you find spelling mistakes, let me know!

(c) Copyright  Dries007.net 2013

### Donate
If you want to donate to me (dries007), you can [paypal](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=M6XDAP29UDX7Q) or [patreon](http://www.patreon.com/dries007) me.

### Video makers:
My name is Dutch, that means the "ie" is pronounced like a long i. [Click for an example, thanks Google.](http://translate.google.com/#nl/en/Driees)
Make sure you use the latest version of the mod (and mention wich one you are using), and that you have a like (to this page or the MCForums) in the video discription.

### Modpacks:
All modpacks can distribute this mod on one condition: You use the official versions (provided by the download link above).

### Setup dev env:

- Install gradle
- Run "gradle setupDevWorkspace" in the git repo (some steps seem to take forever, namely getAssets)
- Import folder with Intellij or Eclipse
- Make changes
- Use the run configurations below
- Run "gradle build" to make a jar, see "build/libs" for output
- Test your changes
- Make a PR!

### Intellij run configurations:
Don't forget to make "minecraft/jars".

#### Client:
- net.minecraft.launchwrapper.Launch
- -Djava.library.path=../natives -XX:-UseSplitVerifier
- --version 1.6 --tweakClass cpw.mods.fml.common.launcher.FMLTweaker --username Dries007
- $MODULE_DIR$/minecraft/jars

#### Server:
- cpw.mods.fml.relauncher.ServerLaunchWrapper
- -XX:-UseSplitVerifier
- (empty)
- $MODULE_DIR$/minecraft/jars
