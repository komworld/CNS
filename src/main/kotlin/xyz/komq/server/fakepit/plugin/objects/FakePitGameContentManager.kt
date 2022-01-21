/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package xyz.komq.server.fakepit.plugin.objects

import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.plugin.Plugin
import xyz.komq.server.fakepit.plugin.FakePitPluginMain
import java.util.*

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

    var teamCount = 0

    val playerTeamCount = HashMap<UUID, Int>()
    val wasDead = HashMap<UUID, Boolean>()
    val hasNetherStar = HashMap<UUID, Boolean>()

    var gameTaskId = 0
    var initialKill = 0

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

    fun setupTeams() {
        if (red == null) sc.registerNewTeam("Red")
        red?.color(NamedTextColor.RED)

        if (orange == null) sc.registerNewTeam("Orange")
        orange?.color(NamedTextColor.GOLD)

        if (yellow == null) sc.registerNewTeam("Yellow")
        yellow?.color(NamedTextColor.YELLOW)

        if (green == null) sc.registerNewTeam("Green")
        green?.color(NamedTextColor.GREEN)

        if (darkGreen == null) sc.registerNewTeam("DarkGreen")
        darkGreen?.color(NamedTextColor.DARK_GREEN)

        if (aqua == null) sc.registerNewTeam("Aqua")
        aqua?.color(NamedTextColor.AQUA)

        if (blue == null) sc.registerNewTeam("Blue")
        blue?.color(NamedTextColor.BLUE)

        if (purple == null) sc.registerNewTeam("Purple")
        purple?.color(NamedTextColor.DARK_PURPLE)

        if (white == null) sc.registerNewTeam("White")
        white?.color(NamedTextColor.WHITE)

        if (gray == null) sc.registerNewTeam("Gray")
        gray?.color(NamedTextColor.GRAY)

        if (darkAqua == null) sc.registerNewTeam("DarkAqua")
        darkAqua?.color(NamedTextColor.DARK_AQUA)

        if (pink == null) sc.registerNewTeam("Pink")
        pink?.color(NamedTextColor.LIGHT_PURPLE)
    }

    fun addTeam(name: String, teamCount: Int) {
        when (teamCount) {
            0 -> red?.addEntry(name)
            1 -> orange?.addEntry(name)
            2 -> yellow?.addEntry(name)
            3 -> green?.addEntry(name)
            4 -> darkGreen?.addEntry(name)
            5 -> aqua?.addEntry(name)
            6 -> blue?.addEntry(name)
            7 -> purple?.addEntry(name)
            8 -> white?.addEntry(name)
            9 -> gray?.addEntry(name)
            10 -> darkAqua?.addEntry(name)
            11 -> pink?.addEntry(name)
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