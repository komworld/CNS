/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package world.komq.server.fakepit.plugin.commands

import io.github.monun.kommand.getValue
import io.github.monun.kommand.node.LiteralNode
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.isRunning
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.plugin
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.startGame
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.stopGame

/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

object FakepitKommand {
    fun register(builder: LiteralNode) {
        builder.apply {
            requires { isOp }
            then("start") {
                executes {
                    if (!isRunning) startGame()
                }
            }
            then("stop") {
                executes {
                    if (isRunning) stopGame()
                }
            }
            then("settings") {
                then("nonproduction") {
                    then("option" to bool()) {
                        executes {
                            val option: Boolean by it
                            if (option) {
                                if (!isRunning) {
                                    plugin.config.set("nonproduction", true)
                                    plugin.saveConfig()
                                    sender.sendMessage(text("Non-production: true"))
                                }
                                else {
                                    sender.sendMessage(text("The game is still running!", NamedTextColor.RED))
                                }
                            } else {
                                if (!isRunning) {
                                    plugin.config.set("nonproduction", false)
                                    plugin.saveConfig()
                                    sender.sendMessage(text("Non-production: false"))
                                }
                                else {
                                    sender.sendMessage(text("The game is still running!", NamedTextColor.RED))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}