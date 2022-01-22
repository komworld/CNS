/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package xyz.komq.server.fakepit.plugin

import org.bukkit.GameRule
import org.bukkit.plugin.java.JavaPlugin
import xyz.komq.server.fakepit.plugin.commands.FakePitKommand.fakePitKommand
import xyz.komq.server.fakepit.plugin.config.FakePitConfig.load
import xyz.komq.server.fakepit.plugin.tasks.FakePitConfigReloadTask
import java.io.File

/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

class FakePitPluginMain : JavaPlugin() {

    companion object {
        lateinit var instance: FakePitPluginMain
            private set
    }

    private val configFile = File(dataFolder, "config.yml")

    override fun onEnable() {
        instance = this
        load(configFile)
        server.scheduler.runTaskTimer(this, FakePitConfigReloadTask(), 0L, 20L)
        fakePitKommand()
    }

    override fun onDisable() {
        server.onlinePlayers.forEach {
            server.scoreboardManager.mainScoreboard.resetScores(it.name)
        }
        server.worlds.forEach {
            it.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false)
            it.setGameRule(GameRule.KEEP_INVENTORY, false)
        }
        server.scoreboardManager.mainScoreboard.objectives.forEach { it.unregister() }
        server.scoreboardManager.mainScoreboard.teams.forEach { it.unregister() }
    }
}