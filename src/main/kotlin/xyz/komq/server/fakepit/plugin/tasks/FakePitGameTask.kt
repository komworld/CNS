/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package xyz.komq.server.fakepit.plugin.tasks

import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.getTeamColor
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.hasNetherStar
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.isRunning
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.netherStarOwner
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.playerTeamCount
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.server

/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

class FakePitGameTask: Runnable {
    override fun run() {
        server.onlinePlayers.forEach {
            if (isRunning) {
                if (hasNetherStar[it.uniqueId] == true) {
                    val points = requireNotNull(it.scoreboard.getObjective("Points"))
                    points.getScore("${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${ChatColor.BOLD}${netherStarOwner.name}").score = points.getScore("${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${ChatColor.BOLD}${netherStarOwner.name}").score + 1
                    it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1000F, 2F)
                }
            }
        }
    }
}
