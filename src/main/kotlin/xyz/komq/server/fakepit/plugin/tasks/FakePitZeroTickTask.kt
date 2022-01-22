/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package xyz.komq.server.fakepit.plugin.tasks

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.getInstance
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.hasNetherStar
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.initialKill
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.itemDrop
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.itemDropLocX
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.itemDropLocY
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.itemDropLocZ
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.netherStarOwner
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.server
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.stopGame

/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

class FakePitZeroTickTask: Runnable {
    override fun run() {
        server.onlinePlayers.forEach {
            if (hasNetherStar[it.uniqueId] == true) {
                it.sendActionBar(text("네더의 별을 가지고 있습니다!", NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
            }
            else {
                if (!itemDrop) {
                    if (initialKill == 1) {
                        it.sendActionBar(text("네더의 별 소유자: ${netherStarOwner.name}", NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
                    }
                }
                else {
                    it.sendActionBar(text("네더의 별 좌표 - X: ${itemDropLocX}, Y: ${itemDropLocY}, Z: $itemDropLocZ"))
                }
            }

            if (it.scoreboard.getObjective("Points")?.getScore(it.name)?.score == 200) {
                stopGame()
                it.sendMessage("우승자: ${it.name}")
                val gameEndTask = server.scheduler.runTaskTimer(getInstance(), FakePitEndTask(), 0L, 20L)
                server.scheduler.runTaskLater(getInstance(), Runnable { server.scheduler.cancelTask(gameEndTask.taskId) }, 20 * 15L)
            }
        }
    }
}
