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

import net.kyori.adventure.text.Component.text
import org.bukkit.*
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
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import world.komq.paralleluniverse.api.data.PlayerGameDataManager.Companion.modifyPlayerIntStatData
import world.komq.paralleluniverse.api.data.UniversalDataManager.Companion.playerCoins
import world.komq.paralleluniverse.api.enums.AssignType
import world.komq.paralleluniverse.api.enums.DataType
import world.komq.paralleluniverse.api.enums.GameType
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.hasNetherStar
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.initialKill
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.isRunning
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.itemDrop
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.itemDropLocX
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.itemDropLocY
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.itemDropLocZ
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.killCount
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.netherStarItem
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.nsOwner
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.onlyOne
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.playerTeam
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.playerTeamColor
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.plugin
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.sc
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.server
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.stopGame
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.trackItem
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.wasDead
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.winner
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.world
import world.komq.server.fakepit.plugin.tasks.FakepitEndTask

class FakepitEvent : Listener {
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
            p.setMetadata("spectator", FixedMetadataValue(plugin,"spectator"))
            p.sendMessage(text("게임에서 퇴장하시어 관전자 상태로 전환되었습니다!"))
            p.gameMode = GameMode.SURVIVAL
        }

        p.teleport(Location(world, 0.0, 70.0, 0.0))
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val p = e.player
        val playerTeam = playerTeam[p.uniqueId]
        val scoreObjective = requireNotNull(sc.getObjective("Points"))
        val playerScoreValue = scoreObjective.getScore("${playerTeamColor[p.uniqueId]}${p.name}").score
        val playerBoldScoreValue = scoreObjective.getScore("${playerTeamColor[p.uniqueId]}${ChatColor.BOLD}${p.name}").score

        if (!p.hasMetadata("spectator")) {
            p.inventory.clear()
            playerTeam?.unregister()
            p.isFlying = true
            p.isGlowing = false

            if (hasNetherStar[p.uniqueId] == true) {
                val dropItem = p.world.dropItem(p.location.clone().add(0.5, 1.2, 0.5), netherStarItem())

                trackItem = dropItem
                dropItem.velocity = Vector()
                server.broadcast(text("네더의 별 소유자가 퇴장하여 네더의 별이 땅에 떨어졌습니다!"))

                itemDrop = true
                itemDropLocX = p.location.x.toInt()
                itemDropLocY = p.location.y.toInt()
                itemDropLocZ = p.location.z.toInt()

                hasNetherStar[p.uniqueId] = false

                scoreObjective.getScore("${playerTeamColor[p.uniqueId]}${ChatColor.STRIKETHROUGH}${p.name}${ChatColor.RESET}${ChatColor.GRAY} (서버 퇴장)").score = playerBoldScoreValue
                sc.resetScores("${playerTeamColor[p.uniqueId]}${ChatColor.BOLD}${p.name}")
            }
            else {
                scoreObjective.getScore("${playerTeamColor[p.uniqueId]}${ChatColor.STRIKETHROUGH}${p.name}${ChatColor.RESET}${ChatColor.GRAY} (서버 퇴장)").score = playerScoreValue
                sc.resetScores("${playerTeamColor[p.uniqueId]}${p.name}")
            }
        }

        server.scheduler.runTaskLater(plugin, Runnable {
            if (server.onlinePlayers.filter { !p.hasMetadata("spectator") }.toMutableList().size == 1) {
                stopGame()
                server.onlinePlayers.forEach {
                    server.scheduler.runTaskTimer(plugin, FakepitEndTask(), 0L, 0L)
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
                nsOwner = killer

                points.getScore("${playerTeamColor[nsOwner.uniqueId]}${ChatColor.BOLD}${nsOwner.name}").score = points.getScore("${playerTeamColor[nsOwner.uniqueId]}${nsOwner.name}").score
                sc.resetScores("${playerTeamColor[nsOwner.uniqueId]}${nsOwner.name}")

                killer.inventory.setItem(EquipmentSlot.OFF_HAND, netherStarItem())
                killer.isGlowing = true
                server.broadcast(text("${playerTeamColor[nsOwner.uniqueId]}${ChatColor.BOLD}${nsOwner.name}${ChatColor.RESET}님이 첫 네더의 별을 소유하고 있습니다!"))
            }
            else if (hasNetherStar[p.uniqueId] == true) {
                p.isGlowing = false
                killer.isGlowing = true
                hasNetherStar[p.uniqueId] = false
                hasNetherStar[killer.uniqueId] = true

                points.getScore("${playerTeamColor[nsOwner.uniqueId]}${nsOwner.name}").score = points.getScore("${playerTeamColor[nsOwner.uniqueId]}${ChatColor.BOLD}${nsOwner.name}").score
                sc.resetScores("${playerTeamColor[nsOwner.uniqueId]}${ChatColor.BOLD}${nsOwner.name}")

                nsOwner = killer

                points.getScore("${playerTeamColor[nsOwner.uniqueId]}${ChatColor.BOLD}${nsOwner.name}").score = points.getScore("${playerTeamColor[nsOwner.uniqueId]}${nsOwner.name}").score
                sc.resetScores("${playerTeamColor[nsOwner.uniqueId]}${nsOwner.name}")

                killer.inventory.setItem(EquipmentSlot.OFF_HAND, netherStarItem())
                server.broadcast(text("${playerTeamColor[nsOwner.uniqueId]}${ChatColor.BOLD}${killer.name}${ChatColor.RESET}님이 네더의 별을 소유하고 있습니다!"))
            }
        }

        killCount[p.uniqueId] = requireNotNull(killCount[p.uniqueId]) + 1
        modifyPlayerIntStatData(p.uniqueId, GameType.FAKEPIT, DataType.KILL, AssignType.ADD, 1)
        modifyPlayerIntStatData(p.uniqueId, GameType.FAKEPIT, DataType.COIN, AssignType.ADD, 10)
        playerCoins(p.uniqueId, AssignType.ADD, 10)

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
                nsOwner = p
                hasNetherStar[p.uniqueId] = true
                item.remove()
                p.inventory.setItemInOffHand(netherStarItem())
                p.isGlowing = true
                e.isCancelled = true

                points.getScore("${playerTeamColor[nsOwner.uniqueId]}${ChatColor.BOLD}${nsOwner.name}").score = points.getScore("${playerTeamColor[nsOwner.uniqueId]}${nsOwner.name}").score
                sc.resetScores("${playerTeamColor[nsOwner.uniqueId]}${nsOwner.name}")

                server.broadcast(text("${playerTeamColor[nsOwner.uniqueId]}${ChatColor.BOLD}${p.name}${ChatColor.RESET}님이 네더의 별을 주우셨습니다!"))
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