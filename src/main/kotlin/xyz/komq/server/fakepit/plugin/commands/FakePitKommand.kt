/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package xyz.komq.server.fakepit.plugin.commands

import io.github.monun.kommand.getValue
import io.github.monun.kommand.kommand
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title.Times.of
import net.kyori.adventure.title.Title.title
import org.bukkit.GameMode
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.scoreboard.DisplaySlot
import xyz.komq.server.fakepit.plugin.events.FakePitEvent
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.addTeam
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.administrators
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.getInstance
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.hasNetherStar
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.initialKill
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.isRunning
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.netherStarOwner
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.playerTeamCount
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.playingWorld
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.sc
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.server
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.setupArmors
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.setupScoreboards
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.stopGame
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.teamCount
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.unbreakableMeta
import xyz.komq.server.fakepit.plugin.tasks.FakePitGameTask
import xyz.komq.server.fakepit.plugin.tasks.FakePitZeroTickTask
import java.time.Duration.ofSeconds

/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

object FakePitKommand {
    val playerNameList = ArrayList<String>()

    fun fakePitKommand() {
        getInstance().kommand {
            register("fakepit") {
                requires { isOp }
                then("start") {
                    executes {
                        if (!isRunning) {
                            server.onlinePlayers.forEach { forEachP ->
                                forEachP.inventory.clear()
                                if (forEachP.uniqueId.toString() in administrators.toString()) {
                                    if (!getInstance().config.getBoolean("allow-admins-to-play")) {
                                        forEachP.gameMode = GameMode.SPECTATOR
                                        forEachP.sendMessage(text("관리자 -> GAMEMODE: SPECTATOR"))
                                    }
                                }
                                forEachP.teleport(Location(forEachP.world, 0.5, 72.5, 0.5))
                            }

                            val players = server.onlinePlayers.asSequence().filter {
                                it.gameMode != GameMode.SPECTATOR
                            }.toMutableList()

                            val sword = ItemStack(Material.STONE_SWORD)
                            val swordMeta = unbreakableMeta(sword.itemMeta)
                            sword.itemMeta = swordMeta

                            players.forEach {
                                playerNameList.add(it.name)
                                it.gameMode = GameMode.ADVENTURE
                                it.inventory.setItem(0, sword)
                            }

                            playerNameList.shuffle()
                            setupScoreboards()

                            FakePitGameContentManager.red?.color(NamedTextColor.RED)
                            FakePitGameContentManager.orange?.color(NamedTextColor.GOLD)
                            FakePitGameContentManager.yellow?.color(NamedTextColor.YELLOW)
                            FakePitGameContentManager.green?.color(NamedTextColor.GREEN)
                            FakePitGameContentManager.darkGreen?.color(NamedTextColor.DARK_GREEN)
                            FakePitGameContentManager.aqua?.color(NamedTextColor.AQUA)
                            FakePitGameContentManager.blue?.color(NamedTextColor.BLUE)
                            FakePitGameContentManager.purple?.color(NamedTextColor.DARK_PURPLE)
                            FakePitGameContentManager.white?.color(NamedTextColor.WHITE)
                            FakePitGameContentManager.gray?.color(NamedTextColor.GRAY)
                            FakePitGameContentManager.darkAqua?.color(NamedTextColor.DARK_AQUA)
                            FakePitGameContentManager.pink?.color(NamedTextColor.LIGHT_PURPLE)

                            sc.getObjective("Points")?.displaySlot = DisplaySlot.SIDEBAR
                            sc.getObjective("Health")?.displaySlot = DisplaySlot.BELOW_NAME

                            fun teamConfiguration() {
                                val teamPlayer = requireNotNull(server.getPlayer(playerNameList[teamCount]))

                                addTeam(playerNameList[teamCount], teamCount)
                                setupArmors(teamCount, teamPlayer)
                                playerTeamCount[teamPlayer.uniqueId] = teamCount
                                ++teamCount
                            }

                            while (teamCount != playerNameList.size) {
                                if (server.onlinePlayers.size != 12) {
                                    teamConfiguration()
                                }
                                else if (server.onlinePlayers.size >= 13 || server.onlinePlayers.size == 1){
                                    server.broadcast(text("The game could not be process because the server has more people than the maximum playable players.", NamedTextColor.RED))
                                    server.broadcast(text("Please turn some of the players into spectators. Otherwise the game would not work.", NamedTextColor.RED))
                                    server.broadcast(text("Minimum playable player count: 2 | Maximum playable player count: 12"))
                                    stopGame()
                                }
                            }

                            server.worlds.forEach {
                                it.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
                                it.setGameRule(GameRule.KEEP_INVENTORY, true)
                                it.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
                            }

                            server.pluginManager.registerEvents(FakePitEvent(), getInstance())
                            server.scheduler.runTaskTimer(getInstance(), FakePitGameTask(), 0L, 14L)
                            server.scheduler.runTaskTimer(getInstance(), FakePitZeroTickTask(), 0L, 0L)

                            val randomPlayer = server.onlinePlayers.toList()[0]

                            netherStarOwner = randomPlayer
                            playingWorld = randomPlayer.world
                            isRunning = true

                            var count = 0

                            server.scheduler.runTaskTimer(getInstance(), Runnable {
                                when(count++) {
                                    60 -> {
                                        if (initialKill == 0) {
                                            initialKill = 1
                                            randomPlayer.inventory.setItemInOffHand(ItemStack(Material.NETHER_STAR))
                                            randomPlayer.isGlowing = true
                                            hasNetherStar[randomPlayer.uniqueId] = true
                                            server.broadcast(text("1분동안 아무도 죽이지 않아 랜덤으로 네더의 별이 지급되었습니다!"))
                                            server.broadcast(text("${randomPlayer.name}님이 첫 네더의 별을 소유하고 있습니다!"))
                                        }
                                    }
                                    1200 -> {
                                        stopGame()
                                        server.broadcast(text("20분동안 아무도 승리 조건을 달성하지 못하여 게임이 강제 종료되었습니다!"))
                                        server.onlinePlayers.forEach {
                                            it.resetTitle()
                                            it.showTitle(title(text("게임 종료!", NamedTextColor.GOLD), text("무승부!", NamedTextColor.YELLOW), of(ofSeconds(0), ofSeconds(8), ofSeconds(0))))
                                        }
                                    }
                                }
                            }, 0L, 20L)

                            server.broadcast(text("Game Started."))
                        }
                    }
                }
                then("stop") {
                    executes {
                        if (isRunning) {
                            stopGame()
                            server.broadcast(text("Game Stopped."))
                        } else {
                            sender.sendMessage(text("The game is not running.", NamedTextColor.RED))
                        }
                    }
                }
                then("settings") {
                    then("allowAdminsToPlay") {
                        then("option" to bool()) {
                            executes {
                                val option: Boolean by it
                                if (option) {
                                    if (!isRunning) {
                                        getInstance().config.set("allow-admins-to-play", true)
                                        getInstance().saveConfig()
                                        sender.sendMessage(text("Allowing admins to play this game."))
                                    } else {
                                        sender.sendMessage(text("The game is still running! Please stop the game in order to change this configuration!", NamedTextColor.RED))
                                    }
                                } else {
                                    if (!isRunning) {
                                        getInstance().config.set("allow-admins-to-play", false)
                                        getInstance().saveConfig()
                                        sender.sendMessage(text("Disallowing admins to play this game."))
                                    } else {
                                        sender.sendMessage(text("The game is still running! Please stop the game in order to change this configuration!", NamedTextColor.RED))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}