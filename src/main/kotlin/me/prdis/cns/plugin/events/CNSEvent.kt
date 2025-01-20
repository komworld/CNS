/*
 * Copyright (C) 2025 Paradise Dev Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package me.prdis.cns.plugin.events

import io.papermc.paper.scoreboard.numbers.NumberFormat
import me.prdis.cns.plugin.objects.CNSImpl.changeNSOwner
import me.prdis.cns.plugin.objects.CNSImpl.ending
import me.prdis.cns.plugin.objects.CNSImpl.runTaskLater
import me.prdis.cns.plugin.objects.CNSImpl.stopGame
import me.prdis.cns.plugin.objects.CNSItemObject.FINITE_HEART
import me.prdis.cns.plugin.objects.CNSItemObject.NETHER_STAR
import me.prdis.cns.plugin.objects.CNSItemObject.SPEED_CARROT
import me.prdis.cns.plugin.objects.CNSObject.SPAWN_LOCATION
import me.prdis.cns.plugin.objects.CNSObject.color
import me.prdis.cns.plugin.objects.CNSObject.droppedStarEntity
import me.prdis.cns.plugin.objects.CNSObject.gamePlayers
import me.prdis.cns.plugin.objects.CNSObject.initialKill
import me.prdis.cns.plugin.objects.CNSObject.internalDeath
import me.prdis.cns.plugin.objects.CNSObject.isRunning
import me.prdis.cns.plugin.objects.CNSObject.itemDropped
import me.prdis.cns.plugin.objects.CNSObject.nsOwner
import me.prdis.cns.plugin.objects.CNSObject.pointsObjective
import me.prdis.cns.plugin.objects.CNSObject.server
import me.prdis.cns.plugin.objects.CNSObject.spectators
import me.prdis.cns.plugin.objects.CNSObject.team
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * @author ContentManager
 */

object CNSEvent : Listener {
    @EventHandler
    fun EntityDamageEvent.onEntityDamagge() {
        if (entity is Player) {
            if (cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                isCancelled = true
            }
        }
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        if (isRunning) {
            if (player.uniqueId in spectators) {
                player.sendMessage(text("게임에서 퇴장하시어 관전자로 전환되었습니다!"))
            } else {
                player.sendMessage(text("게임이 진행중이어 관전자로 전환되었습니다!"))
            }
            player.gameMode = GameMode.SPECTATOR
        }

        player.teleportAsync(SPAWN_LOCATION)
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        if (player.uniqueId !in spectators && player.uniqueId in gamePlayers) {
            spectators.add(player.uniqueId)
            gamePlayers.remove(player.uniqueId)

            player.inventory.clear()

            player.allowFlight = true
            player.isGlowing = false

            val score = pointsObjective.getScore(player.name)

            score.customName(
                text(player.name, player.color!!).append(text(" (서버 퇴장)", NamedTextColor.GRAY))
                    .decorate(TextDecoration.STRIKETHROUGH)
            )
            score.numberFormat(NumberFormat.styled(Style.style(NamedTextColor.GRAY)))

            player.team?.unregister()

            if (nsOwner == player.uniqueId) {
                val dropItem =
                    player.world.dropItemNaturally(player.location.clone().add(0.5, 1.2, 0.5), NETHER_STAR.clone())

                dropItem.isGlowing = true
                droppedStarEntity = dropItem
                server.broadcast(text("네더의 별 소유자가 퇴장하여 네더의 별이 땅에 떨어졌습니다!"))

                itemDropped = true

                nsOwner = null
            }

            runTaskLater(2) {
                server.onlinePlayers.filter { !spectators.contains(it.uniqueId) }.let { players ->
                    if (players.size == 1) {
                        stopGame()
                        ending(players.first(), true)
                    }
                }
            }
        }
    }

    @EventHandler
    fun PlayerDeathEvent.onDeath() {
        val killer = player.killer

        if (killer is Player) {
            if (!initialKill) {
                initialKill = true
                changeNSOwner(killer)
            } else if (nsOwner == player.uniqueId) {
                changeNSOwner(killer, player)
            }
        }

        isCancelled = true
        player.internalDeath = true
        player.inventory.setItem(EquipmentSlot.OFF_HAND, ItemStack.of(Material.AIR))
        player.foodLevel += 1
        player.saturation = 5f

        runTaskLater(2) {
            player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false, false))
        }

        runTaskLater(20) {
            player.internalDeath = false
        }
    }

    @EventHandler
    fun PlayerAttemptPickupItemEvent.onPlayerAttemptPickupItem() {
        if (item.itemStack.isSimilar(NETHER_STAR.clone())) {
            if (itemDropped) {
                itemDropped = false
                nsOwner = player.uniqueId

                item.remove()
                player.inventory.setItemInOffHand(NETHER_STAR.clone())
                player.isGlowing = true
                isCancelled = true

                val score = pointsObjective.getScore(player.name)

                score.customName(text(player.name, player.color!!).decorate(TextDecoration.BOLD))
                score.numberFormat(NumberFormat.styled(Style.style(NamedTextColor.RED, TextDecoration.BOLD)))

                server.broadcast(
                    text(player.name, player.color!!).append(
                        text(
                            "님이 네더의 별을 주우셨습니다!",
                            NamedTextColor.WHITE
                        )
                    )
                )
            }
        }
    }

    @EventHandler
    fun PlayerMoveEvent.onPlayerMove() {
        if (player.internalDeath) {
            isCancelled = true
        }
    }

    @EventHandler
    fun EntityDamageByEntityEvent.onEntityDamageByEntity() {
        if (entity is Player && (entity as Player).internalDeath) {
            isCancelled = true
        }
    }

    @EventHandler
    fun PlayerItemConsumeEvent.onPlayerItemConsume() {
        if (item.isSimilar(SPEED_CARROT.clone())) {
            player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 100, 1))
        }
    }

    @EventHandler
    fun PlayerInteractEvent.onPlayerInteract() {
        val item = item ?: return

        if (action.isRightClick && item.isSimilar(FINITE_HEART.clone())) {
            item.amount--
            player.health = 20.0
        }
    }
}
