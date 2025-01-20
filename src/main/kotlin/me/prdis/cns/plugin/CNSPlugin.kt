/*
 * Copyright (C) 2025 Paradise Dev Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package me.prdis.cns.plugin

import me.prdis.cns.plugin.commands.CNSCommand.registerCommands
import me.prdis.cns.plugin.config.CNSConfig.checkConfig
import me.prdis.cns.plugin.events.CNSMapProtection
import me.prdis.cns.plugin.objects.CNSObject.maxPlayers
import me.prdis.cns.plugin.objects.CNSObject.minPlayers
import me.prdis.cns.plugin.objects.CNSObject.world
import org.bukkit.Difficulty
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * @author ContentManager
 */

class CNSPlugin : JavaPlugin() {
    companion object {
        lateinit var instance: CNSPlugin
            private set
    }

    override fun onEnable() {
        instance = this

        val configFile = File(dataFolder, "config.yml")

        if (!configFile.exists()) {
            config.set("minPlayers", minPlayers)
            config.set("maxPlayers", maxPlayers)
            saveConfig()
        }

        checkConfig()

        world.pvp = false
        world.difficulty = Difficulty.NORMAL

        registerCommands()
        server.pluginManager.registerEvents(CNSMapProtection, this)
    }
}