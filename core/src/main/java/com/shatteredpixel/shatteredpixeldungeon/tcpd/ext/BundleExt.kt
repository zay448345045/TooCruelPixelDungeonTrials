package com.shatteredpixel.shatteredpixeldungeon.tcpd.ext

import com.watabou.noosa.Game
import com.watabou.utils.Bundle

fun bundleFromString(data: String): Bundle = Bundle.read(data.byteInputStream())

fun tryBundleFromString(data: String): Bundle? =
    try {
        bundleFromString(data)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

fun Bundle.put(
    key: String,
    values: Array<Enum<*>>,
) {
    val names = values.map { it.name }.toTypedArray()
    put(key, names)
}

inline fun <reified T : Enum<T>> Bundle.getEnumArray(key: String): Array<T> {
    val names = getStringArray(key)
    return names
        .map {
            try {
                java.lang.Enum.valueOf(T::class.java, it)
            } catch (e: IllegalArgumentException) {
                Game.reportException(e)
                T::class.java.enumConstants[0]
            }
        }.toTypedArray()
}
