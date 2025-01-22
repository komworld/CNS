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

package me.prdis.cns.plugin.commands

import me.prdis.cns.plugin.objects.CNSImpl.changeMap
import me.prdis.cns.plugin.objects.CNSImpl.startGame
import me.prdis.cns.plugin.objects.CNSImpl.stopGame
import me.prdis.cns.plugin.objects.CNSObject.commandManager
import me.prdis.cns.plugin.objects.CNSObject.isRunning
import me.prdis.cns.plugin.objects.CNSObject.maxPlayers
import me.prdis.cns.plugin.objects.CNSObject.minPlayers
import me.prdis.cns.plugin.objects.CNSObject.plugin
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.incendo.cloud.parser.standard.IntegerParser

object CNSCommand {
    fun registerCommands() {
        registerCNS()
    }

    private fun registerCNS() {
        val cns = commandManager.commandBuilder("cns", { "A CNS Game command." }, "별잡아라")

        commandManager.command(cns.handler { ctx ->
            ctx.sender().source().sendMessage(
                text(
                    """
                별 잡아라 (CNS)
                Paradise Dev Team (구 Komtents, Komworld Dev Team)
                
                /cns start - 게임 시작
                /cns stop - 게임 종료
                /cns players <최소 플레이어 수> <최대 플레이어 수> - 플레이어 수 변경
                
                사용자가 지정한 플레이어 수는 설정 파일에 저장되며 매 서버 재시작시 자동으로 불러옵니다.
                설정할 수 있는 최소 플레이어 수는 2명으로 강제됩니다.
                설정할 수 있는 최대 플레이어 수는 17명으로 강제됩니다.
            """.trimIndent()
                )
            )
        })

        commandManager.command(cns.literal("start").handler { ctx ->
            if (!isRunning) {
                ctx.sender().source().sendMessage(text("게임을 시작합니다."))
                startGame()
            } else {
                ctx.sender().source().sendMessage(text("게임이 이미 시작되었습니다.", NamedTextColor.RED))
            }
        })

        commandManager.command(cns.literal("stop").handler { ctx ->
            if (isRunning) {
                ctx.sender().source().sendMessage(text("게임을 종료합니다."))
                stopGame()
            } else {
                ctx.sender().source().sendMessage(text("게임이 이미 종료된 상태입니다.", NamedTextColor.RED))
            }
        })

        commandManager.command(
            cns.literal("players").required("min", IntegerParser.integerParser(2, 17))
                .required("max", IntegerParser.integerParser(2, 17)).handler { ctx ->
                    val min = ctx.get<Int>("min")
                    val max = ctx.get<Int>("max")

                    minPlayers = min
                    maxPlayers = max

                    plugin.config.set("minPlayers", min)
                    plugin.config.set("maxPlayers", max)
                    plugin.saveConfig()

                    if (min > max) {
                        ctx.sender().source()
                            .sendMessage(text("설정의 최소 플레이어가 최대 플레이어보다 작거나 같지 않습니다.", NamedTextColor.RED))
                        return@handler
                    }

                    ctx.sender().source().sendMessage(text("게임의 최소 플레이어 수를 ${min}명, 최대 플레이어 수를 ${max}명으로 설정했습니다."))
                })

        commandManager.command(
            cns.literal("map").required("num", IntegerParser.integerParser(1, 4)).handler { ctx ->
                val num = ctx.get<Int>("num")

                ctx.sender().source().sendMessage(
                    text(
                        """
                    ${num}번 맵을 불러옵니다.
                    ※ 맵을 변경하는 동안 컴퓨터 사양에 따라 서버가 멈추고 WorldEdit Schematic을 불러오는 시간이 소요 될 수 있습니다.
                """.trimIndent()
                    )
                )

                try {
                    changeMap(num)
                    ctx.sender().source().sendMessage(text("성공적으로 ${num}번 맵을 불러왔습니다."))
                } catch (e: Exception) {
                    ctx.sender().source().sendMessage(
                        text(
                            "Schemtaic 파일을 불러오는데 실패하였습니다. ",
                            NamedTextColor.RED
                        ).append(
                            text("[오류]").decorate(TextDecoration.BOLD).hoverEvent(text(e.message ?: "오류 메시지가 존재하지 않음."))
                        )
                    )
                }
            }
        )
    }
}