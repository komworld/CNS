/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package xyz.komq.server.fakepit.plugin.objects

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Color
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.plugin.Plugin
import org.bukkit.scoreboard.Team
import xyz.komq.server.fakepit.plugin.FakePitPluginMain
import xyz.komq.server.fakepit.plugin.commands.FakePitKommand.playerNameList
import xyz.komq.server.fakepit.plugin.tasks.FakePitConfigReloadTask
import java.util.*
import kotlin.collections.HashMap

/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

object FakePitGameContentManager {
    fun getInstance(): Plugin {
        return FakePitPluginMain.instance
    }

    val server = getInstance().server

    var administrators = arrayListOf(
        "389c4c9b-6342-42fc-beb3-922a7d7a72f9", // komq
        "5082c832-7f7c-4b04-b0c7-2825062b7638", // BaeHyeonWoo
        "762dea11-9c45-4b18-95fc-a86aab3b39ee", // aroxu
        "6340af5e-bbd1-41c4-b86d-3425b347063c", // RootPi
        "63e8e8a6-4104-4abf-811b-2ed277a02738", // norhu1130
        "ad524e9e-acf5-4977-9c12-938212663361", // ssapgosuX
        "3013e38a-74a7-41d4-8e68-71ee440c0e20" // choda100x
    )

    var isRunning = false
    var teamCount = 0
    var itemDrop = false
    var itemDropLocX = 0
    var itemDropLocY = 0
    var itemDropLocZ = 0
    var initialKill = 0
    lateinit var netherStarOwner: Player

    val playerTeam = HashMap<UUID, Team?>()
    val playerTeamCount = HashMap<UUID, Int>()
    val wasDead = HashMap<UUID, Boolean>()
    val hasNetherStar = HashMap<UUID, Boolean>()

    val deathLocation = HashMap<UUID, Location>()

    // Team Settings

    private val sm = server.scoreboardManager
    val sc = sm.mainScoreboard

    private val red = sc.getTeam("Red")
    private val orange = sc.getTeam("Orange")
    private val yellow = sc.getTeam("Yellow")
    private val green = sc.getTeam("Green")
    private val darkGreen = sc.getTeam("DarkGreen")
    private val aqua = sc.getTeam("Aqua")
    private val blue = sc.getTeam("Blue")
    private val purple = sc.getTeam("Purple")
    private val white = sc.getTeam("White")
    private val gray = sc.getTeam("Gray")
    private val darkAqua = sc.getTeam("DarkAqua")
    private val pink = sc.getTeam("Pink")

    fun setupScoreboards() {
        val point = sc.getObjective("Points")
        if (point == null) sc.registerNewObjective("Points", "dummy", text("POINTS", NamedTextColor.AQUA).decorate(TextDecoration.BOLD))

        if (red == null) sc.registerNewTeam("Red")

        if (orange == null) sc.registerNewTeam("Orange")

        if (yellow == null) sc.registerNewTeam("Yellow")

        if (green == null) sc.registerNewTeam("Green")

        if (darkGreen == null) sc.registerNewTeam("DarkGreen")

        if (aqua == null) sc.registerNewTeam("Aqua")

        if (blue == null) sc.registerNewTeam("Blue")

        if (purple == null) sc.registerNewTeam("Purple")

        if (white == null) sc.registerNewTeam("White")

        if (gray == null) sc.registerNewTeam("Gray")

        if (darkAqua == null) sc.registerNewTeam("DarkAqua")

        if (pink == null) sc.registerNewTeam("Pink")

        server.scheduler.runTaskLater(getInstance(), Runnable {
            red?.color(NamedTextColor.RED)
            orange?.color(NamedTextColor.GOLD)
            yellow?.color(NamedTextColor.YELLOW)
            green?.color(NamedTextColor.GREEN)
            darkGreen?.color(NamedTextColor.DARK_GREEN)
            aqua?.color(NamedTextColor.AQUA)
            blue?.color(NamedTextColor.BLUE)
            purple?.color(NamedTextColor.DARK_PURPLE)
            white?.color(NamedTextColor.WHITE)
            gray?.color(NamedTextColor.GRAY)
            darkAqua?.color(NamedTextColor.DARK_AQUA)
            pink?.color(NamedTextColor.LIGHT_PURPLE)
        }, 20L)
    }

    fun addTeam(name: String, teamCount: Int) {
        val player = requireNotNull(server.getPlayer(name))

        when (teamCount) {
            0 -> { red?.addEntry(name); playerTeam[player.uniqueId] = red }
            1 -> { orange?.addEntry(name); playerTeam[player.uniqueId] = orange }
            2 -> { yellow?.addEntry(name); playerTeam[player.uniqueId] = yellow }
            3 -> { green?.addEntry(name); playerTeam[player.uniqueId] = green }
            4 -> { darkGreen?.addEntry(name); playerTeam[player.uniqueId] = darkGreen }
            5 -> { aqua?.addEntry(name); playerTeam[player.uniqueId] = aqua }
            6 -> { blue?.addEntry(name); playerTeam[player.uniqueId] = blue }
            7 -> { purple?.addEntry(name); playerTeam[player.uniqueId] = purple }
            8 -> { white?.addEntry(name); playerTeam[player.uniqueId] = white }
            9 -> { gray?.addEntry(name); playerTeam[player.uniqueId] = gray }
            10 -> { darkAqua?.addEntry(name); playerTeam[player.uniqueId] = darkAqua }
            11 -> { pink?.addEntry(name); playerTeam[player.uniqueId] = pink }
        }
    }

    fun setupArmors(teamCount: Int, player: Player) {
        when (teamCount) {
            0 -> {
                val rhelmet = ItemStack(Material.LEATHER_HELMET)
                val rchestplate = ItemStack(Material.LEATHER_CHESTPLATE)
                val rleggings = ItemStack(Material.LEATHER_LEGGINGS)
                val rboots = ItemStack(Material.LEATHER_BOOTS)
                val inv = player.inventory

                rhelmet.itemMeta = setupMetas(rhelmet.itemMeta as LeatherArmorMeta, Color.RED)
                rchestplate.itemMeta = setupMetas(rchestplate.itemMeta as LeatherArmorMeta, Color.RED)
                rleggings.itemMeta = setupMetas(rleggings.itemMeta as LeatherArmorMeta, Color.RED)
                rboots.itemMeta = setupMetas(rboots.itemMeta as LeatherArmorMeta, Color.RED)

                equipArmor(inv, rhelmet, rchestplate, rleggings, rboots)
            }
            1 -> {
                val ohelmet = ItemStack(Material.LEATHER_HELMET)
                val ochestplate = ItemStack(Material.LEATHER_CHESTPLATE)
                val oleggings = ItemStack(Material.LEATHER_LEGGINGS)
                val oboots = ItemStack(Material.LEATHER_BOOTS)
                val inv = player.inventory

                ohelmet.itemMeta = setupMetas(ohelmet.itemMeta as LeatherArmorMeta, Color.ORANGE)
                ochestplate.itemMeta = setupMetas(ochestplate.itemMeta as LeatherArmorMeta, Color.ORANGE)
                oleggings.itemMeta = setupMetas(oleggings.itemMeta as LeatherArmorMeta, Color.ORANGE)
                oboots.itemMeta = setupMetas(oboots.itemMeta as LeatherArmorMeta, Color.ORANGE)

                equipArmor(inv, ohelmet, ochestplate, oleggings, oboots)
            }
            2 -> {
                val yhelmet = ItemStack(Material.LEATHER_HELMET)
                val ychestplate = ItemStack(Material.LEATHER_CHESTPLATE)
                val yleggings = ItemStack(Material.LEATHER_LEGGINGS)
                val yboots = ItemStack(Material.LEATHER_BOOTS)
                val inv = player.inventory

                yhelmet.itemMeta = setupMetas(yhelmet.itemMeta as LeatherArmorMeta, Color.YELLOW)
                ychestplate.itemMeta = setupMetas(ychestplate.itemMeta as LeatherArmorMeta, Color.YELLOW)
                yleggings.itemMeta = setupMetas(yleggings.itemMeta as LeatherArmorMeta, Color.YELLOW)
                yboots.itemMeta = setupMetas(yboots.itemMeta as LeatherArmorMeta, Color.YELLOW)

                equipArmor(inv, yhelmet, ychestplate, yleggings, yboots)
            }
            3 -> {
                val ghelmet = ItemStack(Material.LEATHER_HELMET)
                val gchestplate = ItemStack(Material.LEATHER_CHESTPLATE)
                val gleggings = ItemStack(Material.LEATHER_LEGGINGS)
                val gboots = ItemStack(Material.LEATHER_BOOTS)
                val inv = player.inventory

                ghelmet.itemMeta = setupMetas(ghelmet.itemMeta as LeatherArmorMeta, Color.LIME)
                gchestplate.itemMeta = setupMetas(gchestplate.itemMeta as LeatherArmorMeta, Color.LIME)
                gleggings.itemMeta = setupMetas(gleggings.itemMeta as LeatherArmorMeta, Color.LIME)
                gboots.itemMeta = setupMetas(gboots.itemMeta as LeatherArmorMeta, Color.LIME)

                equipArmor(inv, ghelmet, gchestplate, gleggings, gboots)
            }
            4 -> {
                val dghelmet = ItemStack(Material.LEATHER_HELMET)
                val dgchestplate = ItemStack(Material.LEATHER_CHESTPLATE)
                val dgleggings = ItemStack(Material.LEATHER_LEGGINGS)
                val dgboots = ItemStack(Material.LEATHER_BOOTS)
                val inv = player.inventory

                dghelmet.itemMeta = setupMetas(dghelmet.itemMeta as LeatherArmorMeta, Color.GREEN)
                dgchestplate.itemMeta = setupMetas(dgchestplate.itemMeta as LeatherArmorMeta, Color.GREEN)
                dgleggings.itemMeta = setupMetas(dgleggings.itemMeta as LeatherArmorMeta, Color.GREEN)
                dgboots.itemMeta = setupMetas(dgboots.itemMeta as LeatherArmorMeta, Color.GREEN)

                equipArmor(inv, dghelmet, dgchestplate, dgleggings, dgboots)
            }
            5 -> {
                val ahelmet = ItemStack(Material.LEATHER_HELMET)
                val achestplate = ItemStack(Material.LEATHER_CHESTPLATE)
                val aleggings = ItemStack(Material.LEATHER_LEGGINGS)
                val aboots = ItemStack(Material.LEATHER_BOOTS)
                val inv = player.inventory

                ahelmet.itemMeta = setupMetas(ahelmet.itemMeta as LeatherArmorMeta, Color.AQUA)
                achestplate.itemMeta = setupMetas(achestplate.itemMeta as LeatherArmorMeta, Color.AQUA)
                aleggings.itemMeta = setupMetas(aleggings.itemMeta as LeatherArmorMeta, Color.AQUA)
                aboots.itemMeta = setupMetas(aboots.itemMeta as LeatherArmorMeta, Color.AQUA)

                equipArmor(inv, ahelmet, achestplate, aleggings, aboots)
            }
            6 -> {
                val rhelmet = ItemStack(Material.LEATHER_HELMET)
                val rchestplate = ItemStack(Material.LEATHER_CHESTPLATE)
                val rleggings = ItemStack(Material.LEATHER_LEGGINGS)
                val rboots = ItemStack(Material.LEATHER_BOOTS)
                val inv = player.inventory

                rhelmet.itemMeta = setupMetas(rhelmet.itemMeta as LeatherArmorMeta, Color.BLUE)
                rchestplate.itemMeta = setupMetas(rchestplate.itemMeta as LeatherArmorMeta, Color.BLUE)
                rleggings.itemMeta = setupMetas(rleggings.itemMeta as LeatherArmorMeta, Color.BLUE)
                rboots.itemMeta = setupMetas(rboots.itemMeta as LeatherArmorMeta, Color.BLUE)

                equipArmor(inv, rhelmet, rchestplate, rleggings, rboots)
            }
            7 -> {
                val phelmet = ItemStack(Material.LEATHER_HELMET)
                val pchestplate = ItemStack(Material.LEATHER_CHESTPLATE)
                val pleggings = ItemStack(Material.LEATHER_LEGGINGS)
                val pboots = ItemStack(Material.LEATHER_BOOTS)
                val inv = player.inventory

                phelmet.itemMeta = setupMetas(phelmet.itemMeta as LeatherArmorMeta, Color.PURPLE)
                pchestplate.itemMeta = setupMetas(pchestplate.itemMeta as LeatherArmorMeta, Color.PURPLE)
                pleggings.itemMeta = setupMetas(pleggings.itemMeta as LeatherArmorMeta, Color.PURPLE)
                pboots.itemMeta = setupMetas(pboots.itemMeta as LeatherArmorMeta, Color.PURPLE)

                equipArmor(inv, phelmet, pchestplate, pleggings, pboots)
            }
            8 -> {
                val whelmet = ItemStack(Material.LEATHER_HELMET)
                val wchestplate = ItemStack(Material.LEATHER_CHESTPLATE)
                val wleggings = ItemStack(Material.LEATHER_LEGGINGS)
                val wboots = ItemStack(Material.LEATHER_BOOTS)
                val inv = player.inventory

                whelmet.itemMeta = setupMetas(whelmet.itemMeta as LeatherArmorMeta, Color.WHITE)
                wchestplate.itemMeta = setupMetas(wchestplate.itemMeta as LeatherArmorMeta, Color.WHITE)
                wleggings.itemMeta = setupMetas(wleggings.itemMeta as LeatherArmorMeta, Color.WHITE)
                wboots.itemMeta = setupMetas(wboots.itemMeta as LeatherArmorMeta, Color.WHITE)

                equipArmor(inv, whelmet, wchestplate, wleggings, wboots)
            }
            9 -> {
                val ghelmet = ItemStack(Material.LEATHER_HELMET)
                val gchestplate = ItemStack(Material.LEATHER_CHESTPLATE)
                val gleggings = ItemStack(Material.LEATHER_LEGGINGS)
                val gboots = ItemStack(Material.LEATHER_BOOTS)
                val inv = player.inventory

                ghelmet.itemMeta = setupMetas(ghelmet.itemMeta as LeatherArmorMeta, Color.GRAY)
                gchestplate.itemMeta = setupMetas(gchestplate.itemMeta as LeatherArmorMeta, Color.GRAY)
                gleggings.itemMeta = setupMetas(gleggings.itemMeta as LeatherArmorMeta, Color.GRAY)
                gboots.itemMeta = setupMetas(gboots.itemMeta as LeatherArmorMeta, Color.GRAY)

                equipArmor(inv, ghelmet, gchestplate, gleggings, gboots)
            }
            10 -> {
                val dahelmet = ItemStack(Material.LEATHER_HELMET)
                val dachestplate = ItemStack(Material.LEATHER_CHESTPLATE)
                val daleggings = ItemStack(Material.LEATHER_LEGGINGS)
                val daboots = ItemStack(Material.LEATHER_BOOTS)
                val inv = player.inventory

                dahelmet.itemMeta = setupMetas(dahelmet.itemMeta as LeatherArmorMeta, Color.SILVER)
                dachestplate.itemMeta = setupMetas(dachestplate.itemMeta as LeatherArmorMeta, Color.SILVER)
                daleggings.itemMeta = setupMetas(daleggings.itemMeta as LeatherArmorMeta, Color.SILVER)
                daboots.itemMeta = setupMetas(daboots.itemMeta as LeatherArmorMeta, Color.SILVER)

                equipArmor(inv, dahelmet, dachestplate, daleggings, daboots)
            }
            11 -> {
                val phelmet = ItemStack(Material.LEATHER_HELMET)
                val pchestplate = ItemStack(Material.LEATHER_CHESTPLATE)
                val pleggings = ItemStack(Material.LEATHER_LEGGINGS)
                val pboots = ItemStack(Material.LEATHER_BOOTS)
                val inv = player.inventory

                phelmet.itemMeta = setupMetas(phelmet.itemMeta as LeatherArmorMeta, Color.FUCHSIA)
                pchestplate.itemMeta = setupMetas(pchestplate.itemMeta as LeatherArmorMeta, Color.FUCHSIA)
                pleggings.itemMeta = setupMetas(pleggings.itemMeta as LeatherArmorMeta, Color.FUCHSIA)
                pboots.itemMeta = setupMetas(pboots.itemMeta as LeatherArmorMeta, Color.FUCHSIA)

                equipArmor(inv, phelmet, pchestplate, pleggings, pboots)
            }
        }
    }

    fun unbreakableMeta(meta: ItemMeta): ItemMeta {
        meta.isUnbreakable = true
        return meta
    }

    fun stopGame() {
        HandlerList.unregisterAll(getInstance())
        server.scheduler.cancelTasks(getInstance())
        server.scheduler.runTaskTimer(getInstance(), FakePitConfigReloadTask(), 0L, 20L)
        playerNameList.clear()
        server.onlinePlayers.forEach {
            it.inventory.clear()
            sc.resetScores(it.name)
            hasNetherStar[it.uniqueId] = false
            it.isGlowing = false
        }
        server.worlds.forEach {
            it.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false)
            it.setGameRule(GameRule.KEEP_INVENTORY, false)
        }
        sc.objectives.forEach { it.unregister() }
        sc.teams.forEach { it.unregister() }
        teamCount = 0
        initialKill = 0
        isRunning = false
    }

    private fun setupMetas(meta: LeatherArmorMeta, color: Color): LeatherArmorMeta {
        meta.setColor(color)
        meta.isUnbreakable = true
        return meta
    }

    private fun equipArmor(inv: PlayerInventory, helmet: ItemStack, chestplate: ItemStack, leggings: ItemStack, boots: ItemStack) {
        inv.helmet = helmet
        inv.chestplate = chestplate
        inv.leggings = leggings
        inv.boots = boots
    }
}