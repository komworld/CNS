package world.komq.server.fakepit.plugin.tasks

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title.Times.times
import net.kyori.adventure.title.Title.title
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.hasNetherStar
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.initialKill
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.nsOwner
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.playerTeamColor
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.randomPlayer
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.sc
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.server
import world.komq.server.fakepit.plugin.objects.FakepitGameContentManager.stopGame
import java.time.Duration.ofSeconds

class FakepitSecondsTickTask: Runnable {
    private var count = 0

    override fun run() {
        when (count++) {
            60 -> {
                if (initialKill == 0) {
                    initialKill = 1

                    randomPlayer = server.onlinePlayers.filter { it.gameMode != GameMode.SPECTATOR }.toMutableList()[0]

                    randomPlayer.inventory.setItemInOffHand(ItemStack(Material.NETHER_STAR))
                    randomPlayer.isGlowing = true
                    nsOwner = randomPlayer
                    hasNetherStar[randomPlayer.uniqueId] = true

                    sc.resetScores("${playerTeamColor[nsOwner.uniqueId]}${randomPlayer.name}")
                    server.broadcast(text("1분동안 아무도 죽이지 않아 랜덤으로 네더의 별이 지급되었습니다!"))
                    server.broadcast(text("${playerTeamColor[nsOwner.uniqueId]}${ChatColor.BOLD}${randomPlayer.name}${ChatColor.RESET}님이 첫 네더의 별을 소유하고 있습니다!"))
                }
            }
            1200 -> {
                stopGame()
                server.broadcast(text("20분동안 아무도 승리 조건을 달성하지 못하여 게임이 강제 종료되었습니다!"))
                server.onlinePlayers.forEach {
                    it.resetTitle()
                    it.showTitle(title(text("게임 종료!", NamedTextColor.GOLD), text("무승부!", NamedTextColor.YELLOW), times(ofSeconds(0), ofSeconds(8), ofSeconds(0))))
                }
            }
        }
    }
}