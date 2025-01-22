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

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.world.block.BlockTypes
import io.papermc.paper.scoreboard.numbers.NumberFormat
import me.prdis.cns.plugin.events.CNSEvent
import me.prdis.cns.plugin.objects.CNSItemObject.NETHER_STAR
import me.prdis.cns.plugin.objects.CNSItemObject.equipArmor
import me.prdis.cns.plugin.objects.CNSItemObject.sword
import me.prdis.cns.plugin.objects.CNSObject.SPAWN_LOCATION
import me.prdis.cns.plugin.objects.CNSObject.color
import me.prdis.cns.plugin.objects.CNSObject.droppedStarEntity
import me.prdis.cns.plugin.objects.CNSObject.gamePlayers
import me.prdis.cns.plugin.objects.CNSObject.healthObjective
import me.prdis.cns.plugin.objects.CNSObject.initialKill
import me.prdis.cns.plugin.objects.CNSObject.isRunning
import me.prdis.cns.plugin.objects.CNSObject.itemDropped
import me.prdis.cns.plugin.objects.CNSObject.mapNum
import me.prdis.cns.plugin.objects.CNSObject.maxPlayers
import me.prdis.cns.plugin.objects.CNSObject.minPlayers
import me.prdis.cns.plugin.objects.CNSObject.nsOwner
import me.prdis.cns.plugin.objects.CNSObject.playerInternalDeaths
import me.prdis.cns.plugin.objects.CNSObject.plugin
import me.prdis.cns.plugin.objects.CNSObject.pointsObjective
import me.prdis.cns.plugin.objects.CNSObject.sc
import me.prdis.cns.plugin.objects.CNSObject.server
import me.prdis.cns.plugin.objects.CNSObject.spectators
import me.prdis.cns.plugin.objects.CNSObject.world
import me.prdis.cns.plugin.tasks.CNSTasks.setupTasks
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title.Times.times
import net.kyori.adventure.title.Title.title
import org.bukkit.*
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.DisplaySlot
import java.io.FileNotFoundException
import java.time.Duration.ofSeconds

/**
 * @author ContentManager
 */

object CNSImpl {
    fun startGame() {
        isRunning = true

        server.onlinePlayers.filter { it.gameMode != GameMode.SPECTATOR }.forEach {
            it.gameMode = GameMode.ADVENTURE
        }

        gamePlayers.addAll(server.onlinePlayers.filter { it.gameMode == GameMode.ADVENTURE }.map { it.uniqueId })
        if (gamePlayers.size !in minPlayers..maxPlayers) {
            server.broadcast(
                text(
                    "현재 접속한 플레이어 수가 최소 ${minPlayers}명 ~ 최대 ${maxPlayers}명을 만족하지 않습니다.",
                    NamedTextColor.RED
                )
            )
            server.broadcast(text("게임을 종료합니다."))
            return
        }

        gamePlayers.forEach { uuid ->
            val player = server.getPlayer(uuid)
            player?.let {
                pointsObjective.getScore(it).score = 0

                it.inventory.setItem(0, sword)
                it.teleportAsync(SPAWN_LOCATION)
                it.health = 19.0
                it.foodLevel = 20
                runTaskLater(1) { _ -> it.health = 20.0 }
            }
        }

        playerInternalDeaths.clear()
        
        teamConfiguration()

        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
        world.setGameRule(GameRule.KEEP_INVENTORY, true)
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)

        server.pluginManager.registerEvents(CNSEvent, plugin)
        setupTasks()

        world.pvp = true

        pointsObjective.displaySlot = DisplaySlot.SIDEBAR
        healthObjective.displaySlot = DisplaySlot.BELOW_NAME
    }

    fun stopGame() {
        isRunning = false

        server.scheduler.cancelTasks(plugin)
        HandlerList.unregisterAll(CNSEvent)

        nsOwner?.let { server.getPlayer(it)?.isGlowing = false }
        nsOwner = null
        itemDropped = false
        droppedStarEntity?.remove()
        droppedStarEntity = null
        initialKill = false

        world.pvp = false

        server.onlinePlayers.forEach {
            it.gameMode = GameMode.ADVENTURE
            it.inventory.clear()
            it.isGlowing = false
        }
        gamePlayers.clear()
        spectators.clear()
        playerInternalDeaths.clear()
        sc.teams.forEach { it.unregister() }

        pointsObjective.unregister()
        sc.getObjective("Health")?.unregister()
    }

    fun ending(winner: Player, onlyOneLeft: Boolean = false) {
        val origin = winner.location
        val loc1 = origin.clone().add(5.0, 3.0, 0.0)
        val loc2 = origin.clone().add(0.0, 3.0, 5.0)
        val loc3 = origin.clone().subtract(5.0, 0.0, 0.0).add(0.0, 3.0, 0.0)
        val loc4 = origin.clone().subtract(0.0, 0.0, 5.0).add(0.0, 3.0, 0.0)

        val colors = arrayListOf(Color.RED, Color.BLUE, Color.GREEN, Color.AQUA, Color.PURPLE, Color.FUCHSIA)

        val firework = FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(colors).build()

        var count = 0

        server.onlinePlayers.forEach {
            it.exp = 0F
            it.level = 0
        }

        val endingTask = runTaskTimer(0, 0) {
            when (count++) {
                0 -> {
                    server.onlinePlayers.forEach {
                        if (!onlyOneLeft) {
                            it.resetTitle()
                            it.showTitle(
                                title(
                                    text("게임 종료!", NamedTextColor.GOLD),
                                    text("${winner.name}님이 우승하셨습니다!", NamedTextColor.YELLOW),
                                    times(ofSeconds(0), ofSeconds(8), ofSeconds(0))
                                )
                            )
                        } else {
                            it.resetTitle()
                            it.showTitle(
                                title(
                                    text("게임 종료!", NamedTextColor.GOLD),
                                    text("모든 사람들이 나갔습니다!", NamedTextColor.YELLOW),
                                    times(ofSeconds(0), ofSeconds(8), ofSeconds(0))
                                )
                            )
                        }
                    }

                    server.onlinePlayers.forEach {
                        it.removePotionEffect(PotionEffectType.SATURATION)
                        it.playSound(it.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 1000F, 1F)
                    }
                }

                20 -> world.playFirework(loc1, firework)
                40 -> world.playFirework(loc2, firework)
                60 -> world.playFirework(loc3, firework)
                80 -> world.playFirework(loc4, firework)

                120 -> {
                    server.onlinePlayers.forEach {
                        it.playSound(it.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 1000F, 1F)
                    }
                    world.playFirework(loc1, firework)
                }

                140 -> world.playFirework(loc2, firework)
                160 -> world.playFirework(loc3, firework)
                180 -> world.playFirework(loc4, firework)

                220 -> {
                    server.onlinePlayers.forEach {
                        it.playSound(it.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 1000F, 1F)
                    }
                    world.playFirework(loc1, firework)
                }

                240 -> world.playFirework(loc2, firework)
                260 -> world.playFirework(loc3, firework)
                280 -> world.playFirework(loc4, firework)
            }
        }

        runTaskLater(340) { endingTask.cancel() }
    }

    private fun teamConfiguration() {
        val colorMaps = listOf(
            "White" to 0xFFFFFF,
            "Gray" to 0x808080,
            "Black" to 0x000000,
            "Red" to 0xFF0000,
            "Maroon" to 0x800000,
            "Yellow" to 0xFFFF00,
            "Olive" to 0x808000,
            "Lime" to 0x00FF00,
            "Green" to 0x008000,
            "Aqua" to 0x00FFFF,
            "Teal" to 0x008080,
            "Blue" to 0x0000FF,
            "Navy" to 0x000080,
            "Fuchsia" to 0xFF00FF,
            "Purple" to 0x800080,
            "Orange" to 0xFFA500
        )

        gamePlayers.forEachIndexed { index, uuid ->
            val player = server.getPlayer(uuid)
            player?.let {
                val team = sc.getTeam(colorMaps[index].first) ?: sc.registerNewTeam(colorMaps[index].first)
                team.addEntry(it.name)
                team.color(NamedTextColor.nearestTo(TextColor.color(colorMaps[index].second)))

                val score = pointsObjective.getScore(it)
                score.customName(text(player.name, player.color ?: NamedTextColor.WHITE))
                score.numberFormat(NumberFormat.styled(Style.style(NamedTextColor.RED)))

                it.equipArmor()
            }
        }
    }

    fun changeNSOwner(player: Player, previous: Player? = null) {
        previous?.let { prevOwner ->
            prevOwner.isGlowing = false

            val prevOwnerScore = pointsObjective.getScore(prevOwner.name)
            prevOwnerScore.customName(text(prevOwner.name, prevOwner.color!!))
            prevOwnerScore.numberFormat(
                NumberFormat.styled(
                    Style.style(NamedTextColor.RED).decoration(TextDecoration.BOLD, false)
                )
            )
        }

        player.isGlowing = true
        nsOwner = player.uniqueId

        val score = pointsObjective.getScore(player.name)

        score.customName(text(player.name, player.color!!).decorate(TextDecoration.BOLD))
        score.numberFormat(NumberFormat.styled(Style.style(NamedTextColor.RED, TextDecoration.BOLD)))

        player.inventory.setItem(EquipmentSlot.OFF_HAND, NETHER_STAR.clone())
        server.broadcast(
            text(player.name, player.color!!).append(
                text(
                    "님이${if (previous != null) "" else " 첫"} 네더의 별을 소유하고 있습니다!",
                    NamedTextColor.WHITE
                )
            )
        )
    }

    fun TextColor.toBukkitColor() = Color.fromRGB(this.value())

    fun runTaskLater(delay: Long, task: (BukkitRunnable) -> Unit): BukkitTask {
        return object : BukkitRunnable() {
            override fun run() {
                task(this)
            }
        }.runTaskLater(plugin, delay)
    }

    fun runTaskTimer(delay: Long, period: Long, task: (BukkitRunnable) -> Unit): BukkitTask {
        return object : BukkitRunnable() {
            override fun run() {
                task(this)
            }
        }.runTaskTimer(plugin, delay, period)
    }

    private fun World.playFirework(location: Location, effect: FireworkEffect) {
        this.spawn(location, Firework::class.java).apply {
            fireworkMeta = this.fireworkMeta.apply {
                addEffect(effect)
            }
        }
    }

    fun changeMap(num: Int) {
        val filename = "map_${num}"
        val fileResource = this::class.java.classLoader.getResourceAsStream("${filename}.schem")
            ?: throw FileNotFoundException("Schematic $filename not found")
        val reader = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getReader(fileResource)
            ?: throw NullPointerException("Reader is null")
        val clipboard = reader.read() ?: throw NullPointerException("Clipboard is null")
        val adaptedWorld = BukkitAdapter.adapt(world)
        val editSession = WorldEdit.getInstance().newEditSession(adaptedWorld)
        val region = CuboidRegion(adaptedWorld, BlockVector3.at(-66, 16, 66), BlockVector3.at(64, 132, -103))
        val pasteOperation = ClipboardHolder(clipboard).createPaste(editSession).to(BlockVector3.at(0.5, 70.0, 0.0))
            .ignoreAirBlocks(true).build()

        editSession.setBlocks(region, BlockTypes.AIR!!.defaultState)
        Operations.complete(pasteOperation)
        editSession.close()

        mapNum = num
        plugin.config.set("mapnum", num)
        plugin.saveConfig()
    }
}