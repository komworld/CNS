/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package xyz.komq.server.fakepit.plugin.events

/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

import net.kyori.adventure.text.Component.text
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.deathLocation
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.getInstance
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.hasNetherStar
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.initialKill
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.itemDrop
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.itemDropLocX
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.itemDropLocY
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.itemDropLocZ
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.netherStarOwner
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.playerTeam
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.playerTeamCount
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.sc
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.server
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.setupArmors
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.stopGame
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.wasDead

class FakePitEvent : Listener {
    @EventHandler
    fun onEntityDamage(e: EntityDamageEvent) {
        if (e.entity is Player) {
            if (e.cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val p = e.player
        val playerTeam = sc.getTeam(playerTeam[p.uniqueId].toString())
        val scoreObjective = sc.getObjective("Points")
        val playerScoreValue = scoreObjective?.getScore(p.name)?.score

        p.inventory.clear()
        playerTeam?.unregister()
        sc.resetScores(p.name)
        scoreObjective?.getScore("${ChatColor.GRAY}${ChatColor.STRIKETHROUGH}${p.name}${ChatColor.RESET}${ChatColor.GRAY} (서버 퇴장)")?.score = requireNotNull(playerScoreValue)

        if (hasNetherStar[p.uniqueId] == true) {
            val dropItem = p.world.dropItem(p.location.clone().add(0.5, 1.2, 0.5), ItemStack(Material.NETHER_STAR))

            p.isGlowing = false
            dropItem.velocity = Vector()
            server.broadcast(text("네더의 별 소유자가 퇴장하여 네더의 별이 땅에 떨어졌습니다!"))

            itemDrop = true
            itemDropLocX = p.location.x.toInt()
            itemDropLocY = p.location.y.toInt()
            itemDropLocZ = p.location.z.toInt()

            hasNetherStar[p.uniqueId] = false
        }

        server.scheduler.runTaskLater(getInstance(), Runnable {
            if (server.onlinePlayers.size == 1) {
                stopGame()
                server.onlinePlayers.forEach {
                    server.broadcast(text("우승자 ${it.name} / 플레이어들이 나감"))
                }
            }
        }, 4L)
    }

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        val p = e.player
        val killer = e.player.killer

        if (killer is Player) {
            if (initialKill == 0) {
                initialKill = 1
                hasNetherStar[killer.uniqueId] = true
                netherStarOwner = killer
                killer.inventory.setItem(EquipmentSlot.OFF_HAND, ItemStack(Material.NETHER_STAR))
                killer.isGlowing = true
                // TODO : LORE
            }
            else if (hasNetherStar[p.uniqueId] == true) {
                p.isGlowing = false
                killer.isGlowing = true
                hasNetherStar[p.uniqueId] = false
                hasNetherStar[killer.uniqueId] = true
                killer.inventory.setItem(EquipmentSlot.OFF_HAND, ItemStack(Material.NETHER_STAR))
            }
        }
        deathLocation[p.uniqueId] = p.location
    }

    @EventHandler
    fun onPlayerAttemptPickupItem(e: PlayerAttemptPickupItemEvent) {
        val p = e.player
        val item = e.item

        if (item.itemStack.type == Material.NETHER_STAR) {
            if (itemDrop) {
                itemDrop = false
                netherStarOwner = p
                hasNetherStar[p.uniqueId] = true
                item.remove()
                p.inventory.setItemInOffHand(ItemStack(Material.NETHER_STAR))
                e.isCancelled = true

                server.sendMessage(text("${p.name}님이 네더의 별을 주우셨습니다!"))
            }
        }
    }

    @EventHandler
    fun onPlayerRespawnEvent(e: PlayerRespawnEvent) {
        val p = e.player

        val sword = ItemStack(Material.STONE_SWORD)
        val swordMeta = FakePitGameContentManager.unbreakableMeta(sword.itemMeta)
        sword.itemMeta = swordMeta
        p.inventory.clear()
        p.inventory.setItemInMainHand(sword)
        p.inventory.addItem(ItemStack(Material.BREAD, 16))
        setupArmors(requireNotNull(playerTeamCount[p.uniqueId]), p)
        wasDead[p.uniqueId] = true
        server.scheduler.runTaskLater(getInstance(), Runnable {
            p.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false, false))
            p.teleport(requireNotNull(deathLocation[p.uniqueId]))
        }, 4L)
        server.scheduler.runTaskLater(getInstance(), Runnable { wasDead[p.uniqueId] = false }, 20L)
    }

    @EventHandler
    fun onPlayerMove(e: PlayerMoveEvent) {
        val p = e.player
        if (wasDead[p.uniqueId] == true) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onEntityDamageByEntity(e: EntityDamageByEntityEvent) {
        val entity = e.entity

        if (entity is Player) {
            if (wasDead[entity.uniqueId] == true) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerDropItem(e: PlayerDropItemEvent) {
        if (e.itemDrop.itemStack.type == Material.NETHER_STAR) e.isCancelled = true
    }

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        if (e.item?.type == Material.NETHER_STAR) e.isCancelled = true
    }

    @EventHandler
    fun onPlayerSwapHandItems(e: PlayerSwapHandItemsEvent) {
        if (e.offHandItem?.type == Material.NETHER_STAR || e.mainHandItem?.type == Material.NETHER_STAR) e.isCancelled = true
    }

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        e.currentItem?.let {
            if (it.type == Material.NETHER_STAR) {
                e.isCancelled = true
            }
        }

        if (e.action == InventoryAction.HOTBAR_SWAP) {
            val item = e.whoClicked.inventory.getItem(e.hotbarButton)
            if (item != null && item.type == Material.NETHER_STAR) {
                e.isCancelled = true
            }
        }
    }
}