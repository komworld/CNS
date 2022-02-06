/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package xyz.komq.server.fakepit.plugin.tasks

import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.plugin
import java.io.File

/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

class FakePitConfigReloadTask: Runnable {
    private val configFile = File(plugin.dataFolder, "config.yml")

    private var configFileLastModified = configFile.lastModified()

    override fun run() {
        if (configFileLastModified != configFile.lastModified()) {
            plugin.logger.info("Config Reloaded.")
            plugin.reloadConfig()
            plugin.saveConfig()

            configFileLastModified = configFile.lastModified()
        }
    }
}