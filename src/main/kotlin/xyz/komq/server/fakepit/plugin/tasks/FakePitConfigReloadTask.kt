/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package xyz.komq.server.fakepit.plugin.tasks

import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.getInstance
import java.io.File

/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

class FakePitConfigReloadTask: Runnable {
    private val configFile = File(getInstance().dataFolder, "config.yml")

    private var configFileLastModified = configFile.lastModified()

    override fun run() {
        if (configFileLastModified != configFile.lastModified()) {
            getInstance().logger.info("Config Reloaded.")
            getInstance().reloadConfig()
            getInstance().saveConfig()

            configFileLastModified = configFile.lastModified()
        }
    }
}