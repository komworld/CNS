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

package me.prdis.cns.plugin.config

import me.prdis.cns.plugin.objects.CNSObject.mapNum
import me.prdis.cns.plugin.objects.CNSObject.maxPlayers
import me.prdis.cns.plugin.objects.CNSObject.minPlayers
import me.prdis.cns.plugin.objects.CNSObject.plugin
import net.kyori.adventure.text.Component.text

/**
 * @author ContentManager
 */

object CNSConfig {
    fun checkConfig() {
        val configMinPlayers = plugin.config.getInt("minPlayers")
        val configMaxPlayers = plugin.config.getInt("maxPlayers")
        val configMapNum = plugin.config.getInt("mapnum")

        if (configMinPlayers == 0 || configMaxPlayers == 0) {
            plugin.componentLogger.error(text("플러그인 설정의 플레이어 수가 0으로 잘못 설정되어있습니다."))
        }

        if (configMapNum != 0) {
            mapNum = configMapNum
        }

        if (configMinPlayers <= configMaxPlayers) {
            minPlayers = configMinPlayers
            maxPlayers = configMaxPlayers
        } else plugin.componentLogger.error(text("플러그인 설정의 최소 플레이어가 최대 플레이어보다 작거나 같지 않습니다. 기본 설정(8 ~ 11명)을 사용합니다."))
    }
}