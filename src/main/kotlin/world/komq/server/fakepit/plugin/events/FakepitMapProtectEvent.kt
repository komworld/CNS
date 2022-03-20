/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package world.komq.server.fakepit.plugin.events

/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent

class FakepitMapProtectEvent : Listener {
    @EventHandler
    fun onPlayerAttemptPickupItem(e: PlayerAttemptPickupItemEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun onBlockPhysics(e: BlockPhysicsEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun onBlockFade(e: BlockFadeEvent) {
        val b = e.block

        if (b.type == Material.FARMLAND) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        if (e.item?.type == Material.NETHER_STAR) {
            e.isCancelled = true
        }

        if (e.action == Action.RIGHT_CLICK_BLOCK) {
            if (Tag.BEDS.isTagged(requireNotNull(e.clickedBlock).type)) {
                e.isCancelled = true
            }
        }

        if (e.clickedBlock?.type.toString().startsWith("POTTED_") || e.clickedBlock?.type == Material.FLOWER_POT) e.isCancelled = true
    }

    @EventHandler
    fun onPlayerDropItem(e: PlayerDropItemEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun onPlayerSwapHandItems(e: PlayerSwapHandItemsEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        e.isCancelled = true
    }
}