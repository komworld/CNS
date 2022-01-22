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
import org.bukkit.GameMode
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.scoreboard.DisplaySlot
import xyz.komq.server.fakepit.plugin.events.FakePitEvent
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.addTeam
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.administrators
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.getInstance
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.isRunning
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.netherStarOwner
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.playerTeamCount
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.sc
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.server
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.setupArmors
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.setupScoreboards
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.stopGame
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.teamCount
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.unbreakableMeta
import xyz.komq.server.fakepit.plugin.tasks.FakePitGameTask
import xyz.komq.server.fakepit.plugin.tasks.FakePitZeroTickTask

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
                                forEachP.teleport(Location(forEachP.world, 0.5, 75.5, 0.5))
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

                            server.broadcast(text("DEBUG: $players"))

                            playerNameList.shuffle()
                            setupScoreboards()
                            sc.getObjective("Points")?.displaySlot = DisplaySlot.SIDEBAR

                            fun teamConfiguration() {
                                val teamPlayer = requireNotNull(server.getPlayer(playerNameList[teamCount]))

                                addTeam(playerNameList[teamCount], teamCount)
                                setupArmors(teamCount, teamPlayer)
                                playerTeamCount[teamPlayer.uniqueId] = teamCount
                                ++teamCount
                            }

                            while (teamCount != playerNameList.size) {
                                if (playerNameList.size < 13) {
                                    teamConfiguration()
                                }
                                else {
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
                            isRunning = true
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