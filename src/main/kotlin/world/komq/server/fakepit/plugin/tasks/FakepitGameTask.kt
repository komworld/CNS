/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package world.komq.server.fakepit.plugin.tasks

import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.hasNetherStar
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.isRunning
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.nsOwner
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.playerTeamColor
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.server

/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

class FakepitGameTask: Runnable {
    override fun run() {
        server.onlinePlayers.forEach {
            if (isRunning) {
                if (hasNetherStar[it.uniqueId] == true) {
                    val points = requireNotNull(it.scoreboard.getObjective("Points"))
                    points.getScore("${playerTeamColor[nsOwner.uniqueId]}${ChatColor.BOLD}${nsOwner.name}").score = points.getScore("${playerTeamColor[nsOwner.uniqueId]}${ChatColor.BOLD}${nsOwner.name}").score + 1
                    it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1000F, 2F)
                }
            }
        }
    }
}