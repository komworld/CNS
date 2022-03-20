/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package world.komq.server.fakepit.plugin

import io.github.monun.kommand.kommand
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import world.komq.server.fakepit.plugin.commands.FakepitKommand.register
import world.komq.server.fakepit.plugin.config.FakepitConfig.load
import world.komq.server.fakepit.plugin.events.FakepitEvent
import world.komq.server.fakepit.plugin.events.FakepitMapProtectEvent
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.event
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.mapProtectEvent
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.world
import world.komq.server.fakepit.plugin.tasks.FakepitConfigReloadTask
import java.io.File

/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

class FakepitPluginMain : JavaPlugin() {

    companion object {
        lateinit var instance: FakepitPluginMain
            private set
    }

    private val configFile = File(dataFolder, "config.yml")

    override fun onEnable() {
        instance = this
        event = FakepitEvent()
        mapProtectEvent = FakepitMapProtectEvent()

        load(configFile)
        server.scheduler.runTaskTimer(this, FakepitConfigReloadTask(), 0L, 20L)
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