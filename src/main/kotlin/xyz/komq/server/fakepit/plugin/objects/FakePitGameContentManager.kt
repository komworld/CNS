/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package xyz.komq.server.fakepit.plugin.objects

import com.google.common.io.ByteStreams
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Team
import xyz.komq.server.fakepit.plugin.FakePitPluginMain
import xyz.komq.server.fakepit.plugin.events.FakePitEvent
import xyz.komq.server.fakepit.plugin.tasks.FakePitGameTask
import xyz.komq.server.fakepit.plugin.tasks.FakePitSecondsTickTask
import xyz.komq.server.fakepit.plugin.tasks.FakePitZeroTickTask
import java.util.*

/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

object FakePitGameContentManager {
    val plugin = FakePitPluginMain.instance

    val server = plugin.server
    lateinit var event: Listener

    val world = requireNotNull(server.getWorld("World"))

    fun Player.sendTo(serverName : String) {
        @Suppress("UnstableApiUsage")
        val out = ByteStreams.newDataOutput()
        out.writeUTF("Connect")
        out.writeUTF(serverName)
        sendPluginMessage(plugin, "BungeeCord", out.toByteArray())
    }
    
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
    private var teamCount = 0

    var isRunning = false
    var playable = true

    var itemDrop = false
    var itemDropLocX = 0
    var itemDropLocY = 0
    var itemDropLocZ = 0

    var initialKill = 0
    var onlyOne = false

    lateinit var randomPlayer: Player
    lateinit var winner: Player
    lateinit var netherStarOwner: Player

    val playerTeam = HashMap<UUID, Team?>()
    val playerTeamCount = HashMap<UUID, Int>()
    val wasDead = HashMap<UUID, Boolean>()
    val hasNetherStar = HashMap<UUID, Boolean>()

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
        if (point == null) sc.registerNewObjective("Points", "dummy", text("별잡아라!", NamedTextColor.AQUA).decorate(TextDecoration.BOLD)).apply {
            displaySlot = DisplaySlot.SIDEBAR
        }

        val health = sc.getObjective("Health")
        if (health == null) sc.registerNewObjective("Health", "health", text("♥", NamedTextColor.RED)).apply {
            displaySlot = DisplaySlot.BELOW_NAME
        }

        if (red == null) sc.registerNewTeam("Red").apply {
            color(NamedTextColor.RED)
        }

        if (orange == null) sc.registerNewTeam("Orange").apply {
            color(NamedTextColor.GOLD)
        }

        if (yellow == null) sc.registerNewTeam("Yellow").apply {
            color(NamedTextColor.YELLOW)
        }

        if (green == null) sc.registerNewTeam("Green").apply {
            color(NamedTextColor.GREEN)
        }

        if (darkGreen == null) sc.registerNewTeam("DarkGreen").apply {
            color(NamedTextColor.DARK_GREEN)
        }

        if (aqua == null) sc.registerNewTeam("Aqua").apply {
            color(NamedTextColor.AQUA)
        }

        if (blue == null) sc.registerNewTeam("Blue").apply {
            color(NamedTextColor.BLUE)
        }

        if (purple == null) sc.registerNewTeam("Purple").apply {
            color(NamedTextColor.DARK_PURPLE)
        }

        if (white == null) sc.registerNewTeam("White").apply {
            color(NamedTextColor.WHITE)
        }

        if (gray == null) sc.registerNewTeam("Gray").apply {
            color(NamedTextColor.GRAY)
        }

        if (darkAqua == null) sc.registerNewTeam("DarkAqua").apply {
            color(NamedTextColor.DARK_AQUA)
        }

        if (pink == null) sc.registerNewTeam("Pink").apply {
            color(NamedTextColor.LIGHT_PURPLE)
        }
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

    private fun setupArmors(teamCount: Int, player: Player) {
        val inventory = player.inventory
        when (teamCount) {
            0 -> {
                equipArmor(inventory, Color.fromRGB(255, 85, 85))
            }
            1 -> {
                equipArmor(inventory, Color.fromRGB(255, 170, 0))
            }
            2 -> {
                equipArmor(inventory, Color.fromRGB(255, 255, 85))
            }
            3 -> {
                equipArmor(inventory, Color.fromRGB(85, 255, 85))
            }
            4 -> {
                equipArmor(inventory, Color.fromRGB(0, 170, 0))
            }
            5 -> {
                equipArmor(inventory, Color.fromRGB(85, 255, 255))
            }
            6 -> {
                equipArmor(inventory, Color.fromRGB(85, 85, 255))
            }
            7 -> {
                equipArmor(inventory, Color.fromRGB(170, 0, 170))
            }
            8 -> {
                equipArmor(inventory, Color.fromRGB(255, 255, 255))
            }
            9 -> {
                equipArmor(inventory, Color.fromRGB(170, 170, 170))
            }
            10 -> {
                equipArmor(inventory, Color.fromRGB(0, 170, 170))
            }
            11 -> {
                equipArmor(inventory, Color.fromRGB(255, 85, 255))
            }
        }
    }

    private fun unbreakableMeta(meta: ItemMeta): ItemMeta {
        meta.isUnbreakable = true
        return meta
    }

    fun startGame() {
        server.onlinePlayers.forEach {
            it.inventory.clear()
            if (administrators.toString().contains(it.uniqueId.toString())) {
                if (!plugin.config.getBoolean("allow-admins-to-play")) {
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

        setupScoreboards()

        players.forEach {
            playerNameList.add(it.name)
            it.gameMode = GameMode.ADVENTURE
            it.inventory.setItem(0, sword)

            server.scheduler.runTaskLater(plugin, Runnable {
                sc.getObjective("Points")?.getScore("${getTeamColor(requireNotNull(playerTeamCount[it.uniqueId]))}${it.name}")?.score = 1
                sc.getObjective("Points")?.getScore("${getTeamColor(requireNotNull(playerTeamCount[it.uniqueId]))}${it.name}")?.score = 0
            }, 2L)
        }

        playerNameList.shuffle()

        fun teamConfiguration() {
            val teamPlayer = requireNotNull(server.getPlayer(playerNameList[teamCount]))

            addTeam(playerNameList[teamCount], teamCount)
            setupArmors(teamCount, teamPlayer)
            playerTeamCount[teamPlayer.uniqueId] = teamCount
            ++teamCount
        }

        while (teamCount < playerNameList.size) {
            if (playerNameList.size in 2..12) {
                teamConfiguration()
            }
            else {
                server.broadcast(text("최소/최대 플레이 가능 플레이어 수가 적거나 많습니다.", NamedTextColor.RED))
                server.broadcast(text("플레이어들을 관전자로 바꿔주세요. 그렇지 않으면 게임이 실행 할 수 없습니다.", NamedTextColor.RED))
                server.broadcast(text("최소 플레이어 수: 2 / 최대 플레이어 수: 12", NamedTextColor.RED))
                stopGame()
                playable = false
                break
            }
        }

        if (playable) {
            server.worlds.forEach {
                it.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
                it.setGameRule(GameRule.KEEP_INVENTORY, true)
                it.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
            }

            server.onlinePlayers.forEach {
                it.teleport(Location(it.world, 0.5, 71.0, 0.5))
                it.health = 20.0
                it.foodLevel = 20
                it.damage(0.5)
            }

            server.pluginManager.registerEvents(event, plugin)
            server.scheduler.runTaskTimer(plugin, FakePitGameTask(), 0L, 14L)
            server.scheduler.runTaskTimer(plugin, FakePitZeroTickTask(), 0L, 0L)
            server.scheduler.runTaskTimer(plugin, FakePitSecondsTickTask(), 0L ,20L)

            val randomPlayer = server.onlinePlayers.toList()[0]
            netherStarOwner = randomPlayer
            world.pvp = true
            isRunning = true
        }
    }

    fun stopGame() {
        world.pvp = false
        HandlerList.unregisterAll(event)
        server.scheduler.cancelTasks(plugin)
        playerNameList.clear()
        server.onlinePlayers.forEach {
            it.inventory.clear()
            sc.resetScores(it.name)
            hasNetherStar[it.uniqueId] = false
            it.isGlowing = false
            it.gameMode = GameMode.ADVENTURE
        }
        FakePitEvent().quitArray.clear()
        sc.objectives.forEach { it.unregister() }
        sc.teams.forEach { it.unregister() }
        teamCount = 0
        initialKill = 0
        itemDrop = false
        isRunning = false
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

        netherStar.itemMeta = netherStar.itemMeta.apply {
            displayName(text("NETHER STAR", NamedTextColor.AQUA).decorate(TextDecoration.BOLD).decorate(TextDecoration.ITALIC))
            lore(listOf(text("소유시 0.7초마다 점수가 1점씩 오릅니다.", NamedTextColor.GRAY)))
        }

        return netherStar
    }
}