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

/**
 * DeveloperCapes by Jadar
 * License: MIT License (https://raw.github.com/jadar/DeveloperCapes/master/LICENSE)
 * version 2.1
 */
package com.jadarstudios.developercapes;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

/**
 * Wraps methods from DevCapes so it does not crash due to client-only class references.
 * Use this class to add a file URL instead of DevCapes to avoid a crash when starting a server.
 *
 * @author Jadar
 */
@SuppressWarnings("ALL")
public class DevCapesUtil
{

    /**
     * Wrapper for getting the DevCapes singleton.
     */
    @Deprecated
    public static DevCapes getInstance()
    {
        if (FMLCommonHandler.instance().getSide().equals(Side.CLIENT)) return DevCapes.getInstance();
        else
        {
            System.out.println(
                    "[SEVERE] [DevCapes] **Someone tried to call DevCapesUtil.getInstance() on a server! If you are attempting to add a file url then use DevCapesUtil.addFileUrl().**");
            return null;
        }
    }

    /**
     * Wrapper for setting up capes.<br>
     * This is recommended over <i>DevCapes.addFileUrl(String);</i><br>
     * *Will not run if on a server.*<p>
     * <p/>
     * Set up capes. All cape URLs are in the txt file passed in.<br>
     * <a href="https://github.com/jadar/DeveloperCapesAPI/blob/master/SampleCape.txt">Sample Cape Config</a>
     *
     * @param parTxtUrl The URL of the .txt file containing the groups, members of
     *                  said groups, and the group's cape URL.
     */
    public static void addFileUrl(String parTxtUrl)
    {
        if (FMLCommonHandler.instance().getSide().equals(Side.CLIENT)) getInstance().addFileUrl(parTxtUrl);
    }
}
