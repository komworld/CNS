/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package xyz.komq.server.fakepit.plugin

import io.github.monun.kommand.kommand
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import xyz.komq.server.fakepit.plugin.commands.FakePitKommand.register
import xyz.komq.server.fakepit.plugin.config.FakePitConfig.load
import xyz.komq.server.fakepit.plugin.events.FakePitEvent
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.event
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.world
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
        event = FakePitEvent()

        load(configFile)
        server.scheduler.runTaskTimer(this, FakePitConfigReloadTask(), 0L, 20L)
        server.messenger.registerOutgoingPluginChannel(this, "BungeeCord")
        world.pvp = false

        kommand {
            register("fakepit") {
                register(this)
            }
        }
    }

    override fun onDisable() {
        HandlerList.unregisterAll(event)
        server.scoreboardManager.mainScoreboard.objectives.forEach { it.unregister() }
        server.scoreboardManager.mainScoreboard.teams.forEach { it.unregister() }
        server.messenger.unregisterOutgoingPluginChannel(this, "BungeeCord")
    }
}