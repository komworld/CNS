/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package xyz.komq.server.fakepit.plugin.tasks

import io.github.monun.tap.effect.playFirework
import net.kyori.adventure.text.Component.text
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Sound
import org.bukkit.SoundCategory
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
        val loc1 = winnerPlayer.location.add(5.0, 0.0, 0.0)
        val loc2 = winnerPlayer.location.add(0.0, 0.0, 5.0)
        val loc3 = winnerPlayer.location.subtract(5.0, 0.0, 0.0)
        val loc4 = winnerPlayer.location.subtract(0.0, 0.0, 5.0)

        val colors = arrayListOf(
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.AQUA
        )

        val firework = FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(colors).build()

        for (onlinePlayers in server.onlinePlayers) {
            when (count++) {
                0 -> {
                    // TODO: 폭죽 터지는 위치를 플레이어의 위치에서 맵 중앙 근처 위치로 변경
                    // TODO: 폭죽 한개에 다양한 색 추가

                    if (!onlyOne) {
                        titleFunc(false)
                    }
                    else {
                        titleFunc(true)
                    }
                    server.broadcast(text(winner))
                    onlinePlayers.removePotionEffect(PotionEffectType.SATURATION)
                    onlinePlayers.playSound(onlinePlayers.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 1000F, 1F)
                }

                20 -> {
                    onlinePlayers.world.playFirework(loc1, firework)
                }
                40 -> {
                    onlinePlayers.world.playFirework(loc2, firework)
                }
                60 -> {
                    onlinePlayers.world.playFirework(loc3, firework)
                }
                80 -> {
                    onlinePlayers.world.playFirework(loc4, firework)
                }

                120 -> {
                    onlinePlayers.playSound(onlinePlayers.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 1000F, 1F)
                    onlinePlayers.world.playFirework(loc1, firework)
                }
                140 -> {
                    onlinePlayers.world.playFirework(loc2, firework)
                }
                160 -> {
                    onlinePlayers.world.playFirework(loc3, firework)
                }
                180 -> {
                    onlinePlayers.world.playFirework(loc4, firework)
                }

                220 -> {
                    onlinePlayers.playSound(onlinePlayers.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 1000F, 1F)
                    onlinePlayers.world.playFirework(loc1, firework)
                }
                240 -> {
                    onlinePlayers.world.playFirework(loc2, firework)
                }
                260 -> {
                    onlinePlayers.world.playFirework(loc3, firework)
                }
                280 -> {
                    onlinePlayers.world.playFirework(loc4, firework)
                }
            }
        }
    }
}