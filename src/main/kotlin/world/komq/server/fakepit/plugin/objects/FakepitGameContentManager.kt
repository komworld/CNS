/*
 * Copyright (c) 2022 BaeHyeonWoo
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package world.komq.server.fakepit.plugin.objects

import com.google.common.io.ByteStreams
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Team
import world.komq.server.fakepit.plugin.FakepitPluginMain
import world.komq.server.fakepit.plugin.tasks.FakepitGameTask
import world.komq.server.fakepit.plugin.tasks.FakepitSecondsTickTask
import world.komq.server.fakepit.plugin.tasks.FakepitZeroTickTask
import java.util.*

/***
 * @author BaeHyeonWoo
 *
 * "Until my feet are crushed,"
 * "Until I can get ahead of myself."
 */

object FakepitGameContentManager {
    val plugin = FakepitPluginMain.instance

    val server = plugin.server
    lateinit var event: Listener
    lateinit var mapProtectEvent: Listener

    val world = requireNotNull(server.getWorld("World"))

    fun Player.sendTo(serverName : String) {
        @Suppress("UnstableApiUsage")
        val out = ByteStreams.newDataOutput()
        out.writeUTF("Connect")
        out.writeUTF(serverName)
        sendPluginMessage(plugin, "BungeeCord", out.toByteArray())
    }

    private val playerNameList = ArrayList<String>()
    private var teamCount = 0

    var isRunning = false
    private var playable = false

    var trackItem: Item? = null
    var itemDrop = false
    var itemDropLocX = 0
    var itemDropLocY = 0
    var itemDropLocZ = 0

    var initialKill = 0
    var onlyOne = false

    lateinit var randomPlayer: Player
    lateinit var winner: Player
    lateinit var nsOwner: Player

    val playerTeam = HashMap<UUID, Team?>()
    val playerTeamColor = HashMap<UUID, ChatColor>()
    val wasDead = HashMap<UUID, Boolean>()
    val hasNetherStar = HashMap<UUID, Boolean>()
    val killCount = HashMap<UUID, Int>()

    // Team Settings

    private val sm = server.scoreboardManager
    val sc = sm.mainScoreboard

    private fun setupScoreboards() {
        if (sc.getObjective("Points") == null) {
            sc.registerNewObjective("Points", "dummy", text("별잡아라!", NamedTextColor.AQUA).decorate(TextDecoration.BOLD)).apply {
                displaySlot = DisplaySlot.SIDEBAR
            }
        }

        if (sc.getObjective("Health") == null) {
            sc.registerNewObjective("Health", "health", text("♥", NamedTextColor.RED)).apply {
                displaySlot = DisplaySlot.BELOW_NAME
            }
        }

        if (sc.getTeam("Red") == null) {
            sc.registerNewTeam("Red").apply {
                color(NamedTextColor.RED)
            }
        }

        if (sc.getTeam("Orange") == null) {
            sc.registerNewTeam("Orange").apply {
                color(NamedTextColor.GOLD)
            }
        }

        if (sc.getTeam("Yellow") == null) {
            sc.registerNewTeam("Yellow").apply {
                color(NamedTextColor.YELLOW)
            }
        }

        if (sc.getTeam("Green") == null) {
            sc.registerNewTeam("Green").apply {
                color(NamedTextColor.GREEN)
            }
        }

        if (sc.getTeam("DarkGreen") == null) {
            sc.registerNewTeam("DarkGreen").apply {
                color(NamedTextColor.DARK_GREEN)
            }
        }

        if (sc.getTeam("Aqua") == null) {
            sc.registerNewTeam("Aqua").apply {
                color(NamedTextColor.AQUA)
            }
        }

        if (sc.getTeam("Blue") == null) {
            sc.registerNewTeam("Blue").apply {
                color(NamedTextColor.BLUE)
            }
        }

        if (sc.getTeam("Purple") == null) {
            sc.registerNewTeam("Purple").apply {
                color(NamedTextColor.DARK_PURPLE)
            }
        }

        if (sc.getTeam("White") == null) {
            sc.registerNewTeam("White").apply {
                color(NamedTextColor.WHITE)
            }
        }

        if (sc.getTeam("Gray") == null) {
            sc.registerNewTeam("Gray").apply {
                color(NamedTextColor.GRAY)
            }
        }

        if (sc.getTeam("DarkAqua") == null) {
            sc.registerNewTeam("DarkAqua").apply {
                color(NamedTextColor.DARK_AQUA)
            }
        }

        if (sc.getTeam("Pink") == null) {
            sc.registerNewTeam("Pink").apply {
                color(NamedTextColor.LIGHT_PURPLE)
            }
        }
    }

    private fun addTeam(name: String, teamCount: Int) {
        val player = requireNotNull(server.getPlayer(name))
        when (teamCount) {
            0 -> { sc.getTeam("Red")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("Red"); playerTeamColor[player.uniqueId] = ChatColor.RED }
            1 -> { sc.getTeam("Orange")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("Orange"); playerTeamColor[player.uniqueId] = ChatColor.GOLD }
            2 -> { sc.getTeam("Yellow")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("Yellow"); playerTeamColor[player.uniqueId] = ChatColor.YELLOW }
            3 -> { sc.getTeam("Green")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("Green"); playerTeamColor[player.uniqueId] = ChatColor.GREEN }
            4 -> { sc.getTeam("DarkGreen")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("DarkGreen"); playerTeamColor[player.uniqueId] = ChatColor.DARK_GREEN }
            5 -> { sc.getTeam("Aqua")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("Aqua"); playerTeamColor[player.uniqueId] = ChatColor.AQUA }
            6 -> { sc.getTeam("Blue")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("Blue"); playerTeamColor[player.uniqueId] = ChatColor.BLUE }
            7 -> { sc.getTeam("Purple")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("Purple"); playerTeamColor[player.uniqueId] = ChatColor.DARK_PURPLE }
            8 -> { sc.getTeam("White")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("White"); playerTeamColor[player.uniqueId] = ChatColor.WHITE }
            9 -> { sc.getTeam("Gray")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("Gray"); playerTeamColor[player.uniqueId] = ChatColor.GRAY }
            10 -> { sc.getTeam("DarkAqua")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("DarkAqua"); playerTeamColor[player.uniqueId] = ChatColor.DARK_AQUA }
            11 -> { sc.getTeam("Pink")?.addEntry(name); playerTeam[player.uniqueId] = sc.getTeam("Pink"); playerTeamColor[player.uniqueId] = ChatColor.LIGHT_PURPLE }
        }
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

    fun startGame() {
        setupScoreboards()

        server.onlinePlayers.forEach {
            it.inventory.clear()
        }

        val players = server.onlinePlayers.asSequence().filter {
            it.gameMode == GameMode.ADVENTURE
        }.toMutableList()

        val sword = ItemStack(Material.STONE_SWORD).apply {
            itemMeta = itemMeta.apply {
                isUnbreakable = true
            }
        }


        players.forEach {
            playerNameList.add(it.name)
            it.gameMode = GameMode.ADVENTURE
            it.inventory.setItem(0, sword)
            killCount[it.uniqueId] = 0

            server.scheduler.runTaskLater(plugin, Runnable {
                sc.getObjective("Points")?.getScore("${playerTeamColor[it.uniqueId]}${it.name}")?.score = 1
                sc.getObjective("Points")?.getScore("${playerTeamColor[it.uniqueId]}${it.name}")?.score = 0
            }, 2L)
        }

        playerNameList.shuffle()

        teamConfiguration()

        if (playable) {
            server.worlds.forEach {
                it.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
                it.setGameRule(GameRule.KEEP_INVENTORY, true)
                it.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false)
            }

            server.onlinePlayers.forEach {
                it.teleport(Location(it.world, 0.5, 70.5, 0.5))
                it.health = 20.0
                it.foodLevel = 20
                it.damage(0.5)
            }

            HandlerList.unregisterAll(mapProtectEvent)
            server.pluginManager.registerEvents(event, plugin)
            server.scheduler.runTaskTimer(plugin, FakepitGameTask(), 0L, 14L)
            server.scheduler.runTaskTimer(plugin, FakepitZeroTickTask(), 0L, 0L)
            server.scheduler.runTaskTimer(plugin, FakepitSecondsTickTask(), 0L ,20L)

            val randomPlayer = server.onlinePlayers.toMutableList()[0]
            nsOwner = randomPlayer
            world.pvp = true
            isRunning = true
        }
    }

    private fun teamConfiguration() {
        while (teamCount != playerNameList.size) {
            if (playerNameList.size in 2..12) {
                val teamPlayer = requireNotNull(server.getPlayer(playerNameList[teamCount]))

                addTeam(playerNameList[teamCount], teamCount)
                setupArmors(teamCount, teamPlayer)
                ++teamCount
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

        if (teamCount == playerNameList.size) {
            playable = true
        }
    }

    fun stopGame() {
        world.pvp = false
        HandlerList.unregisterAll(event)
        server.pluginManager.registerEvents(mapProtectEvent, plugin)
        server.scheduler.cancelTasks(plugin)
        playerNameList.clear()
        server.onlinePlayers.forEach {
            it.inventory.clear()
            sc.resetScores(it.name)
            hasNetherStar[it.uniqueId] = false
            killCount[it.uniqueId] = 0
            it.isGlowing = false
            it.gameMode = GameMode.ADVENTURE
        }
        sc.objectives.forEach { it.unregister() }
        sc.teams.forEach { it.unregister() }
        server.dispatchCommand(server.consoleSender, "rank settings isGameRunning false")
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