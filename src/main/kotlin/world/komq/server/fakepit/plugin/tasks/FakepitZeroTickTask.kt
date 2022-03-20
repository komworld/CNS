/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package world.komq.server.fakepit.plugin.tasks

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.ChatColor
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.initialKill
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.itemDrop
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.itemDropLocX
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.itemDropLocY
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.itemDropLocZ
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.nsOwner
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.playerTeamColor
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.plugin
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.sc
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.server
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.stopGame
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.trackItem
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.winner

/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

class FakepitZeroTickTask: Runnable {
    override fun run() {
        server.onlinePlayers.forEach {
            if (!itemDrop) {
                if (initialKill == 1) {
                    it.sendActionBar(text("${ChatColor.AQUA}네더의 별 소유자: ${playerTeamColor[nsOwner.uniqueId]}${ChatColor.BOLD}${nsOwner.name}", NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
                }
            }
            else {
                itemDropLocX = trackItem?.location?.x?.toInt() ?: 0
                itemDropLocY = trackItem?.location?.y?.toInt() ?: 0
                itemDropLocZ = trackItem?.location?.z?.toInt() ?: 0
                it.sendActionBar(text("네더의 별 좌표 | X: $itemDropLocX, Y: $itemDropLocY, Z: $itemDropLocZ"))
            }

            if (sc.getObjective("Points")?.getScore("${playerTeamColor[nsOwner.uniqueId]}${ChatColor.BOLD}${nsOwner.name}")?.score == 100) {
                stopGame()
                server.scheduler.runTaskTimer(plugin, FakepitEndTask(), 0L, 0L)
                winner = nsOwner
            }

            it.addPotionEffect(PotionEffect(PotionEffectType.SATURATION, 1000000, 255, false, false, false))
        }
    }
}
