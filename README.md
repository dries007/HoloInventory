# HoloInventory

Adds Holographic inventory screen for all blocks with an inventory.

[Click for the latest download.](http://jenkins.dries007.net/job/HoloInventory/)

Based on idea of [this reddit thread](http://www.reddit.com/r/Minecraft/comments/1prvo4) by [aleqsio](http://www.reddit.com/user/aleqsio).

(c) Copyright  Dries007.net 2013

## Setup dev env:

- Install gradle
- Run "gradle setupDevWorkspace" in the git repo (some steps seem to take forever, namely getAssets)
- Import folder with Intellij or Eclipse
- Make changes
- Use the run configurations below
- Run "gradle build" to make a jar, see "build/libs" for output
- Test your changes
- Make a PR!

## Intellij run configurations:
Don't forget to make "minecraft/jars".

### Client:
- net.minecraft.launchwrapper.Launch
- -Djava.library.path=../natives -XX:-UseSplitVerifier
- --version 1.6 --tweakClass cpw.mods.fml.common.launcher.FMLTweaker --username Dries007
- $MODULE_DIR$/minecraft/jars

### Server:
- cpw.mods.fml.relauncher.ServerLaunchWrapper
- -XX:-UseSplitVerifier
- (empty)
- $MODULE_DIR$/minecraft/jars
