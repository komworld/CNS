/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package xyz.komq.server.fakepit.plugin.commands

import io.github.monun.kommand.getValue
import io.github.monun.kommand.node.LiteralNode
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.isRunning
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.playable
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.plugin
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.server
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.startGame
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.stopGame

/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

object FakePitKommand {
    fun register(builder: LiteralNode) {
        builder.apply {
            requires { isOp }
            then("start") {
                executes {
                    if (!isRunning) {
                        startGame()
                        if (playable) {
                            server.broadcast(text("Game Started."))
                        }
                    }
                    else {
                        sender.sendMessage(text("The game is not running.", NamedTextColor.RED))
                    }
                }
            }
            then("stop") {
                executes {
                    if (isRunning) {
                        stopGame()
                        server.broadcast(text("Game Stopped."))
                    }
                    else {
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
                                    plugin.config.set("allow-admins-to-play", true)
                                    plugin.saveConfig()
                                    sender.sendMessage(text("Allowing admins to play this game."))
                                }
                                else {
                                    sender.sendMessage(text("The game is still running! Please stop the game in order to change this configuration!", NamedTextColor.RED))
                                }
                            } else {
                                if (!isRunning) {
                                    plugin.config.set("allow-admins-to-play", false)
                                    plugin.saveConfig()
                                    sender.sendMessage(text("Disallowing admins to play this game."))
                                }
                                else {
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