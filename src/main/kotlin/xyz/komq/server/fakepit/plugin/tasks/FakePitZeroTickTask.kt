/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package xyz.komq.server.fakepit.plugin.tasks

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.ChatColor
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.getInstance
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.getTeamColor
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.initialKill
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.itemDrop
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.itemDropLocX
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.itemDropLocY
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.itemDropLocZ
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.netherStarOwner
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.playerTeamCount
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.sc
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.server
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.stopGame
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.winner

/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

class FakePitZeroTickTask: Runnable {
    override fun run() {
        val points = requireNotNull(sc.getObjective("Points"))

        server.onlinePlayers.forEach {
            if (!itemDrop) {
                if (initialKill == 1) {
                    it.sendActionBar(text("${ChatColor.AQUA}네더의 별 소유자: ${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${ChatColor.BOLD}${netherStarOwner.name}", NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
                }
            }
            else {
                it.sendActionBar(text("네더의 별 좌표 | X: ${itemDropLocX}, Y: ${itemDropLocY}, Z: $itemDropLocZ"))
            }

            if (points.getScore("${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${ChatColor.BOLD}${netherStarOwner.name}").score == 100) {
                stopGame()
                server.scheduler.runTaskTimer(getInstance(), FakePitEndTask(), 0L, 0L)
                winner = netherStarOwner
                server.scheduler.runTaskLater(getInstance(), Runnable { server.scheduler.cancelTasks(getInstance()); server.scheduler.runTaskTimer(getInstance(), FakePitConfigReloadTask(), 0L, 20L) }, 20 * 15L)
            }

            it.addPotionEffect(PotionEffect(PotionEffectType.SATURATION, 1000000, 255, false, false, false))
        }
    }
}
