package com.mcstarrysky.aiyatsbus.core.util

import com.google.gson.JsonParser
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.metadata.Metadatable
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import taboolib.common5.Baffle
import taboolib.common5.cint
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.reflex.UnsafeAccess
import taboolib.module.configuration.util.asMap
import taboolib.platform.util.*
import java.lang.reflect.Field
import java.util.concurrent.TimeUnit

/**
 * Aiyatsbus
 * com.mcstarrysky.aiyatsbus.impl.registration.modern.Utils
 *
 * @author mical
 * @since 2024/2/17 17:07
 */

/**
 * 标记
 */
fun Metadatable.mark(key: String) {
    setMeta(key, bukkitPlugin)
}

fun Metadatable.unmark(key: String) {
    removeMeta(key)
}

/**
 * 旧版 JsonParser
 * 旧版没有 parseString 静态方法
 */
val JSON_PARSER = JsonParser()

/**
 * 旧版文本序列化器
 */
val LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder()
    .character('\u00a7')
    .useUnusualXRepeatedCharacterHexFormat()
    .hexColors()
    .build()

/**
 * 将带有 &a 之类的旧版文本转换为 Adventure Component
 */
fun String.legacyToAdventure(): Component {
    return LEGACY_COMPONENT_SERIALIZER.deserialize(this)
}

/**
 * 设置 static final 字段
 */
fun Field.setStaticFinal(value: Any) {
    val offset = UnsafeAccess.unsafe.staticFieldOffset(this)
    UnsafeAccess.unsafe.putObject(UnsafeAccess.unsafe.staticFieldBase(this), offset, value)
}

/**
 * 判断物品是否为 null 或是空气方块
 */
val ItemStack?.isNull get() = this?.isAir ?: true

/**
 * 判断物品是否为附魔书
 */
val ItemStack.isEnchantedBook get() = itemMeta is EnchantmentStorageMeta

/**
 * 获取/修改物品显示名称
 */
var ItemStack.name
    get() = itemMeta?.displayName
    set(value) {
        modifyMeta<ItemMeta> { setDisplayName(value) }
    }

/**
 * 获取/修改物品耐久
 */
var ItemStack.damage
    get() = (itemMeta as? Damageable)?.damage ?: 0
    set(value) {
        modifyMeta<Damageable> { damage = value }
    }

/**
 * 物品最大耐久度
 * */
val ItemStack.maxDurability: Int
    get() = this.type.maxDurability.toInt()

/**
 * 物品耐久度
 * */
var ItemStack.dura: Int
    get() = this.maxDurability - damage
    set(value) {
        this.damage = this.maxDurability - value
    }

/**
 * 将 Location 的 世界 和 XYZ 信息序列化成文本字符串
 */
val Location.serialized get() = "${world.name},$blockX,$blockY,$blockZ"

/**
 * 对 LivingEntity 造成真实伤害, 插件和原版无法减伤
 */
fun LivingEntity.realDamage(amount: Double, who: Entity? = null) {
    health = maxOf(0.1, health - amount)
    damage(0.1, who)
}


/**
 * 从 PDC 获取内容
 */
operator fun <T, Z> PersistentDataHolder.get(key: String, type: PersistentDataType<T, Z>): Z? {
    return persistentDataContainer.get(NamespacedKey.minecraft(key), type)
}

/**
 * 向 PDC 设置内容
 */
operator fun <T, Z : Any> PersistentDataHolder.set(key: String, type: PersistentDataType<T, Z>, value: Z) {
    persistentDataContainer.set(NamespacedKey.minecraft(key), type, value)
}

/**
 * 判断 PDC 是否包含某个键
 */
fun <T, Z : Any> PersistentDataHolder.has(key: String, type: PersistentDataType<T, Z>): Boolean {
    return persistentDataContainer.has(NamespacedKey.minecraft(key), type)
}

/**
 * 从 PDC 移除内容
 */
fun PersistentDataHolder.remove(key: String) {
    return persistentDataContainer.remove(NamespacedKey.minecraft(key))
}

/**
 * 从 ConfigurationSection 读取 Baffle
 */
fun ConfigurationSection?.baffle(): Baffle? {
    return this?.let {
        val section = it.asMap()
        when {
            "time" in section -> {
                // 按时间阻断
                val time = section["time"]?.cint ?: -1
                if (time > 0) {
                    Baffle.of(time * 50L, TimeUnit.MILLISECONDS)
                } else null
            }
            "count" in section -> {
                // 按次数阻断
                val count = section["count"]?.cint ?: -1
                if (count > 0) {
                    Baffle.of(count)
                } else null
            }
            else -> null
        }
    }
}

/**
 * 从 ConfigurationSection 读取 submit 信息
 */
fun ConfigurationSection?.submit(): Submit {
    return this?.let {
        val section = it.asMap()
        val enable = section["enable"].coerceBoolean(true)
        val now = section["now"].coerceBoolean(false)
        val async = section["async"].coerceBoolean(false)
        val delay = section["delay"].coerceLong(0L)
        val period = section["period"].coerceLong(0L)
        Submit(enable, now, async, delay, period)
    } ?: Submit(false, false, false, 0L, 0L)
}

data class Submit(val enable: Boolean, val now: Boolean, val async: Boolean, val delay: Long, val period: Long)

/**
 * ItemsAdder 是否存在
 */
internal val itemsAdderEnabled = runCatching { Class.forName("dev.lone.itemsadder.api.ItemsAdder") }.isSuccess