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

package me.prdis.cns.plugin.objects

import me.prdis.cns.plugin.CNSPlugin
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criteria
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper
import org.incendo.cloud.paper.util.sender.Source
import java.util.*

/**
 * @author ContentManager
 */

@Suppress("UnstableApiUsage")
object CNSObject {
    val plugin = CNSPlugin.instance
    val server = plugin.server
    val world: World = server.worlds.first { it.environment == World.Environment.NORMAL }
    val sc = server.scoreboardManager.mainScoreboard

    private val executionCoordinator = ExecutionCoordinator.simpleCoordinator<Source>()
    val commandManager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
        .executionCoordinator(executionCoordinator)
        .buildOnEnable(plugin)

    val gamePlayers = mutableSetOf<UUID>()
    val spectators = mutableSetOf<UUID>()

    var minPlayers = 8
    var maxPlayers = 11

    var mapNum = 0

    val SPAWN_LOCATION
        get() = Location(world, 0.0, 70.0, 0.0).apply {
            when (mapNum) {
                1 -> {
                    this.add(0.5, 0.0, 0.5)
                    this.yaw = 0F
                    this.pitch = 0F
                }

                2 -> {
                    this.yaw = 90F
                    this.pitch = 0F
                }

                3, 4 -> {
                    this.add(0.5, 0.0, 0.5)
                    this.yaw = -90F
                    this.pitch = 0F
                }

                else -> {
                    this.add(0.5, 0.0, 0.5)
                    this.yaw = 0F
                    this.pitch = 0F
                }
            }
        }

    val Player.team
        get() = sc.teams.find { it.entries.contains(this.name) }

    val Player.color
        get() = team?.color()

    var isRunning = false

    var itemDropped = false
    var droppedStarEntity: Item? = null
    var initialKill = false

    var nsOwner: UUID? = null

    private val playerInternalDeaths = HashMap<UUID, Boolean>()
    var Player.internalDeath: Boolean
        get() = playerInternalDeaths[this.uniqueId] ?: false
        set(value) {
            playerInternalDeaths[this.uniqueId] = value
        }

    val pointsObjective
        get() = sc.getObjective("Points") ?: sc.registerNewObjective(
            "Points", Criteria.DUMMY, text("별 잡아라!", NamedTextColor.AQUA).decorate(
                TextDecoration.BOLD
            )
        )

    val healthObjective
        get() = sc.getObjective("Health") ?: sc.registerNewObjective(
            "Health", Criteria.HEALTH, text("♥", NamedTextColor.RED)
        )
}