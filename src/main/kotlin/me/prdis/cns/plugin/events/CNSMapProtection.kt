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

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent

/**
 * @author ContentManager
 */

object CNSMapProtection : Listener {
    @EventHandler
    fun BlockBreakEvent.onBlockBreak() {
        if (player.gameMode == GameMode.ADVENTURE) isCancelled = true
    }

    @EventHandler
    fun BlockFadeEvent.onBlockFade() {
        if (block.type == Material.FARMLAND) {
            isCancelled = true
        }
    }

    @EventHandler
    fun PlayerInteractEvent.onPlayerInteract() {
        val block = clickedBlock ?: return

        if (action.isRightClick) {
            if (Tag.BEDS.isTagged(block.type) || Tag.FLOWER_POTS.isTagged(block.type)) {
                if (player.gameMode == GameMode.ADVENTURE) isCancelled = true
            }
        }
    }

    @EventHandler
    fun PlayerDropItemEvent.onPlayerDropItem() {
        if (player.gameMode == GameMode.ADVENTURE) isCancelled = true
    }

    @EventHandler
    fun PlayerSwapHandItemsEvent.onPlayerSwapHandItems() {
        if (player.gameMode == GameMode.ADVENTURE) isCancelled = true
    }

    @EventHandler
    fun InventoryClickEvent.onInventoryClick() {
        if (whoClicked.gameMode == GameMode.ADVENTURE) isCancelled = true
    }
}