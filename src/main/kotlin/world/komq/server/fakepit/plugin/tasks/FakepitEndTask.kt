/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package world.komq.server.fakepit.plugin.tasks

import io.github.monun.tap.effect.playFirework
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title.Times.times
import net.kyori.adventure.title.Title.title
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.potion.PotionEffectType
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.onlyOne
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.plugin
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.sendTo
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.server
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.winner
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.world
import java.time.Duration.ofSeconds

/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

class FakepitEndTask: Runnable {
    private var count = 0

    override fun run() {
        val loc1 = winner.location.add(5.0, 3.0, 0.0)
        val loc2 = winner.location.add(0.0, 3.0, 5.0)
        val loc3 = winner.location.subtract(5.0, 0.0, 0.0).add(0.0, 3.0, 0.0)
        val loc4 = winner.location.subtract(0.0, 0.0, 5.0).add(0.0, 3.0, 0.0)

        val colors = arrayListOf(Color.RED, Color.BLUE, Color.GREEN, Color.AQUA, Color.PURPLE, Color.FUCHSIA)

        val firework = FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(colors).build()

        when (count++) {
            0 -> {
                server.onlinePlayers.forEach {
                    if (!onlyOne) {
                        it.resetTitle()
                        it.showTitle(title(text("게임 종료!", NamedTextColor.GOLD), text("${winner.name}님이 우승하셨습니다!", NamedTextColor.YELLOW), times(ofSeconds(0), ofSeconds(8), ofSeconds(0))))
                    } else {
                        it.resetTitle()
                        it.showTitle(title(text("게임 종료!", NamedTextColor.GOLD), text("모든 사람들이 나갔습니다!", NamedTextColor.YELLOW), times(ofSeconds(0), ofSeconds(8), ofSeconds(0))))
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
                world.playFirework(loc1, firework)
            }
            140 -> world.playFirework(loc2, firework)
            160 -> world.playFirework(loc3, firework)
            180 -> world.playFirework(loc4, firework)

            220 -> {
                server.onlinePlayers.forEach {
                    it.playSound(it.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 1000F, 1F)
                }
                world.playFirework(loc1, firework)
            }
            240 -> world.playFirework(loc2, firework)
            260 -> world.playFirework(loc3, firework)
            280 -> world.playFirework(loc4, firework)

            300 -> if (!plugin.config.getBoolean("nonproduction")) server.onlinePlayers.forEach { it.sendTo(plugin.config.getString("lobbyserver").toString()) }
            340 -> if (!plugin.config.getBoolean("nonproduction")) {
                server.scheduler.cancelTasks(plugin)
                server.shutdown()
            } else return
        }
    }
}