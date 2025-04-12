package com.shatteredpixel.shatteredpixeldungeon.tcpd.utils

fun <T> MutableList<T?>.compactNotNull() {
    var shift = 0
    for (i in indices) {
        if (this[i] == null) {
            shift++
        } else {
            this[i - shift] = this[i]
        }
    }

    repeat(shift) {
        removeAt(size - 1)
    }
}

inline fun <T> MutableList<T>.filterMapInPlace(crossinline cb: (T) -> T?) {
    var shift = 0
    for (i in indices) {
        val value = cb(this[i])
        if (value == null) {
            shift++
            continue
        }
        this[i - shift] = value
    }

    repeat(shift) {
        removeAt(size - 1)
    }
}
