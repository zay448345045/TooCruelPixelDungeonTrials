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

@Suppress("UNCHECKED_CAST")
inline fun <reified K, reified V> Bundle.putMap(
    key: String,
    values: Map<K, V>,
    putKey: Bundle.(String, Array<K>) -> Unit,
    putValue: Bundle.(String, Array<V>) -> Unit,
) {
    val keys = arrayOfNulls<K>(values.size)
    val vals = arrayOfNulls<V>(values.size)

    var i = 0
    for ((k, v) in values) {
        keys[i] = k
        vals[i] = v
        i++
    }

    putKey(key + "_keys", keys as Array<K>)
    putValue(key + "_values", vals as Array<V>)
}

inline fun <K, V> Bundle.getMap(
    key: String,
    getKey: Bundle.(String) -> Array<K>,
    getValue: Bundle.(String) -> Array<V>,
): MutableMap<K, V> {
    val keys = getKey(key + "_keys")
    val vals = getValue(key + "_values")

    val m = mutableMapOf<K, V>()
    return keys.zip(vals).toMap(m)
}
