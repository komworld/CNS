package xyz.komq.server.fakepit.plugin.tasks

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title.Times.of
import net.kyori.adventure.title.Title.title
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.getTeamColor
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.hasNetherStar
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.initialKill
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.netherStarOwner
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.playerTeamCount
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.randomPlayer
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.sc
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.server
import xyz.komq.server.fakepit.plugin.objects.FakePitGameContentManager.stopGame
import java.time.Duration.ofSeconds

class FakePitSecondsTickTask: Runnable {
    private var count = 0

    override fun run() {
        when (count++) {
            60 -> {
                if (initialKill == 0) {
                    initialKill = 1

                    randomPlayer = server.onlinePlayers.filter { it.gameMode != GameMode.SPECTATOR }.toList()[0]

                    randomPlayer.inventory.setItemInOffHand(ItemStack(Material.NETHER_STAR))
                    randomPlayer.isGlowing = true
                    netherStarOwner = randomPlayer
                    hasNetherStar[randomPlayer.uniqueId] = true

                    sc.resetScores("${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${randomPlayer.name}")
                    server.broadcast(text("1분동안 아무도 죽이지 않아 랜덤으로 네더의 별이 지급되었습니다!"))
                    server.broadcast(text("${getTeamColor(requireNotNull(playerTeamCount[netherStarOwner.uniqueId]))}${ChatColor.BOLD}${randomPlayer.name}${ChatColor.RESET}님이 첫 네더의 별을 소유하고 있습니다!"))
                }
            }
            1200 -> {
                stopGame()
                server.broadcast(text("20분동안 아무도 승리 조건을 달성하지 못하여 게임이 강제 종료되었습니다!"))
                server.onlinePlayers.forEach {
                    it.resetTitle()
                    it.showTitle(title(text("게임 종료!", NamedTextColor.GOLD), text("무승부!", NamedTextColor.YELLOW), of(ofSeconds(0), ofSeconds(8), ofSeconds(0))))
                }
            }
        }
    }
}