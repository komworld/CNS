package me.prdis.cns.plugin.tasks

import io.papermc.paper.scoreboard.numbers.NumberFormat
import me.prdis.cns.plugin.objects.CNSImpl.ending
import me.prdis.cns.plugin.objects.CNSImpl.runTaskTimer
import me.prdis.cns.plugin.objects.CNSImpl.stopGame
import me.prdis.cns.plugin.objects.CNSItemObject.FINITE_HEART
import me.prdis.cns.plugin.objects.CNSItemObject.NETHER_STAR
import me.prdis.cns.plugin.objects.CNSItemObject.SPEED_CARROT
import me.prdis.cns.plugin.objects.CNSObject.color
import me.prdis.cns.plugin.objects.CNSObject.droppedStarEntity
import me.prdis.cns.plugin.objects.CNSObject.gamePlayers
import me.prdis.cns.plugin.objects.CNSObject.initialKill
import me.prdis.cns.plugin.objects.CNSObject.itemDropped
import me.prdis.cns.plugin.objects.CNSObject.nsOwner
import me.prdis.cns.plugin.objects.CNSObject.pointsObjective
import me.prdis.cns.plugin.objects.CNSObject.server
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title.Times.times
import net.kyori.adventure.title.Title.title
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.time.Duration.ofSeconds

object CNSTasks {
    fun setupTasks() {
        pointsTask()
        gameManageTask()
    }

    private fun pointsTask() {
        runTaskTimer(0, 14) {
            val nsOwnerPlayer = nsOwner?.let { server.getPlayer(it) }

            nsOwnerPlayer?.let { player ->
                val score = pointsObjective.getScore(player.name)
                score.score++

                player.playSound(player.location.clone(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1000F, 2F)
                player.level = pointsObjective.getScore(player.name).score

                player.exp = score.score.toFloat() / 100

                if (score.score == 100) {
                    server.sendActionBar(text(" "))
                    stopGame()
                    ending(player)
                }
            }
        }
    }

    private fun gameManageTask() {
        var count = 0

        runTaskTimer(0, 20) {
            if (count % 30 == 0) {
                gamePlayers.forEach {
                    val player = server.getPlayer(it)
                    player?.inventory?.addItem(SPEED_CARROT.clone())
                }
            }
            if (count % 120 == 0) {
                gamePlayers.forEach {
                    val player = server.getPlayer(it)
                    if (player?.inventory?.contents?.any { item -> item != null && item.isSimilar(FINITE_HEART.clone()) } == false) {
                        player.inventory.addItem(FINITE_HEART.clone())
                    }
                }
            }
            when (count++) {
                0 -> {
                    gamePlayers.forEach {
                        val player = server.getPlayer(it)
                        player?.exp = 0F
                        player?.level = 0
                    }
                }

                60 -> {
                    if (!initialKill) {
                        initialKill = true

                        val randomPlayerUUID = gamePlayers.random()
                        val randomPlayer = server.getPlayer(randomPlayerUUID)

                        randomPlayer?.inventory?.setItemInOffHand(NETHER_STAR.clone())
                        randomPlayer?.isGlowing = true

                        nsOwner = randomPlayerUUID

                        val score = pointsObjective.getScore(randomPlayer?.name ?: "")

                        score.customName(
                            text(
                                randomPlayer?.name ?: "",
                                randomPlayer?.color!!
                            ).decorate(TextDecoration.BOLD)
                        )
                        score.numberFormat(NumberFormat.styled(Style.style(NamedTextColor.RED, TextDecoration.BOLD)))

                        server.broadcast(text("1분동안 아무도 죽이지 않아 랜덤으로 네더의 별이 지급되었습니다!"))
                        server.broadcast(
                            text(
                                randomPlayer.name,
                                randomPlayer.color!!
                            ).append(text("님이 첫 네더의 별을 소유하고 있습니다!", NamedTextColor.WHITE))
                        )
                    }
                }

                1200 -> {
                    stopGame()
                    server.broadcast(text("20분동안 아무도 승리 조건을 달성하지 못하여 게임이 강제 종료되었습니다!"))
                    server.onlinePlayers.forEach {
                        it.resetTitle()
                        it.showTitle(
                            title(
                                text("게임 종료!", NamedTextColor.GOLD),
                                text("무승부!", NamedTextColor.YELLOW),
                                times(ofSeconds(0), ofSeconds(8), ofSeconds(0))
                            )
                        )
                    }
                }
            }
        }

        runTaskTimer(0, 0) {
            server.onlinePlayers.forEach {
                it.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, -1, 255, false, false, false))

                if (!itemDropped) {
                    if (initialKill) {
                        nsOwner?.let { nsOwner ->
                            val nsOwnerPlayer = server.getPlayer(nsOwner) ?: return@let

                            it.sendActionBar(
                                text(
                                    nsOwnerPlayer.name,
                                    nsOwnerPlayer.color!!
                                ).append(text("님이 네더의 별을 소유하고 있습니다!", NamedTextColor.AQUA))
                                    .decorate(TextDecoration.BOLD)
                            )
                        }
                    }
                } else {
                    val itemDropLocX = droppedStarEntity?.location?.x?.toInt() ?: 0
                    val itemDropLocY = droppedStarEntity?.location?.y?.toInt() ?: 0
                    val itemDropLocZ = droppedStarEntity?.location?.z?.toInt() ?: 0
                    it.sendActionBar(text("네더의 별 좌표 | X: ${itemDropLocX}, Y: $itemDropLocY, Z: $itemDropLocZ"))
                }
            }
        }
    }
}