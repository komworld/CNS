/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package xyz.komq.server.fakepit.plugin.tasks

import io.github.monun.tap.effect.playFirework
import org.bukkit.*
import org.bukkit.potion.PotionEffectType
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.onlyOne
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.server
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.titleFunc
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.winner

/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

class FakePitEndTask: Runnable {
    private var count = 0

    override fun run() {
        val winnerPlayer = requireNotNull(server.getPlayer(winner))
        val loc1 = Location(winnerPlayer.world, 25.0, 82.0, 15.0)
        val loc2 = Location(winnerPlayer.world, 13.0, 82.0, 23.0)
        val loc3 = Location(winnerPlayer.world, -5.0, 82.0, 14.0)
        val loc4 = Location(winnerPlayer.world, -17.0, 82.0, 19.0)
        val loc5 = Location(winnerPlayer.world, -17.0, 82.0, 2.0)
        val loc6 = Location(winnerPlayer.world, -18.0, 82.0, -15.0)
        val loc7 = Location(winnerPlayer.world, 3.0, 82.0, -18.0)
        val loc8 = Location(winnerPlayer.world, 20.0, 82.0, -19.0)
        val loc9 = Location(winnerPlayer.world, 25.0, 82.0, -1.0)
        val loc10 = Location(winnerPlayer.world, 1.0, 82.0, -1.0)
        val loc11 = Location(winnerPlayer.world, -8.0, 82.0, 12.0)
        val loc12 = Location(winnerPlayer.world, 9.0, 82.0, -7.0)


        val colors = arrayListOf(
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.AQUA,
            Color.PURPLE,
            Color.FUCHSIA
        )

        val firework = FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(colors).build()

        server.onlinePlayers.forEach {
            when (count++) {
                0 -> {
                    if (!onlyOne) {
                        titleFunc(false)
                    }
                    else {
                        titleFunc(true)
                    }
                    it.removePotionEffect(PotionEffectType.SATURATION)
                    it.playSound(it.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 1000F, 1F)
                }

                20 -> {
                    it.world.playFirework(loc1, firework)
                }
                40 -> {
                    it.world.playFirework(loc2, firework)
                }
                60 -> {
                    it.world.playFirework(loc3, firework)
                }
                80 -> {
                    it.world.playFirework(loc4, firework)
                }

                120 -> {
                    it.playSound(it.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 1000F, 1F)
                    it.world.playFirework(loc5, firework)
                }
                140 -> {
                    it.world.playFirework(loc6, firework)
                }
                160 -> {
                    it.world.playFirework(loc7, firework)
                }
                180 -> {
                    it.world.playFirework(loc8, firework)
                }

                220 -> {
                    it.playSound(it.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 1000F, 1F)
                    it.world.playFirework(loc9, firework)
                }
                240 -> {
                    it.world.playFirework(loc10, firework)
                }
                260 -> {
                    it.world.playFirework(loc11, firework)
                }
                280 -> {
                    it.world.playFirework(loc12, firework)
                }
            }
        }
    }
}