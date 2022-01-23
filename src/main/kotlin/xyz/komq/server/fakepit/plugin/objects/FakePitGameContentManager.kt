/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package xyz.komq.server.fakepit.plugin.objects

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title.Times.of
import net.kyori.adventure.title.Title.title
import org.bukkit.*
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
import xyz.komq.server.fakepit.plugin.events.FakePitEvent
import java.time.Duration.ofSeconds
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
    var winner = ""
    var onlyOne = false
    lateinit var netherStarOwner: Player
    lateinit var playingWorld: World

    private val playerTeam = HashMap<UUID, Team?>()
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

        val health = sc.getObjective("Health")
        if (health == null) sc.registerNewObjective("Health", "health", text("♥", NamedTextColor.RED))

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
    }

    fun getTeam(teamCount: Int): Team? {
        var team = sc.getTeam("")
        when (teamCount) {
            0 -> { team = sc.getTeam("Red") }
            1 -> { team = sc.getTeam("Orange") }
            2 -> { team = sc.getTeam("Yellow") }
            3 -> { team = sc.getTeam("Green") }
            4 -> { team = sc.getTeam("DarkGreen") }
            5 -> { team = sc.getTeam("Aqua") }
            6 -> { team = sc.getTeam("Blue") }
            7 -> { team = sc.getTeam("Purple") }
            8 -> { team = sc.getTeam("White") }
            9 -> { team = sc.getTeam("Gray") }
            10 -> { team = sc.getTeam("DarkAqua") }
            11 -> { team = sc.getTeam("Pink") }
        }

        return team
    }

    fun addTeam(name: String, teamCount: Int) {
        val player = requireNotNull(server.getPlayer(name))
        when (teamCount) {
            0 -> { sc.getTeam("Red")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("Red") }
            1 -> { sc.getTeam("Orange")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("Orange") }
            2 -> { sc.getTeam("Yellow")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("Yellow") }
            3 -> { sc.getTeam("Green")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("Green") }
            4 -> { sc.getTeam("DarkGreen")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("DarkGreen") }
            5 -> { sc.getTeam("Aqua")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("Aqua") }
            6 -> { sc.getTeam("Blue")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("Blue") }
            7 -> { sc.getTeam("Purple")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("Purple") }
            8 -> { sc.getTeam("White")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("White") }
            9 -> { sc.getTeam("Gray")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("Gray") }
            10 -> { sc.getTeam("DarkAqua")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("DarkAqua") }
            11 -> { sc.getTeam("Pink")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("Pink") }
        }
    }

    fun getTeamChatColor(teamCount: Int): ChatColor {
        var color = ChatColor.RESET

        when (teamCount) {
            0 -> { color = ChatColor.RED }
            1 -> { color = ChatColor.GOLD }
            2 -> { color = ChatColor.YELLOW }
            3 -> { color = ChatColor.GREEN }
            4 -> { color = ChatColor.DARK_GREEN }
            5 -> { color = ChatColor.AQUA }
            6 -> { color = ChatColor.BLUE }
            7 -> { color = ChatColor.DARK_PURPLE }
            8 -> { color = ChatColor.WHITE }
            9 -> { color = ChatColor.GRAY }
            10 -> { color = ChatColor.DARK_AQUA }
            11 -> { color = ChatColor.LIGHT_PURPLE }
        }

        return color
    }

    fun setupArmors(teamCount: Int, player: Player) {
        val inventory = player.inventory
        when (teamCount) {
            0 -> {
                equipArmor(inventory, Color.RED)
            }
            1 -> {
                equipArmor(inventory, Color.ORANGE)
            }
            2 -> {
                equipArmor(inventory, Color.YELLOW)
            }
            3 -> {
                equipArmor(inventory, Color.LIME)
            }
            4 -> {
                equipArmor(inventory, Color.GREEN)
            }
            5 -> {
                equipArmor(inventory, Color.AQUA)
            }
            6 -> {
                equipArmor(inventory, Color.BLUE)
            }
            7 -> {
                equipArmor(inventory, Color.PURPLE)
            }
            8 -> {
                equipArmor(inventory, Color.WHITE)
            }
            9 -> {
                equipArmor(inventory, Color.GRAY)
            }
            10 -> {
                equipArmor(inventory, Color.SILVER)
            }
            11 -> {
                equipArmor(inventory, Color.FUCHSIA)
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
        playerNameList.clear()
        server.onlinePlayers.forEach {
            it.inventory.clear()
            sc.resetScores(it.name)
            hasNetherStar[it.uniqueId] = false
            it.isGlowing = false
        }
        FakePitEvent().quitArray.clear()
        sc.objectives.forEach { it.unregister() }
        sc.teams.forEach { it.unregister() }
        teamCount = 0
        initialKill = 0
        itemDrop = false
        isRunning = false
    }

    fun titleFunc(OnlyOne: Boolean) {
        server.onlinePlayers.forEach {
            if (!OnlyOne) {
                it.resetTitle()
                it.showTitle(title(text("게임 종료!", NamedTextColor.GOLD), text("우승자: $winner", NamedTextColor.YELLOW), of(ofSeconds(0), ofSeconds(8), ofSeconds(0))))
            } else {
                it.resetTitle()
                it.showTitle(title(text("게임 종료!", NamedTextColor.GOLD), text("모든 사람들이 나갔습니다!", NamedTextColor.YELLOW), of(ofSeconds(0), ofSeconds(8), ofSeconds(0))))
            }
        }
    }

    private fun equipArmor(inv: PlayerInventory, color: Color) {
        val helmet = ItemStack(Material.LEATHER_HELMET)
        val chestplate = ItemStack(Material.LEATHER_CHESTPLATE)
        val leggings = ItemStack(Material.LEATHER_LEGGINGS)
        val boots = ItemStack(Material.LEATHER_BOOTS)

        helmet.itemMeta = colorMeta(helmet.itemMeta as LeatherArmorMeta, color)
        chestplate.itemMeta = colorMeta(chestplate.itemMeta as LeatherArmorMeta, color)
        leggings.itemMeta = colorMeta(leggings.itemMeta as LeatherArmorMeta, color)
        boots.itemMeta = colorMeta(boots.itemMeta as LeatherArmorMeta, color)

        inv.helmet = helmet
        inv.chestplate = chestplate
        inv.leggings = leggings
        inv.boots = boots
    }

    private fun colorMeta(meta: LeatherArmorMeta, color: Color): LeatherArmorMeta {
        meta.setColor(color)
        meta.isUnbreakable = true
        return meta
    }

    fun netherStarItem(): ItemStack {
        val netherStar = ItemStack(Material.NETHER_STAR, 1)
        val netherStarMeta = netherStar.itemMeta

        netherStarMeta.displayName(text("NETHER STAR", NamedTextColor.AQUA).decorate(TextDecoration.BOLD).decorate(TextDecoration.ITALIC))
        netherStarMeta.lore(listOf(text("소유시 0.7초마다 점수가 1점씩 오릅니다.", NamedTextColor.GRAY)))

        netherStar.itemMeta = netherStarMeta
        return netherStar
    }
}