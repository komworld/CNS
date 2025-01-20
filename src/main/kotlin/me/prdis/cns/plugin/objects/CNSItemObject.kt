/*
 * Copyright (C) 2025 Paradise Dev Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package me.prdis.cns.plugin.objects

import me.prdis.cns.plugin.objects.CNSImpl.toBukkitColor
import me.prdis.cns.plugin.objects.CNSObject.color
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta

/**
 * @author ContentManager
 */

object CNSItemObject {
    val NETHER_STAR = ItemStack.of(Material.NETHER_STAR).apply {
        editMeta {
            it.displayName(text("NETHER STAR", NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
            it.lore(listOf(text("소유할 때 0.7초마다 점수가 1점씩 오릅니다.", NamedTextColor.GRAY)))
        }
    }

    val SPEED_CARROT = ItemStack.of(Material.GOLDEN_CARROT).apply {
        editMeta {
            it.displayName(text("신속 당근", NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
            it.lore(listOf(text("그 어떠한 황금 당근보다 귀중해보인다.", NamedTextColor.GRAY), text("- 사용 시 5초간 신속 II 적용")))
        }
    }

    val FINITE_HEART = ItemStack.of(Material.HEART_OF_THE_SEA).apply {
        editMeta {
            it.displayName(text("유한한 심장", NamedTextColor.DARK_AQUA).decorate(TextDecoration.BOLD))
            it.lore(listOf(text("한번 쯤은 다시 태어나도 좋지.", NamedTextColor.GRAY), text("- 사용 시 전체 체력 회복")))
        }
    }

    val sword = ItemStack(Material.STONE_SWORD).apply {
        itemMeta = itemMeta.apply {
            isUnbreakable = true
            addItemFlags(*ItemFlag.entries.toTypedArray())
        }
    }

    fun Player.equipArmor() {
        inventory.helmet = ItemStack.of(Material.LEATHER_HELMET).apply {
            editMeta {
                it as LeatherArmorMeta
                it.setColor(color?.toBukkitColor())
                it.isUnbreakable = true
                it.addItemFlags(*ItemFlag.entries.toTypedArray())
            }
        }
        inventory.chestplate = ItemStack.of(Material.LEATHER_CHESTPLATE).apply {
            editMeta {
                it as LeatherArmorMeta
                it.setColor(color?.toBukkitColor())
                it.isUnbreakable = true
                it.addItemFlags(*ItemFlag.entries.toTypedArray())
            }
        }
        inventory.leggings = ItemStack.of(Material.LEATHER_LEGGINGS).apply {
            editMeta {
                it as LeatherArmorMeta
                it.setColor(color?.toBukkitColor())
                it.isUnbreakable = true
                it.addItemFlags(*ItemFlag.entries.toTypedArray())
            }
        }
        inventory.boots = ItemStack.of(Material.LEATHER_BOOTS).apply {
            editMeta {
                it as LeatherArmorMeta
                it.setColor(color?.toBukkitColor())
                it.isUnbreakable = true
                it.addItemFlags(*ItemFlag.entries.toTypedArray())
            }
        }
    }
}