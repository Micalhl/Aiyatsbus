package com.mcstarrysky.aiyatsbus.module.ingame.listener.mechanism

import com.mcstarrysky.aiyatsbus.core.AiyatsbusSettings
import org.bukkit.event.player.PlayerJoinEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.pluginVersion
import taboolib.platform.util.sendLang

/**
 * Aiyatsbus
 * com.mcstarrysky.aiyatsbus.module.listener.mechanism.ThankMessageListener
 *
 * @author mical
 * @since 2024/3/3 17:01
 */
object ThankMessageListener {

    @SubscribeEvent
    fun e(e: PlayerJoinEvent) {
        if (AiyatsbusSettings.sendThankMessages && (e.player.isOp || e.player.hasPermission("aiyatsbus.thanks"))) {
            e.player.sendLang("thanks", pluginVersion)
        }
    }
}