/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package xyz.komq.server.fakepit.plugin.commands

import io.github.monun.kommand.kommand
import net.kyori.adventure.text.Component.text
import org.bukkit.GameRule
import org.bukkit.Material
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack
import xyz.komq.server.fakepit.plugin.events.FakePitEvent
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.addTeam
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.gameTaskId
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.getInstance
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.initialKill
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.playerTeamCount
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.sc
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.server
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.setupArmors
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.setupTeams
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.teamCount
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.unbreakableMeta
import xyz.komq.server.fakepit.plugin.tasks.FakePitGameTask


/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

object FakePitKommand {

    private val playerNameList = ArrayList<String>()

    fun fakePitKommand() {
        getInstance().kommand {
            register("fakepit") {
                requires { isOp }
                then("start") {
                    executes {
                        server.onlinePlayers.forEach {
                            playerNameList.add(it.name)

                            val sword = ItemStack(Material.STONE_SWORD)
                            val swordMeta = unbreakableMeta(sword.itemMeta)
                            sword.itemMeta = swordMeta

                            it.inventory.setItemInMainHand(sword)
                            it.inventory.addItem(ItemStack(Material.BREAD, 16))
                        }

                        playerNameList.shuffle()
                        setupTeams()

                        for (name in playerNameList) {
                            val teamPlayer = server.getPlayer(name[teamCount].toString())

                            addTeam(playerNameList[teamCount], teamCount)
                            setupArmors(teamCount, teamPlayer)
                            if (teamPlayer != null) {
                                playerTeamCount[teamPlayer.uniqueId] = teamCount
                            }

                            if (teamCount != playerNameList.size) {
                                ++teamCount
                            }
                        }

                        val teamArray = ArrayList<String>()

                        server.worlds.forEach {
                            it.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
                        }

                        server.onlinePlayers.forEach {
                            teamArray.add(it.scoreboard.teams.toString())
                        }

                        server.broadcast(text("DEBUG: $teamArray"))
                        server.broadcast(text("DEBUG: $teamCount"))

                        server.pluginManager.registerEvents(FakePitEvent(), getInstance())
                        val gameTask = server.scheduler.runTaskTimer(getInstance(), FakePitGameTask(), 0L, 0L)
                        gameTaskId = gameTask.taskId
                        server.broadcast(text("Game Started."))
                    }
                }
                then("stop") {
                    executes {
                        HandlerList.unregisterAll(getInstance())
                        server.scheduler.cancelTask(gameTaskId)
                        playerNameList.clear()
                        sc.teams.forEach { it.unregister() }
                        teamCount = 0
                        initialKill = 0
                        server.worlds.forEach {
                            it.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
                        }
                        server.broadcast(text("Game Stopped."))
                    }
                }
            }
        }
    }
}