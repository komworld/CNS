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
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.getTeamColor
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.hasNetherStar
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.initialKill
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.isRunning
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.itemDrop
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.itemDropLocX
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.itemDropLocY
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.itemDropLocZ
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.netherStarItem
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.netherStarOwner
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.onlyOne
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.playerTeam
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.playerTeamCount
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.plugin
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.sc
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.server
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.stopGame
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.wasDead
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.winner
import xyz.komq.server.fakepit.plugin.tasks.FakePitEndTask
import java.util.*

class FakePitEvent : Listener {

    var quitArray = ArrayList<UUID>()

    @EventHandler
    fun onEntityDamage(e: EntityDamageEvent) {
        if (e.entity is Player) {
            if (e.cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val p = e.player

        if (isRunning) {
            if (quitArray.contains(p.uniqueId)) {
                p.sendMessage(text("게임에서 퇴장하시어 관전자 상태로 전환되었습니다!"))
            }
            else {
                p.sendMessage(text("게임이 진행중이어 관전자 상태로 전환되었습니다!"))
            }
            p.gameMode = GameMode.SPECTATOR
        }
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val p = e.player
        val playerTeam = playerTeam[p.uniqueId]
        val scoreObjective = requireNotNull(sc.getObjective("Points"))
        val quitPlayerTeamColor = getTeamColor(requireNotNull(playerTeamCount[p.uniqueId]))
        val playerScoreValue = scoreObjective.getScore("${quitPlayerTeamColor}${p.name}").score
        val playerBoldScoreValue = scoreObjective.getScore("${quitPlayerTeamColor}${ChatColor.BOLD}${p.name}").score

        quitArray.add(p.uniqueId)
        p.inventory.clear()
        playerTeam?.unregister()

        if (hasNetherStar[p.uniqueId] == true) {
            val dropItem = p.world.dropItem(p.location.clone().add(0.5, 1.2, 0.5), netherStarItem())

            p.isGlowing = false
            dropItem.velocity = Vector()
            server.broadcast(text("네더의 별 소유자가 퇴장하여 네더의 별이 땅에 떨어졌습니다!"))

            itemDrop = true
            itemDropLocX = p.location.x.toInt()
            itemDropLocY = p.location.y.toInt()
            itemDropLocZ = p.location.z.toInt()

            hasNetherStar[p.uniqueId] = false

            scoreObjective.getScore("${quitPlayerTeamColor}${ChatColor.STRIKETHROUGH}${p.name}${ChatColor.RESET}${ChatColor.GRAY} (서버 퇴장)").score = playerBoldScoreValue
            sc.resetScores("${quitPlayerTeamColor}${ChatColor.BOLD}${p.name}")
        }
        else {
            scoreObjective.getScore("${quitPlayerTeamColor}${ChatColor.STRIKETHROUGH}${p.name}${ChatColor.RESET}${ChatColor.GRAY} (서버 퇴장)").score = playerScoreValue
            sc.resetScores("${quitPlayerTeamColor}${p.name}")
        }

        server.scheduler.runTaskLater(plugin, Runnable {
            if (server.onlinePlayers.size == 1) {
                stopGame()
                server.onlinePlayers.forEach {
                    server.scheduler.runTaskTimer(plugin, FakePitEndTask(), 0L, 0L)
                    onlyOne = true
                    winner = it
                }
            }
        }, 2L)
    }

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        val p = e.player
        val killer = e.player.killer
        val points = requireNotNull(sc.getObjective("Points"))

        if (killer is Player) {
            if (initialKill == 0) {
                initialKill = 1
                hasNetherStar[killer.uniqueId] = true
                netherStarOwner = killer

                points.getScore("${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${ChatColor.BOLD}${netherStarOwner.name}").score = points.getScore("${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${netherStarOwner.name}").score
                sc.resetScores("${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${netherStarOwner.name}")

                killer.inventory.setItem(EquipmentSlot.OFF_HAND, netherStarItem())
                killer.isGlowing = true
                server.broadcast(text("${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${ChatColor.BOLD}${netherStarOwner.name}${ChatColor.RESET}님이 첫 네더의 별을 소유하고 있습니다!"))
            }
            else if (hasNetherStar[p.uniqueId] == true) {
                p.isGlowing = false
                killer.isGlowing = true
                hasNetherStar[p.uniqueId] = false
                hasNetherStar[killer.uniqueId] = true

                points.getScore("${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${netherStarOwner.name}").score = points.getScore("${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${ChatColor.BOLD}${netherStarOwner.name}").score
                sc.resetScores("${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${ChatColor.BOLD}${netherStarOwner.name}")

                netherStarOwner = killer

                points.getScore("${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${ChatColor.BOLD}${netherStarOwner.name}").score = points.getScore("${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${netherStarOwner.name}").score
                sc.resetScores("${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${netherStarOwner.name}")

                killer.inventory.setItem(EquipmentSlot.OFF_HAND, netherStarItem())
                server.broadcast(text("${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${ChatColor.BOLD}${killer.name}${ChatColor.RESET}님이 네더의 별을 소유하고 있습니다!"))
            }
        }

        e.isCancelled = true
        wasDead[p.uniqueId] = true
        p.inventory.setItem(EquipmentSlot.OFF_HAND, ItemStack(Material.AIR))

        server.scheduler.runTaskLater(plugin, Runnable {
            p.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false, false))
        }, 2L)

        server.scheduler.runTaskLater(plugin, Runnable { wasDead[p.uniqueId] = false }, 20L)
    }

    @EventHandler
    fun onPlayerAttemptPickupItem(e: PlayerAttemptPickupItemEvent) {
        val p = e.player
        val item = e.item
        val points = requireNotNull(sc.getObjective("Points"))

        if (item.itemStack.type == Material.NETHER_STAR) {
            if (itemDrop) {
                itemDrop = false
                netherStarOwner = p
                hasNetherStar[p.uniqueId] = true
                item.remove()
                p.inventory.setItemInOffHand(netherStarItem())
                p.isGlowing = true
                e.isCancelled = true

                points.getScore("${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${ChatColor.BOLD}${netherStarOwner.name}").score = points.getScore("${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${netherStarOwner.name}").score
                sc.resetScores("${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${netherStarOwner.name}")

                server.broadcast(text("${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${ChatColor.BOLD}${p.name}${ChatColor.RESET}님이 네더의 별을 주우셨습니다!"))
            }
        }
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