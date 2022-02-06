/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package xyz.komq.server.fakepit.plugin.tasks

import io.github.monun.tap.effect.playFirework
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title.Times.of
import net.kyori.adventure.title.Title.title
import org.bukkit.*
import org.bukkit.potion.PotionEffectType
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.onlyOne
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.plugin
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.sendTo
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.server
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.winner
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.world
import java.time.Duration.ofSeconds

/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

class FakePitEndTask: Runnable {
    private var count = 0

    override fun run() {
        val loc1 = Location(world, 25.0, 82.0, 15.0)
        val loc2 = Location(world, 13.0, 82.0, 23.0)
        val loc3 = Location(world, -5.0, 82.0, 14.0)
        val loc4 = Location(world, -17.0, 82.0, 19.0)
        val loc5 = Location(world, -17.0, 82.0, 2.0)
        val loc6 = Location(world, -18.0, 82.0, -15.0)
        val loc7 = Location(world, 3.0, 82.0, -18.0)
        val loc8 = Location(world, 20.0, 82.0, -19.0)
        val loc9 = Location(world, 25.0, 82.0, -1.0)
        val loc10 = Location(world, 1.0, 82.0, -1.0)
        val loc11 = Location(world, -8.0, 82.0, 12.0)
        val loc12 = Location(world, 9.0, 82.0, -7.0)

        val colors = arrayListOf(
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.AQUA,
            Color.PURPLE,
            Color.FUCHSIA
        )

        val firework = FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(colors).build()

        when (count++) {
            0 -> {
                server.onlinePlayers.forEach {
                    if (!onlyOne) {
                        it.resetTitle()
                        it.showTitle(title(text("게임 종료!", NamedTextColor.GOLD), text("${winner.name}님이 우승하셨습니다!", NamedTextColor.YELLOW), of(ofSeconds(0), ofSeconds(8), ofSeconds(0))))
                    } else {
                        it.resetTitle()
                        it.showTitle(title(text("게임 종료!", NamedTextColor.GOLD), text("모든 사람들이 나갔습니다!", NamedTextColor.YELLOW), of(ofSeconds(0), ofSeconds(8), ofSeconds(0))))
                    }
                }
                server.onlinePlayers.forEach {
                    it.removePotionEffect(PotionEffectType.SATURATION)
                    it.playSound(it.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 1000F, 1F)
                }
            }

            20 -> world.playFirework(loc1, firework)
            40 -> world.playFirework(loc2, firework)
            60 -> world.playFirework(loc3, firework)
            80 -> world.playFirework(loc4, firework)

            120 -> {
                server.onlinePlayers.forEach {
                    it.playSound(it.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 1000F, 1F)
                }
                world.playFirework(loc5, firework)
            }
            140 -> world.playFirework(loc6, firework)
            160 -> world.playFirework(loc7, firework)
            180 -> world.playFirework(loc8, firework)

            220 -> {
                server.onlinePlayers.forEach {
                    it.playSound(it.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 1000F, 1F)
                }
                world.playFirework(loc9, firework)
            }
            240 -> world.playFirework(loc10, firework)
            260 -> world.playFirework(loc11, firework)
            280 -> world.playFirework(loc12, firework)

            300 -> server.onlinePlayers.forEach { it.sendTo(plugin.config.getString("lobbyserver").toString()) }
            320 -> server.shutdown()
        }
    }
}