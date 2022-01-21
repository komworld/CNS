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
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.getInstance
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.hasNetherStar
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.initialKill
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.playerTeamCount
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.server
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.setupArmors
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
    fun onPlayerDeath(e: PlayerDeathEvent) {
        val p = e.player

        p.bedSpawnLocation = p.location.add(0.0, 0.5, 0.0)
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
        server.scheduler.runTaskLater(getInstance(), Runnable { wasDead[p.uniqueId] = false }, 20L)
        p.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 20, 0, false, false, false))
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
        val dmgr = e.damager
        val entity = e.entity

        if (entity is Player) {
            if (wasDead[entity.uniqueId] == true) {
                e.isCancelled = true
            }
        }
        if (dmgr is Player && entity is Player) {
            if (entity.isDead) {
                if (initialKill == 0) {
                    initialKill = 1
                    hasNetherStar[dmgr.uniqueId] = true

                    dmgr.inventory.setItem(8, ItemStack(Material.NETHER_STAR))
                    // TODO : LORE
                }
                if (hasNetherStar[entity.uniqueId] == true) {
                    hasNetherStar[entity.uniqueId] = false
                    dmgr.inventory.setItem(8, ItemStack(Material.NETHER_STAR))
                }
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
}