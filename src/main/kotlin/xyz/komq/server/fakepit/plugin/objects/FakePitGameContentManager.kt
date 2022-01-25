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
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Team
import xyz.komq.server.fakepit.plugin.FakePitPluginMain
import xyz.komq.server.fakepit.plugin.events.FakePitEvent
import xyz.komq.server.fakepit.plugin.tasks.FakePitGameTask
import xyz.komq.server.fakepit.plugin.tasks.FakePitSecondsTickTask
import xyz.komq.server.fakepit.plugin.tasks.FakePitZeroTickTask
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

    private var administrators = arrayListOf(
        "389c4c9b-6342-42fc-beb3-922a7d7a72f9", // komq
        "5082c832-7f7c-4b04-b0c7-2825062b7638", // BaeHyeonWoo
        "762dea11-9c45-4b18-95fc-a86aab3b39ee", // aroxu
        "6340af5e-bbd1-41c4-b86d-3425b347063c", // RootPi
        "63e8e8a6-4104-4abf-811b-2ed277a02738", // norhu1130
        "ad524e9e-acf5-4977-9c12-938212663361", // ssapgosuX
        "3013e38a-74a7-41d4-8e68-71ee440c0e20" // choda100x
    )

    private val playerNameList = ArrayList<String>()
    private val playerUUIDList = ArrayList<String>()

    val randomPlayer: Player = server.onlinePlayers.toList()[0]

    var isRunning = false
    private var teamCount = 0
    var itemDrop = false
    var itemDropLocX = 0
    var itemDropLocY = 0
    var itemDropLocZ = 0
    var initialKill = 0
    var winner = ""
    var onlyOne = false
    lateinit var netherStarOwner: Player
    private lateinit var playingWorld: World

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

    private fun setupScoreboards() {
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

        sc.getTeam("Red")?.color(NamedTextColor.RED)
        sc.getTeam("Orange")?.color(NamedTextColor.GOLD)
        sc.getTeam("Yellow")?.color(NamedTextColor.YELLOW)
        sc.getTeam("Green")?.color(NamedTextColor.GREEN)
        sc.getTeam("DarkGreen")?.color(NamedTextColor.DARK_GREEN)
        sc.getTeam("Aqua")?.color(NamedTextColor.AQUA)
        sc.getTeam("Blue")?.color(NamedTextColor.BLUE)
        sc.getTeam("Purple")?.color(NamedTextColor.DARK_PURPLE)
        sc.getTeam("White")?.color(NamedTextColor.WHITE)
        sc.getTeam("Gray")?.color(NamedTextColor.GRAY)
        sc.getTeam("DarkAqua")?.color(NamedTextColor.DARK_AQUA)
        sc.getTeam("Pink")?.color(NamedTextColor.LIGHT_PURPLE)

        sc.getObjective("Points")?.displaySlot = DisplaySlot.SIDEBAR
        sc.getObjective("Health")?.displaySlot = DisplaySlot.BELOW_NAME
    }

    private fun addTeam(name: String, teamCount: Int) {
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

    fun getTeamColor(teamCount: Int): ChatColor {
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

    fun startGame() {
        server.onlinePlayers.forEach {
            it.inventory.clear()
            if (it.uniqueId.toString() in administrators.toString()) {
                if (!getInstance().config.getBoolean("allow-admins-to-play")) {
                    it.gameMode = GameMode.SPECTATOR
                    it.sendMessage(text("관리자 -> GAMEMODE: SPECTATOR"))
                }
            }
        }

        val players = server.onlinePlayers.asSequence().filter {
            it.gameMode != GameMode.SPECTATOR
        }.toMutableList()

        val sword = ItemStack(Material.STONE_SWORD)
        val swordMeta = unbreakableMeta(sword.itemMeta)
        sword.itemMeta = swordMeta

        players.forEach {
            playerNameList.add(it.name)
            playerUUIDList.add(it.uniqueId.toString())
            it.gameMode = GameMode.ADVENTURE
            it.inventory.setItem(0, sword)
        }

        playerNameList.shuffle()
        setupScoreboards()

        fun teamConfiguration() {
            val teamPlayer = requireNotNull(server.getPlayer(playerNameList[teamCount]))

            addTeam(playerNameList[teamCount], teamCount)
            setupArmors(teamCount, teamPlayer)
            playerTeamCount[teamPlayer.uniqueId] = teamCount
            ++teamCount
        }

        var playable = false

        while (teamCount != playerNameList.size) {
            if (server.onlinePlayers.size in 2..12) {
                teamConfiguration()
                playable = true
            }
            else if (server.onlinePlayers.size <= 13) {
                if (administrators.toString() in playerUUIDList.toString()) {
                    playable = true
                }
            }
            else {
                server.broadcast(text("최소/최대 플레이 가능 플레이어 수가 적거나 많습니다.", NamedTextColor.RED))
                server.broadcast(text("몇몇 플레이어들을 관전자로 바꿔주세요. 그렇지 않으면 게임이 실행 될 수 없습니다.", NamedTextColor.RED))
                server.broadcast(text("최소 플레이어 수: 2 / 최대 플레이어 수: 12"))
                stopGame()
                playable = false
            }
        }

        if (playable) {
            server.worlds.forEach {
                it.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
                it.setGameRule(GameRule.KEEP_INVENTORY, true)
                it.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
            }

            server.onlinePlayers.forEach {
                it.teleport(Location(it.world, 0.5, 72.5, 0.5))
                it.health = 20.0
                it.foodLevel = 20
                it.damage(0.5)
                it.scoreboard.getObjective("Points")?.getScore(it.name)?.score = 1
                it.scoreboard.getObjective("Points")?.getScore(it.name)?.score = 0
            }

            server.pluginManager.registerEvents(FakePitEvent(), getInstance())
            server.scheduler.runTaskTimer(getInstance(), FakePitGameTask(), 0L, 14L)
            server.scheduler.runTaskTimer(getInstance(), FakePitZeroTickTask(), 0L, 0L)
            server.scheduler.runTaskTimer(getInstance(), FakePitSecondsTickTask(), 0L ,20L)

            val randomPlayer = server.onlinePlayers.toList()[0]

            netherStarOwner = randomPlayer
            playingWorld = randomPlayer.world
            isRunning = true
        }
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